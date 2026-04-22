package entities;

import system.DataManager;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Provider implements Serializable {

    private static final long serialVersionUID = 1L;

    private int         providerId;
    private User        user;
    private String      businessName;
    private ServiceType serviceType;
    private int         yearsOfExperience;
    private double      averageRating;
    private boolean     isVerified;
    private double      hourlyRate;
    private List<Integer> ratings;

    public Provider(int providerId, String username, String email, String password,
                    String phone, String businessName, ServiceType serviceType,
                    int yearsOfExperience, double averageRating, boolean isVerified, double hourlyRate) {
        this.providerId        = providerId;
        this.user              = new User(providerId, username, email, password, phone);
        this.businessName      = businessName;
        this.serviceType       = serviceType;
        this.yearsOfExperience = yearsOfExperience;
        this.averageRating     = averageRating;
        this.isVerified        = isVerified;
        this.hourlyRate        = hourlyRate;
        this.ratings           = new ArrayList<>();
    }

    public int          getProviderId()       { return providerId; }
    public User         getUser()             { return user; }
    public String       getBusinessName()     { return businessName; }
    public ServiceType  getServiceType()      { return serviceType; }
    public int          getYearsOfExperience(){ return yearsOfExperience; }
    public double       getAverageRating()    { return averageRating; }
    public boolean      isVerified()          { return isVerified; }
    public double       getHourlyRate()       { return hourlyRate; }
    public List<Integer>getRatings()          { return ratings; }

    public void setBusinessName(String n)     { this.businessName      = n; }
    public void setServiceType(ServiceType t) { this.serviceType       = t; }
    public void setYearsOfExperience(int y)   { this.yearsOfExperience = y; }
    public void setVerified(boolean v)         { isVerified             = v; }
    public void setHourlyRate(double r)        { this.hourlyRate        = r; }

    public void addReview(int rating) {
        ratings.add(rating);
        updateAverageRating();
    }

    private void updateAverageRating() {
        if (ratings.isEmpty()) return;
        double sum = 0;
        for (int r : ratings) sum += r;
        averageRating = sum / ratings.size();
    }

    // ===================== PROVIDER MENU =====================
    public void showProviderMenu(Scanner scanner, DataManager dataManager) {
        while (dataManager.getCurrentProvider() != null) {
            System.out.println("\n========== PROVIDER DASHBOARD ==========");
            System.out.println("Welcome, " + businessName);
            System.out.println("1. View My Services");
            System.out.println("2. Update Service Status");
            System.out.println("3. View My Reviews");
            System.out.println("4. Update My Profile");
            System.out.println("5. View Earnings");
            System.out.println("6. Logout");
            System.out.print("Enter your choice (1-6): ");

            try {
                int choice = Integer.parseInt(scanner.nextLine());
                switch (choice) {
                    case 1 -> viewProviderServices(dataManager);
                    case 2 -> updateServiceStatus(scanner, dataManager);
                    case 3 -> viewProviderReviews(dataManager);
                    case 4 -> updateProviderProfile(scanner, dataManager);
                    case 5 -> viewProviderEarnings(dataManager);
                    case 6 -> { dataManager.logout(); return; }
                    default -> System.out.println("Invalid choice! Please enter 1-6.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number!");
            }
        }
    }

    private void viewProviderServices(DataManager dataManager) {
        System.out.println("\n========== MY SERVICES ==========");
        List<Service> providerServices = new ArrayList<>();
        for (Service s : dataManager.getServices()) {
            if (s.getProvider().getProviderId() == this.providerId) providerServices.add(s);
        }
        if (providerServices.isEmpty()) { System.out.println("No services found."); return; }

        System.out.println("ID  Service          Customer           Date            Status       Price");
        System.out.println("--------------------------------------------------------------------------");
        for (Service s : providerServices) {
            System.out.printf("%-3d %-15s %-18s %-15s %-12s ₹%-9.2f%n",
                s.getServiceId(), s.getServiceType().getServiceName(),
                (s.getCustomer().getFirstName() + " " + s.getCustomer().getLastName().charAt(0) + "."),
                s.getScheduledDate().toLocalDate(), s.getStatus(), s.getFinalPrice());
        }
    }

    private void updateServiceStatus(Scanner scanner, DataManager dataManager) {
        System.out.println("\n========== UPDATE SERVICE STATUS ==========");
        List<Service> providerServices = new ArrayList<>();
        for (Service s : dataManager.getServices()) {
            if (s.getProvider().getProviderId() == this.providerId) providerServices.add(s);
        }
        if (providerServices.isEmpty()) { System.out.println("No services to update."); return; }

        for (Service s : providerServices) {
            System.out.println(s.getServiceId() + ". " + s.getServiceType().getServiceName() + " - " + s.getStatus());
        }
        System.out.print("Enter Service ID: ");
        try {
            int serviceId = Integer.parseInt(scanner.nextLine());
            for (Service s : providerServices) {
                if (s.getServiceId() == serviceId) {
                    System.out.println("1. Accepted  2. In Progress  3. Completed");
                    System.out.print("Enter new status (1-3): ");
                    int choice = Integer.parseInt(scanner.nextLine());
                    String newStatus = switch (choice) {
                        case 1 -> "Accepted";
                        case 2 -> "In Progress";
                        case 3 -> "Completed";
                        default -> s.getStatus();
                    };
                    s.setStatus(newStatus);
                    System.out.println("✅ Status updated to: " + newStatus);
                    return;
                }
            }
            System.out.println("Service not found!");
        } catch (NumberFormatException e) { System.out.println("Invalid input!"); }
    }

    private void viewProviderReviews(DataManager dataManager) {
        System.out.println("\n========== MY REVIEWS ==========");
        List<Review> providerReviews = new ArrayList<>();
        for (Review r : dataManager.getReviews()) {
            if (r.getProvider().getProviderId() == this.providerId) providerReviews.add(r);
        }
        if (providerReviews.isEmpty()) { System.out.println("No reviews yet."); return; }
        System.out.println("Total Reviews: " + providerReviews.size());
        System.out.println("Average Rating: ⭐ " + String.format("%.1f", averageRating));
        System.out.println("-------------------------------------------");
        for (Review r : providerReviews) {
            System.out.println("Rating: ⭐ " + r.getRating());
            System.out.println("Customer: " + r.getCustomer().getFirstName() + " " + r.getCustomer().getLastName());
            System.out.println("Comment: " + r.getComment());
            System.out.println("-------------------------------------------");
        }
    }

    private void updateProviderProfile(Scanner scanner, DataManager dataManager) {
        System.out.println("\n========== UPDATE PROFILE ==========");
        System.out.println("Business Name: " + businessName);
        System.out.println("Hourly Rate: ₹" + hourlyRate);
        System.out.print("\nEnter new Business Name (or press Enter to skip): ");
        String newName = scanner.nextLine();
        if (!newName.isBlank()) this.businessName = newName;
        System.out.print("Enter new Hourly Rate (or press Enter to skip): ");
        String newRate = scanner.nextLine();
        if (!newRate.isBlank()) {
            try { this.hourlyRate = Double.parseDouble(newRate); }
            catch (NumberFormatException e) { System.out.println("Invalid rate!"); }
        }
        System.out.println("✅ Profile updated!");
    }

    private void viewProviderEarnings(DataManager dataManager) {
        System.out.println("\n========== MY EARNINGS ==========");
        double total = 0; int count = 0;
        for (Service s : dataManager.getServices()) {
            if (s.getProvider().getProviderId() == this.providerId && "Completed".equalsIgnoreCase(s.getStatus())) {
                total += s.getFinalPrice(); count++;
            }
        }
        System.out.println("Total Earnings: ₹" + String.format("%.2f", total));
        System.out.println("Completed Services: " + count);
        if (count > 0) System.out.println("Avg per Service: ₹" + String.format("%.2f", total / count));
    }
}
