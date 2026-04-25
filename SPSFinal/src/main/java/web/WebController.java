package web;

import exceptions.ProviderNotFoundException;
import rest.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;
import java.time.LocalDate;
import java.util.List;
import java.util.OptionalDouble;

@Controller
@RequestMapping("/web")
public class WebController {

    private static final String ADMIN_USERNAME = "admin";
    private static final String ADMIN_PASSWORD = "admin123";

    @Autowired private ServiceProviderRepository providerRepo;
    @Autowired private CustomerRepository        customerRepo;
    @Autowired private BookingRepository         bookingRepo;

    // ── HOME ──────────────────────────────────────────────────
    @GetMapping("/")
    public String home(Model model, HttpSession session) {
        long total    = providerRepo.count();
        long verified = providerRepo.findByIsVerifiedTrue().size();
        model.addAttribute("totalProviders",   total);
        model.addAttribute("verifiedProviders",verified);
        model.addAttribute("pendingProviders", total - verified);
        model.addAttribute("totalCustomers",   customerRepo.count());
        model.addAttribute("totalBookings",    bookingRepo.count());
        addSession(model, session);
        return "index";
    }

    // ── REGISTER ──────────────────────────────────────────────
    @GetMapping("/register")
    public String showRegisterForm(HttpSession session) {
        if (session.getAttribute("customerId") != null) return "redirect:/web/customer/dashboard";
        if (session.getAttribute("providerId") != null) return "redirect:/web/provider/dashboard";
        return "register";
    }

    @PostMapping("/register/customer")
    public String registerCustomer(@RequestParam String username, @RequestParam String password,
            @RequestParam String firstName, @RequestParam String lastName,
            @RequestParam String email, @RequestParam String phone,
            @RequestParam String address, Model model, HttpSession session) {
        if (customerRepo.existsByUsername(username)) {
            model.addAttribute("error", "Username '" + username + "' is already taken.");
            model.addAttribute("activeTab", "customer");
            return "register";
        }
        if (customerRepo.existsByEmail(email)) {
            model.addAttribute("error", "Email '" + email + "' is already registered.");
            model.addAttribute("activeTab", "customer");
            return "register";
        }
        CustomerEntity c = new CustomerEntity(username, password, firstName, lastName, email, phone, address);
        customerRepo.save(c);
        session.setAttribute("customerId",   c.getId());
        session.setAttribute("customerName", c.getFirstName() + " " + c.getLastName());
        session.setAttribute("role",         "customer");
        return "redirect:/web/customer/dashboard";
    }

    @PostMapping("/register/provider")
    public String registerProvider(@RequestParam String username, @RequestParam String password,
            @RequestParam String businessName, @RequestParam String serviceType,
            @RequestParam String email, @RequestParam String phone,
            @RequestParam int yearsOfExperience, @RequestParam double hourlyRate,
            Model model, HttpSession session) {
        if (providerRepo.existsByUsername(username)) {
            model.addAttribute("error", "Username '" + username + "' is already taken.");
            model.addAttribute("activeTab", "provider");
            return "register";
        }
        ServiceProviderEntity p = new ServiceProviderEntity(
                businessName, serviceType, username, password, email, phone, yearsOfExperience, hourlyRate);
        providerRepo.save(p);
        session.setAttribute("providerId",       p.getId());
        session.setAttribute("providerName",     p.getBusinessName());
        session.setAttribute("providerVerified", p.isVerified());
        session.setAttribute("role",             "provider");
        return "redirect:/web/provider/dashboard";
    }

