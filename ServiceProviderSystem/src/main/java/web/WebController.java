package web;

import exceptions.ProviderNotFoundException;
import rest.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/web")
public class WebController {

    @Autowired private ServiceProviderRepository providerRepo;
    @Autowired private CustomerRepository        customerRepo;
    @Autowired private BookingRepository         bookingRepo;

    // HOME
    @GetMapping({"", "/"})
    public String home(Model model, HttpSession session) {
        model.addAttribute("totalProviders",    providerRepo.count());
        model.addAttribute("verifiedProviders", providerRepo.findByIsVerifiedTrue().size());
        model.addAttribute("totalCustomers",    customerRepo.count());
        model.addAttribute("totalBookings",     bookingRepo.count());
        model.addAttribute("loggedInCustomer",  session.getAttribute("customer"));
        model.addAttribute("loggedInAdmin",     session.getAttribute("adminUser"));
        return "index";
    }

    // LOGIN PAGE
    @GetMapping("/login")
    public String loginPage(@RequestParam(required = false) String error,
                            @RequestParam(required = false) String registered,
                            Model model) {
        if (error != null)      model.addAttribute("errorMsg",   "Invalid username or password!");
        if (registered != null) model.addAttribute("successMsg", "Registered successfully! Please login.");
        return "login";
    }

    @PostMapping("/login")
    public String doLogin(@RequestParam String username,
                          @RequestParam String password,
                          @RequestParam String role,
                          HttpSession session, Model model) {
        if ("admin".equals(role)) {
            if ("admin".equals(username) && "admin123".equals(password)) {
                session.setAttribute("adminUser", "admin");
                return "redirect:/web/admin/dashboard";
            }
            model.addAttribute("errorMsg", "Invalid admin credentials!");
            return "login";
        }
        var customer = customerRepo.findByUsernameAndPassword(username, password);
        if (customer.isPresent()) {
            session.setAttribute("customer", customer.get());
            return "redirect:/web/customer/dashboard";
        }
        model.addAttribute("errorMsg", "Invalid username or password!");
        return "login";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/web/";
    }

    // REGISTER
    @GetMapping("/register")
    public String registerPage() { return "register"; }

    @PostMapping("/register")
    public String doRegister(@RequestParam String username,
                             @RequestParam String password,
                             @RequestParam String firstName,
                             @RequestParam String lastName,
                             @RequestParam String email,
                             @RequestParam String phone,
                             @RequestParam String address,
                             Model model) {
        if (customerRepo.existsByUsername(username)) {
            model.addAttribute("errorMsg", "Username already taken. Try another.");
            return "register";
        }
        customerRepo.save(new CustomerEntity(username, password, firstName, lastName, email, phone, address));
        return "redirect:/web/login?registered=true";
    }

    // CUSTOMER DASHBOARD
    @GetMapping("/customer/dashboard")
    public String customerDashboard(HttpSession session, Model model) {
        CustomerEntity customer = getCustomer(session);
        if (customer == null) return "redirect:/web/login";
        List<BookingEntity> myBookings = bookingRepo.findByCustomerIdOrderByScheduledDateDesc(customer.getId());
        long toReview    = bookingRepo.findUnreviewedCompletedByCustomer(customer.getId()).size();
        long completed   = myBookings.stream().filter(b -> "COMPLETED".equals(b.getStatus())).count();
        model.addAttribute("customer",       customer);
        model.addAttribute("myBookings",     myBookings);
        model.addAttribute("reviewCount",    toReview);
        model.addAttribute("totalBookings",  myBookings.size());
        model.addAttribute("completedCount", completed);
        return "customer/dashboard";
    }

    // BOOK SERVICE
    @GetMapping("/customer/book")
    public String bookPage(HttpSession session, Model model,
                           @RequestParam(required = false) String serviceType) {
        if (getCustomer(session) == null) return "redirect:/web/login";
        model.addAttribute("serviceTypes", List.of("Plumbing","Electrical","Cleaning","Carpentry","Painting"));
        if (serviceType != null && !serviceType.isBlank()) {
            model.addAttribute("providers",    providerRepo.findVerifiedByServiceType(serviceType));
            model.addAttribute("selectedType", serviceType);
        }
        return "customer/book";
    }

