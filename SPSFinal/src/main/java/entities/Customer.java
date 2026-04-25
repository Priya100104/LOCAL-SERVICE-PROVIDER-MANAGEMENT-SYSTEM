package entities;

import system.DataManager;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Customer implements Serializable {

    private static final long serialVersionUID = 1L;

    private int       customerId;
    private User      user;
    private String    firstName;
    private String    lastName;
    private String    address;
    private LocalDate joinDate;

    public Customer(int customerId, String username, String email, String password,
                    String phone, String firstName, String lastName, String address) {
        this.customerId = customerId;
        this.user       = new User(customerId, username, email, password, phone);
        this.firstName  = firstName;
        this.lastName   = lastName;
        this.address    = address;
        this.joinDate   = LocalDate.now();
    }

    public int      getCustomerId() { return customerId; }
    public User     getUser()       { return user; }
    public String   getFirstName()  { return firstName; }
    public String   getLastName()   { return lastName; }
    public String   getAddress()    { return address; }
    public LocalDate getJoinDate()  { return joinDate; }

    public void setAddress(String address) { this.address = address; }

    // ===================== CUSTOMER MENU =====================
    public void showCustomerMenu(Scanner scanner, DataManager dataManager) {
        while (dataManager.getCurrentCustomer() != null) {
            System.out.println("\n========== CUSTOMER DASHBOARD ==========");
            System.out.println("Welcome, " + firstName + " " + lastName);
            System.out.println("1. Book a New Service");
            System.out.println("2. View My Bookings");
            System.out.println("3. View Available Providers");
            System.out.println("4. Write a Review");
            System.out.println("5. View My Reviews");
            System.out.println("6. View My Profile");
            System.out.println("7. Logout");
            System.out.print("Enter your choice (1-7): ");

            try {
                int choice = Integer.parseInt(scanner.nextLine());
                switch (choice) {
                    case 1 -> bookNewService(scanner, dataManager);
                    case 2 -> viewCustomerBookings(scanner, dataManager);
                    case 3 -> viewAllProviders(dataManager);
                    case 4 -> writeReview(scanner, dataManager);
                    case 5 -> viewCustomerReviews(dataManager);
                    case 6 -> viewCustomerProfile(dataManager);
                    case 7 -> { dataManager.logout(); return; }
                    default -> System.out.println("Invalid choice! Please enter 1-7.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number!");
            }
        }
    }

    private void bookNewService(Scanner scanner, DataManager dataManager) {
        System.out.println("\n========== BOOK A SERVICE ==========");
        dataManager.viewAllServiceTypes();
        try {
            System.out.print("\nSelect Service Type ID: ");
            int serviceTypeId = Integer.parseInt(scanner.nextLine());

            ServiceType selectedType = null;
            for (ServiceType t : dataManager.getServiceTypes()) {
                if (t.getServiceTypeId() == serviceTypeId) { selectedType = t; break; }
            }
            if (selectedType == null) { System.out.println("Invalid service type!"); return; }

            List<Provider> availableProviders = new ArrayList<>();
            for (Provider p : dataManager.getProviders()) {
                if (p.getServiceType().getServiceTypeId() == serviceTypeId && p.isVerified())
                    availableProviders.add(p);
            }
            if (availableProviders.isEmpty()) { System.out.println("No providers available."); return; }

            System.out.println("\nAvailable Providers:");
            System.out.println("ID  Business Name          Rating  Experience  Hourly Rate");
            System.out.println("---------------------------------------------------------");
            for (Provider p : availableProviders) {
                System.out.printf("%-3d %-23s %-7.1f %-11d ₹%-9.0f%n",
                    p.getProviderId(),
                    (p.getBusinessName().length() > 23 ? p.getBusinessName().substring(0, 20) + "..." : p.getBusinessName()),
                    p.getAverageRating(), p.getYearsOfExperience(), p.getHourlyRate());
            }
            System.out.print("\nSelect Provider ID: ");
            int providerId = Integer.parseInt(scanner.nextLine());

            Provider selectedProvider = null;
            for (Provider p : availableProviders) {
                if (p.getProviderId() == providerId) { selectedProvider = p; break; }
            }
            if (selectedProvider == null) { System.out.println("Invalid provider selection!"); return; }

            System.out.print("Enter Service Description: "); String description = scanner.nextLine();
            System.out.print("Enter Service Address: ");     String address     = scanner.nextLine();
            System.out.print("Enter Preferred Date (YYYY-MM-DD): "); String dateStr = scanner.nextLine();
            System.out.print("Enter Preferred Time (HH:MM): ");      String timeStr = scanner.nextLine();

            LocalDateTime scheduledDate = LocalDateTime.parse(dateStr + "T" + timeStr + ":00");

            int newServiceId = dataManager.getServices().size() + 1;
            Service newService = new Service(newServiceId, selectedProvider, this,
                                             selectedType, description, address, scheduledDate);
            dataManager.addService(newService);

            int newAppointmentId = dataManager.getAppointments().size() + 1;
            Appointment newAppointment = new Appointment(newAppointmentId, newService, scheduledDate);
            dataManager.addAppointment(newAppointment);

            System.out.println("\n✅ Service booked successfully!");
            System.out.println("Service ID: " + newServiceId);
            System.out.println("Appointment scheduled for: " + scheduledDate);
        } catch (Exception e) {
            System.out.println("Booking failed! Please check your inputs.");
        }
    }

    private void viewCustomerBookings(Scanner scanner, DataManager dataManager) {
        System.out.println("\n========== MY BOOKINGS ==========");
        List<Service> customerServices = new ArrayList<>();
        for (Service s : dataManager.getServices()) {
            if (s.getCustomer().getCustomerId() == this.customerId) customerServices.add(s);
        }
        if (customerServices.isEmpty()) { System.out.println("No bookings found."); return; }

        System.out.println("ID  Service          Provider              Date            Status       Price");
        System.out.println("---------------------------------------------------------------------------");
        for (Service s : customerServices) {
            System.out.printf("%-3d %-15s %-21s %-15s %-12s ₹%-9.2f%n",
                s.getServiceId(), s.getServiceType().getServiceName(),
                (s.getProvider().getBusinessName().length() > 21 ?
                 s.getProvider().getBusinessName().substring(0, 18) + "..." :
                 s.getProvider().getBusinessName()),
                s.getScheduledDate().toLocalDate(), s.getStatus(), s.getFinalPrice());
        }
    }

    private void viewAllProviders(DataManager dataManager) {
        System.out.println("\n========== ALL PROVIDERS ==========");
        System.out.println("ID  Business Name          Service        Rating  Experience  Verified");
        System.out.println("---------------------------------------------------------------------");
        for (Provider p : dataManager.getProviders()) {
            if (p.isVerified()) {
                System.out.printf("%-3d %-23s %-15s %-7.1f %-11d %-8s%n",
                    p.getProviderId(),
                    (p.getBusinessName().length() > 23 ? p.getBusinessName().substring(0, 20) + "..." : p.getBusinessName()),
                    p.getServiceType().getServiceName(),
                    p.getAverageRating(), p.getYearsOfExperience(), "Yes");
            }
        }
    }

    private void writeReview(Scanner scanner, DataManager dataManager) {
        System.out.println("\n========== WRITE A REVIEW ==========");
        List<Service> completedServices = new ArrayList<>();
        for (Service s : dataManager.getServices()) {
            if (s.getCustomer().getCustomerId() == this.customerId && s.getStatus().equalsIgnoreCase("completed"))
                completedServices.add(s);
        }
        if (completedServices.isEmpty()) { System.out.println("No completed services to review."); return; }

        System.out.println("Completed Services:");
        for (Service s : completedServices) {
            System.out.printf("%-3d %-15s %-21s %-15s%n",
                s.getServiceId(), s.getServiceType().getServiceName(),
                s.getProvider().getBusinessName(), s.getScheduledDate().toLocalDate());
        }
        try {
            System.out.print("\nSelect Service ID to review: ");
            int serviceId = Integer.parseInt(scanner.nextLine());
            Service selected = null;
            for (Service s : completedServices) { if (s.getServiceId() == serviceId) { selected = s; break; } }
            if (selected == null) { System.out.println("Service not found!"); return; }

            for (Review r : dataManager.getReviews()) {
                if (r.getService().getServiceId() == serviceId && r.getCustomer().getCustomerId() == this.customerId) {
                    System.out.println("You have already reviewed this service!"); return;
                }
            }
            System.out.print("Enter Rating (1-5): ");
            int rating = Integer.parseInt(scanner.nextLine());
            if (rating < 1 || rating > 5) { System.out.println("Rating must be 1-5!"); return; }

            System.out.print("Enter Review Title: ");   String title   = scanner.nextLine();
            System.out.print("Enter Review Comments: "); String comment = scanner.nextLine();

            int newReviewId = dataManager.getReviews().size() + 1;
            Review newReview = new Review(newReviewId, this, selected.getProvider(), selected, rating, title, comment);
            dataManager.addReview(newReview);
            selected.getProvider().addReview(rating);
            System.out.println("\n✅ Review submitted successfully!");
        } catch (Exception e) { System.out.println("Review submission failed!"); }
    }

    private void viewCustomerReviews(DataManager dataManager) {
        System.out.println("\n========== MY REVIEWS ==========");
        List<Review> customerReviews = new ArrayList<>();
        for (Review r : dataManager.getReviews()) {
            if (r.getCustomer().getCustomerId() == this.customerId) customerReviews.add(r);
        }
        if (customerReviews.isEmpty()) { System.out.println("No reviews found."); return; }
        for (Review r : customerReviews) {
            System.out.println("Review ID: " + r.getReviewId());
            System.out.println("Provider: " + r.getProvider().getBusinessName());
            System.out.println("Rating: " + r.getRating() + "/5");
            System.out.println("Title: " + r.getTitle());
            System.out.println("Comment: " + r.getComment());
            System.out.println("-----------------------------------");
        }
    }

    private void viewCustomerProfile(DataManager dataManager) {
        System.out.println("\n========== MY PROFILE ==========");
        System.out.println("Customer ID: " + customerId);
        System.out.println("Name: " + firstName + " " + lastName);
        System.out.println("Email: " + user.getEmail());
        System.out.println("Phone: " + user.getPhone());
        System.out.println("Address: " + address);
        System.out.println("Member Since: " + joinDate);
        int count = 0;
        for (Service s : dataManager.getServices()) { if (s.getCustomer().getCustomerId() == customerId) count++; }
        System.out.println("Total Bookings: " + count);
    }
}