    // ── LOGIN ─────────────────────────────────────────────────
    @GetMapping("/login")
    public String showLoginForm(HttpSession session) {
        if (session.getAttribute("customerId") != null) return "redirect:/web/customer/dashboard";
        if (session.getAttribute("providerId") != null) return "redirect:/web/provider/dashboard";
        if (Boolean.TRUE.equals(session.getAttribute("isAdmin"))) return "redirect:/web/admin/dashboard";
        return "login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String username, @RequestParam String password,
            @RequestParam(defaultValue = "customer") String role,
            Model model, HttpSession session) {

        if ("admin".equals(role)) {
            if (ADMIN_USERNAME.equals(username) && ADMIN_PASSWORD.equals(password)) {
                session.setAttribute("isAdmin", true);
                session.setAttribute("role",    "admin");
                return "redirect:/web/admin/dashboard";
            }
            model.addAttribute("error",        "Invalid admin credentials.");
            model.addAttribute("selectedRole", "admin");
            return "login";
        }

        if ("provider".equals(role)) {
            var opt = providerRepo.findByUsername(username);
            if (opt.isPresent() && opt.get().getPassword().equals(password)) {
                ServiceProviderEntity p = opt.get();
                session.setAttribute("providerId",       p.getId());
                session.setAttribute("providerName",     p.getBusinessName());
                session.setAttribute("providerVerified", p.isVerified());
                session.setAttribute("role",             "provider");
                return "redirect:/web/provider/dashboard";
            }
            model.addAttribute("error",        "Invalid provider username or password.");
            model.addAttribute("selectedRole", "provider");
            return "login";
        }

        // customer
        var opt = customerRepo.findByUsername(username);
        if (opt.isPresent() && opt.get().getPassword().equals(password)) {
            CustomerEntity c = opt.get();
            session.setAttribute("customerId",   c.getId());
            session.setAttribute("customerName", c.getFirstName() + " " + c.getLastName());
            session.setAttribute("role",         "customer");
            return "redirect:/web/customer/dashboard";
        }
        model.addAttribute("error",        "Invalid username or password.");
        model.addAttribute("selectedRole", "customer");
        return "login";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/web/login";
    }

    // ── CUSTOMER DASHBOARD ────────────────────────────────────
    @GetMapping("/customer/dashboard")
    public String customerDashboard(Model model, HttpSession session) {
        Long cid = (Long) session.getAttribute("customerId");
        if (cid == null) return "redirect:/web/login";
        CustomerEntity customer = customerRepo.findById(cid).orElse(null);
        if (customer == null) return "redirect:/web/logout";

        List<BookingEntity> bookings     = bookingRepo.findByCustomer(customer);
        long pendingCount    = bookings.stream().filter(b -> "PENDING".equals(b.getStatus())).count();
        long activeCount     = bookings.stream().filter(b ->
                "ACCEPTED".equals(b.getStatus()) || "IN_PROGRESS".equals(b.getStatus())).count();
        long workDoneCount   = bookings.stream().filter(b -> "WORK_DONE".equals(b.getStatus())).count();
        long completedCount  = bookings.stream().filter(b -> "COMPLETED".equals(b.getStatus())).count();

        model.addAttribute("customer",      customer);
        model.addAttribute("bookings",      bookings);
        model.addAttribute("pendingCount",  pendingCount);
        model.addAttribute("activeCount",   activeCount);
        model.addAttribute("workDoneCount", workDoneCount);
        model.addAttribute("completedCount",completedCount);
        addSession(model, session);
        return "customer-dashboard";
    }

    // ── CUSTOMER BOOK ─────────────────────────────────────────
    @GetMapping("/customer/book")
    public String showBookingForm(Model model, HttpSession session) {
        if (session.getAttribute("customerId") == null) return "redirect:/web/login";
        model.addAttribute("providers",    providerRepo.findByIsVerifiedTrue());
        model.addAttribute("serviceTypes", serviceTypes());
        model.addAttribute("today",        LocalDate.now().toString());
        addSession(model, session);
        return "book-service";
    }

    @PostMapping("/customer/book")
    public String bookService(@RequestParam Long providerId, @RequestParam String bookingDate,
            @RequestParam(required = false) String notes,
            HttpSession session, RedirectAttributes ra) {
        Long cid = (Long) session.getAttribute("customerId");
        if (cid == null) return "redirect:/web/login";
        CustomerEntity customer        = customerRepo.findById(cid).orElseThrow();
        ServiceProviderEntity provider = providerRepo.findById(providerId)
                .orElseThrow(() -> new ProviderNotFoundException(providerId));
        bookingRepo.save(new BookingEntity(customer, provider, provider.getServiceType(),
                LocalDate.parse(bookingDate), notes));
        ra.addFlashAttribute("success", "Booking sent to " + provider.getBusinessName() + "! Waiting for acceptance.");
        return "redirect:/web/customer/dashboard";
    }