    @PostMapping("/customer/book")
    public String confirmBook(@RequestParam Long   providerId,
                              @RequestParam String serviceType,
                              @RequestParam String description,
                              @RequestParam String address,
                              @RequestParam String scheduledDate,
                              @RequestParam String scheduledTime,
                              HttpSession session) {
        CustomerEntity customer = getCustomer(session);
        if (customer == null) return "redirect:/web/login";
        ServiceProviderEntity provider = providerRepo.findById(providerId)
                .orElseThrow(() -> new ProviderNotFoundException(providerId));
        LocalDateTime dt = LocalDateTime.parse(scheduledDate + "T" + scheduledTime);
        bookingRepo.save(new BookingEntity(customer, provider, serviceType, description, address, dt));
        return "redirect:/web/customer/mybookings?booked=true";
    }

    // MY BOOKINGS
    @GetMapping("/customer/mybookings")
    public String myBookings(HttpSession session, Model model,
                             @RequestParam(required = false) String booked,
                             @RequestParam(required = false) String reviewed) {
        CustomerEntity customer = getCustomer(session);
        if (customer == null) return "redirect:/web/login";
        model.addAttribute("bookings",  bookingRepo.findByCustomerIdOrderByScheduledDateDesc(customer.getId()));
        model.addAttribute("customer",  customer);
        if (booked   != null) model.addAttribute("successMsg", "Service booked successfully!");
        if (reviewed != null) model.addAttribute("successMsg", "Review submitted! Thank you.");
        return "customer/mybookings";
    }