    // ── CUSTOMER CONFIRMS SERVICE DONE ────────────────────────
    @PostMapping("/customer/confirm/{bookingId}")
    public String customerConfirmDone(@PathVariable Long bookingId,
            HttpSession session, RedirectAttributes ra) {
        Long cid = (Long) session.getAttribute("customerId");
        if (cid == null) return "redirect:/web/login";
        BookingEntity b = bookingRepo.findById(bookingId).orElseThrow();
        // Only the booking's customer can confirm, and only when status is WORK_DONE
        if (!b.getCustomer().getId().equals(cid)) return "redirect:/web/customer/dashboard";
        if (!"WORK_DONE".equals(b.getStatus()))   return "redirect:/web/customer/dashboard";
        b.setStatus("COMPLETED");
        b.setCompletedDate(LocalDate.now());
        bookingRepo.save(b);
        ra.addFlashAttribute("success", "Service confirmed as completed! You can now rate the provider.");
        return "redirect:/web/customer/dashboard";
    }

    // ── CUSTOMER RATE ─────────────────────────────────────────
    @GetMapping("/customer/rate/{bookingId}")
    public String showRatingForm(@PathVariable Long bookingId, Model model, HttpSession session) {
        Long cid = (Long) session.getAttribute("customerId");
        if (cid == null) return "redirect:/web/login";
        BookingEntity booking = bookingRepo.findById(bookingId).orElseThrow();
        if (!booking.getCustomer().getId().equals(cid) || !"COMPLETED".equals(booking.getStatus()))
            return "redirect:/web/customer/dashboard";
        model.addAttribute("booking", booking);
        addSession(model, session);
        return "rate-service";
    }

    @PostMapping("/customer/rate/{bookingId}")
    public String submitRating(@PathVariable Long bookingId,
            @RequestParam int rating, @RequestParam String feedback,
            HttpSession session, RedirectAttributes ra) {
        Long cid = (Long) session.getAttribute("customerId");
        if (cid == null) return "redirect:/web/login";
        BookingEntity booking = bookingRepo.findById(bookingId).orElseThrow();
        booking.setRating(rating);
        booking.setFeedback(feedback);
        bookingRepo.save(booking);
        ServiceProviderEntity provider = booking.getProvider();
        OptionalDouble avg = bookingRepo.findCompletedWithRatingByProvider(provider)
                .stream().mapToInt(BookingEntity::getRating).average();
        provider.setAverageRating(avg.orElse(0.0));
        providerRepo.save(provider);
        ra.addFlashAttribute("success", "Thank you for your feedback!");
        return "redirect:/web/customer/dashboard";
    }

    // ── PROVIDER DASHBOARD ────────────────────────────────────
    @GetMapping("/provider/dashboard")
    public String providerDashboard(Model model, HttpSession session) {
        Long pid = (Long) session.getAttribute("providerId");
        if (pid == null) return "redirect:/web/login";
        ServiceProviderEntity provider = providerRepo.findById(pid).orElse(null);
        if (provider == null) return "redirect:/web/logout";

        List<BookingEntity> bookings     = bookingRepo.findByProvider(provider);
        long pendingCount    = bookings.stream().filter(b -> "PENDING".equals(b.getStatus())).count();
        long activeCount     = bookings.stream().filter(b ->
                "ACCEPTED".equals(b.getStatus()) || "IN_PROGRESS".equals(b.getStatus())).count();
        long completedCount  = bookings.stream().filter(b -> "COMPLETED".equals(b.getStatus())).count();
        List<BookingEntity> reviews = bookingRepo.findCompletedWithRatingByProvider(provider);

        model.addAttribute("provider",      provider);
        model.addAttribute("bookings",      bookings);
        model.addAttribute("pendingCount",  pendingCount);
        model.addAttribute("activeCount",   activeCount);
        model.addAttribute("completedCount",completedCount);
        model.addAttribute("reviews",       reviews);
        addSession(model, session);
        return "provider-dashboard";
    }

    // ── PROVIDER BOOKING ACTIONS ──────────────────────────────
    // Provider accepts a PENDING booking
    @PostMapping("/provider/booking/{id}/accept")
    public String providerAccept(@PathVariable Long id, HttpSession session, RedirectAttributes ra) {
        Long pid = (Long) session.getAttribute("providerId");
        if (pid == null) return "redirect:/web/login";
        BookingEntity b = bookingRepo.findById(id).orElseThrow();
        if (!b.getProvider().getId().equals(pid)) return "redirect:/web/provider/dashboard";
        if (!"PENDING".equals(b.getStatus()))     return "redirect:/web/provider/dashboard";
        b.setStatus("ACCEPTED");
        bookingRepo.save(b);
        ra.addFlashAttribute("success", "Booking #" + id + " accepted! Customer has been notified.");
        return "redirect:/web/provider/dashboard";
    }

    // Provider starts work (ACCEPTED → IN_PROGRESS)
    @PostMapping("/provider/booking/{id}/start")
    public String providerStart(@PathVariable Long id, HttpSession session, RedirectAttributes ra) {
        Long pid = (Long) session.getAttribute("providerId");
        if (pid == null) return "redirect:/web/login";
        BookingEntity b = bookingRepo.findById(id).orElseThrow();
        if (!b.getProvider().getId().equals(pid)) return "redirect:/web/provider/dashboard";
        if (!"ACCEPTED".equals(b.getStatus()))    return "redirect:/web/provider/dashboard";
        b.setStatus("IN_PROGRESS");
        bookingRepo.save(b);
        ra.addFlashAttribute("success", "Booking #" + id + " marked as In Progress.");
        return "redirect:/web/provider/dashboard";
    }

    // Provider marks work done (IN_PROGRESS → WORK_DONE)
    @PostMapping("/provider/booking/{id}/done")
    public String providerMarkDone(@PathVariable Long id, HttpSession session, RedirectAttributes ra) {
        Long pid = (Long) session.getAttribute("providerId");
        if (pid == null) return "redirect:/web/login";
        BookingEntity b = bookingRepo.findById(id).orElseThrow();
        if (!b.getProvider().getId().equals(pid))  return "redirect:/web/provider/dashboard";
        if (!"IN_PROGRESS".equals(b.getStatus()))  return "redirect:/web/provider/dashboard";
        b.setStatus("WORK_DONE");
        b.setWorkDoneDate(LocalDate.now());
        bookingRepo.save(b);
        ra.addFlashAttribute("success", "Work marked as done! Waiting for customer to confirm.");
        return "redirect:/web/provider/dashboard";
    }

    // Provider cancels a PENDING booking
    @PostMapping("/provider/booking/{id}/cancel")
    public String providerCancel(@PathVariable Long id, HttpSession session, RedirectAttributes ra) {
        Long pid = (Long) session.getAttribute("providerId");
        if (pid == null) return "redirect:/web/login";
        BookingEntity b = bookingRepo.findById(id).orElseThrow();
        if (!b.getProvider().getId().equals(pid)) return "redirect:/web/provider/dashboard";
        if (!"PENDING".equals(b.getStatus()))     return "redirect:/web/provider/dashboard";
        b.setStatus("CANCELLED");
        bookingRepo.save(b);
        ra.addFlashAttribute("success", "Booking #" + id + " cancelled.");
        return "redirect:/web/provider/dashboard";
    }

    // Provider update profile
    @PostMapping("/provider/profile")
    public String updateProviderProfile(@RequestParam String businessName,
            @RequestParam String phone, @RequestParam double hourlyRate,
            @RequestParam int yearsOfExperience,
            HttpSession session, RedirectAttributes ra) {
        Long pid = (Long) session.getAttribute("providerId");
        if (pid == null) return "redirect:/web/login";
        ServiceProviderEntity p = providerRepo.findById(pid).orElseThrow();
        p.setBusinessName(businessName);
        p.setPhone(phone);
        p.setHourlyRate(hourlyRate);
        p.setYearsOfExperience(yearsOfExperience);
        providerRepo.save(p);
        session.setAttribute("providerName", p.getBusinessName());
        ra.addFlashAttribute("success", "Profile updated successfully!");
        return "redirect:/web/provider/dashboard";
    }

    // ── ADMIN ─────────────────────────────────────────────────
    @GetMapping("/admin/dashboard")
    public String adminDashboard(Model model, HttpSession session) {
        if (!Boolean.TRUE.equals(session.getAttribute("isAdmin"))) return "redirect:/web/login";
        model.addAttribute("totalProviders",   providerRepo.count());
        model.addAttribute("verifiedProviders",providerRepo.findByIsVerifiedTrue().size());
        model.addAttribute("pendingProviders", providerRepo.count() - providerRepo.findByIsVerifiedTrue().size());
        model.addAttribute("totalCustomers",   customerRepo.count());
        model.addAttribute("totalBookings",    bookingRepo.count());
        model.addAttribute("providers",        providerRepo.findAll());
        model.addAttribute("customers",        customerRepo.findAll());
        addSession(model, session);
        return "admin-dashboard";
    }

    @GetMapping("/admin/bookings")
    public String adminBookings(Model model, HttpSession session) {
        if (!Boolean.TRUE.equals(session.getAttribute("isAdmin"))) return "redirect:/web/login";
        model.addAttribute("bookings", bookingRepo.findAll());
        addSession(model, session);
        return "admin-bookings";
    }