    // REVIEW PAGE
    @GetMapping("/customer/review/{bookingId}")
    public String reviewPage(@PathVariable Long bookingId, HttpSession session, Model model) {
        CustomerEntity customer = getCustomer(session);
        if (customer == null) return "redirect:/web/login";
        BookingEntity booking = bookingRepo.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));
        if (!booking.getCustomer().getId().equals(customer.getId())) return "redirect:/web/customer/mybookings";
        if (!"COMPLETED".equals(booking.getStatus()))                return "redirect:/web/customer/mybookings";
        if (booking.isReviewed())                                    return "redirect:/web/customer/mybookings";
        model.addAttribute("booking",  booking);
        model.addAttribute("customer", customer);
        return "customer/review";
    }

    @PostMapping("/customer/review/{bookingId}")
    public String submitReview(@PathVariable Long bookingId,
                               @RequestParam int    rating,
                               @RequestParam String reviewTitle,
                               @RequestParam String reviewComment,
                               HttpSession session) {
        CustomerEntity customer = getCustomer(session);
        if (customer == null) return "redirect:/web/login";
        BookingEntity booking = bookingRepo.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));
        booking.setRating(rating);
        booking.setReviewTitle(reviewTitle);
        booking.setReviewComment(reviewComment);
        bookingRepo.save(booking);
        // Recalculate provider average rating
        ServiceProviderEntity provider = booking.getProvider();
        double avg = bookingRepo.findByProviderIdOrderByScheduledDateDesc(provider.getId())
                .stream().filter(b -> b.getRating() > 0)
                .mapToInt(BookingEntity::getRating).average().orElse(0.0);
        provider.setAverageRating(Math.round(avg * 10.0) / 10.0);
        providerRepo.save(provider);
        return "redirect:/web/customer/mybookings?reviewed=true";
    }

    // PUBLIC PROVIDERS
    @GetMapping("/providers")
    public String listProviders(Model model, HttpSession session,
                                @RequestParam(required = false) String type,
                                @RequestParam(required = false) String search) {
        List<ServiceProviderEntity> providers;
        if (type   != null && !type.isBlank())   { providers = providerRepo.findByServiceType(type);                           model.addAttribute("filterType",     type); }
        else if (search != null && !search.isBlank()) { providers = providerRepo.findByBusinessNameContainingIgnoreCase(search); model.addAttribute("searchKeyword", search); }
        else                                          { providers = providerRepo.findAll(); }
        model.addAttribute("providers",        providers);
        model.addAttribute("totalCount",       providers.size());
        model.addAttribute("loggedInCustomer", session.getAttribute("customer"));
        model.addAttribute("loggedInAdmin",    session.getAttribute("adminUser"));
        return "providers";
    }

    @GetMapping("/providers/{id}")
    public String providerDetail(@PathVariable Long id, Model model, HttpSession session) {
        ServiceProviderEntity provider = providerRepo.findById(id)
                .orElseThrow(() -> new ProviderNotFoundException(id));
        List<BookingEntity> reviews = bookingRepo.findByProviderIdOrderByScheduledDateDesc(id)
                .stream().filter(b -> b.getRating() > 0).toList();
        model.addAttribute("provider",         provider);
        model.addAttribute("reviews",          reviews);
        model.addAttribute("loggedInCustomer", session.getAttribute("customer"));
        return "provider-detail";
    }

    // ADMIN PAGES
    @GetMapping("/admin/dashboard")
    public String adminDashboard(HttpSession session, Model model) {
        if (session.getAttribute("adminUser") == null) return "redirect:/web/login";
        model.addAttribute("totalProviders",    providerRepo.count());
        model.addAttribute("verifiedProviders", providerRepo.findByIsVerifiedTrue().size());
        model.addAttribute("pendingProviders",  providerRepo.count() - providerRepo.findByIsVerifiedTrue().size());
        model.addAttribute("totalCustomers",    customerRepo.count());
        model.addAttribute("totalBookings",     bookingRepo.count());
        model.addAttribute("completedBookings", bookingRepo.findByStatus("COMPLETED").size());
        double revenue = bookingRepo.findByStatus("COMPLETED").stream().mapToDouble(BookingEntity::getFinalPrice).sum();
        model.addAttribute("totalRevenue",      revenue);
        return "admin/dashboard";
    }

    @GetMapping("/admin/providers")
    public String adminProviders(HttpSession session, Model model) {
        if (session.getAttribute("adminUser") == null) return "redirect:/web/login";
        model.addAttribute("providers", providerRepo.findAll());
        return "admin/providers";
    }

    @GetMapping("/admin/customers")
    public String adminCustomers(HttpSession session, Model model) {
        if (session.getAttribute("adminUser") == null) return "redirect:/web/login";
        model.addAttribute("customers", customerRepo.findAll());
        return "admin/customers";
    }

    @GetMapping("/admin/bookings")
    public String adminBookings(HttpSession session, Model model) {
        if (session.getAttribute("adminUser") == null) return "redirect:/web/login";
        model.addAttribute("bookings", bookingRepo.findAll());
        return "admin/bookings";
    }

    @PostMapping("/admin/bookings/{id}/status")
    public String updateBookingStatus(@PathVariable Long id,
                                      @RequestParam String status,
                                      @RequestParam(required = false) String finalPrice,
                                      HttpSession session) {
        if (session.getAttribute("adminUser") == null) return "redirect:/web/login";
        BookingEntity b = bookingRepo.findById(id).orElseThrow(() -> new RuntimeException("Booking not found"));
        b.setStatus(status);
        if (finalPrice != null && !finalPrice.isBlank()) b.setFinalPrice(Double.parseDouble(finalPrice));
        bookingRepo.save(b);
        return "redirect:/web/admin/bookings";
    }

    @GetMapping("/providers/add")
    public String showAddForm(Model model, HttpSession session) {
        if (session.getAttribute("adminUser") == null) return "redirect:/web/login";
        model.addAttribute("provider",     new ServiceProviderEntity());
        model.addAttribute("serviceTypes", List.of("Plumbing","Electrical","Cleaning","Carpentry","Painting"));
        return "add-provider";
    }

    @PostMapping("/providers/add")
    public String saveProvider(@ModelAttribute ServiceProviderEntity provider, HttpSession session, Model model) {
        if (session.getAttribute("adminUser") == null) return "redirect:/web/login";
        if (providerRepo.findAll().stream().anyMatch(p -> p.getUsername().equalsIgnoreCase(provider.getUsername()))) {
            model.addAttribute("errorMessage", "Username already exists!");
            model.addAttribute("provider", provider);
            model.addAttribute("serviceTypes", List.of("Plumbing","Electrical","Cleaning","Carpentry","Painting"));
            return "add-provider";
        }
        provider.setCreatedAt(LocalDate.now());
        provider.setVerified(false);
        provider.setAverageRating(0.0);
        providerRepo.save(provider);
        return "redirect:/web/admin/providers";
    }

    @PostMapping("/providers/{id}/verify")
    public String verifyProvider(@PathVariable Long id, HttpSession session) {
        if (session.getAttribute("adminUser") == null) return "redirect:/web/login";
        ServiceProviderEntity p = providerRepo.findById(id).orElseThrow(() -> new ProviderNotFoundException(id));
        p.setVerified(true);
        providerRepo.save(p);
        return "redirect:/web/admin/providers";
    }

    @PostMapping("/providers/{id}/delete")
    public String deleteProvider(@PathVariable Long id, HttpSession session) {
        if (session.getAttribute("adminUser") == null) return "redirect:/web/login";
        providerRepo.deleteById(id);
        return "redirect:/web/admin/providers";
    }

    // HELPER
    private CustomerEntity getCustomer(HttpSession session) {
        Object obj = session.getAttribute("customer");
        if (obj instanceof CustomerEntity c) return customerRepo.findById(c.getId()).orElse(null);
        return null;
    }
}