    @PostMapping("/providers/{id}/verify")
    public String verifyProvider(@PathVariable Long id, HttpSession session) {
        if (!Boolean.TRUE.equals(session.getAttribute("isAdmin"))) return "redirect:/web/login";
        ServiceProviderEntity p = providerRepo.findById(id)
                .orElseThrow(() -> new ProviderNotFoundException(id));
        p.setVerified(true);
        providerRepo.save(p);
        return "redirect:/web/providers/" + id;
    }

    @PostMapping("/providers/{id}/delete")
    public String deleteProvider(@PathVariable Long id, HttpSession session) {
        if (!Boolean.TRUE.equals(session.getAttribute("isAdmin"))) return "redirect:/web/login";
        if (!providerRepo.existsById(id)) throw new ProviderNotFoundException(id);
        providerRepo.deleteById(id);
        return "redirect:/web/providers";
    }

    // ── PUBLIC PROVIDERS ──────────────────────────────────────
    @GetMapping("/providers")
    public String listProviders(Model model, HttpSession session,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String search) {
        List<ServiceProviderEntity> providers;
        if (type != null && !type.isBlank()) {
            providers = providerRepo.findByServiceType(type);
            model.addAttribute("filterType", type);
        } else if (search != null && !search.isBlank()) {
            providers = providerRepo.findByBusinessNameContainingIgnoreCase(search);
            model.addAttribute("searchKeyword", search);
        } else {
            providers = providerRepo.findAll();
        }
        model.addAttribute("providers",  providers);
        model.addAttribute("totalCount", providers.size());
        addSession(model, session);
        return "providers";
    }

    @GetMapping("/providers/{id}")
    public String providerDetail(@PathVariable Long id, Model model, HttpSession session) {
        ServiceProviderEntity provider = providerRepo.findById(id)
                .orElseThrow(() -> new ProviderNotFoundException(id));
        model.addAttribute("provider", provider);
        model.addAttribute("reviews",  bookingRepo.findCompletedWithRatingByProvider(provider));
        addSession(model, session);
        return "provider-detail";
    }

    @GetMapping("/providers/top")
    public String topProviders(Model model, HttpSession session) {
        List<ServiceProviderEntity> top = providerRepo.findTopRatedProviders();
        model.addAttribute("providers",  top);
        model.addAttribute("pageTitle",  "Top Rated Providers");
        model.addAttribute("totalCount", top.size());
        addSession(model, session);
        return "providers";
    }

    @GetMapping("/providers/add")
    public String showAddForm(Model model, HttpSession session) {
        if (!Boolean.TRUE.equals(session.getAttribute("isAdmin"))) return "redirect:/web/login";
        model.addAttribute("provider",     new ServiceProviderEntity());
        model.addAttribute("serviceTypes", serviceTypes());
        addSession(model, session);
        return "add-provider";
    }

    @PostMapping("/providers/add")
    public String saveProvider(@ModelAttribute ServiceProviderEntity provider, Model model, HttpSession session) {
        if (!Boolean.TRUE.equals(session.getAttribute("isAdmin"))) return "redirect:/web/login";
        if (providerRepo.existsByUsername(provider.getUsername())) {
            model.addAttribute("errorMessage", "Username already exists!");
            model.addAttribute("provider",     provider);
            model.addAttribute("serviceTypes", serviceTypes());
            return "add-provider";
        }
        provider.setCreatedAt(LocalDate.now());
        provider.setVerified(false);
        provider.setAverageRating(0.0);
        providerRepo.save(provider);
        return "redirect:/web/providers";
    }

    // ── HELPER ────────────────────────────────────────────────
    private void addSession(Model model, HttpSession session) {
        model.addAttribute("isAdmin",        Boolean.TRUE.equals(session.getAttribute("isAdmin")));
        model.addAttribute("isCustomer",     session.getAttribute("customerId") != null);
        model.addAttribute("isProvider",     session.getAttribute("providerId") != null);
        model.addAttribute("customerName",   session.getAttribute("customerName"));
        model.addAttribute("providerName",   session.getAttribute("providerName"));
        model.addAttribute("providerVerified", session.getAttribute("providerVerified"));
    }

    private List<String> serviceTypes() {
        return List.of("Plumbing","Electrical","Cleaning","Carpentry","Painting");
    }
}
