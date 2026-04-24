package entities;

import system.DataManager;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Provider implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private int providerId;
    private User user;
    private String businessName;
    private ServiceType serviceType;
    private int yearsOfExperience;
    private double averageRating;
    private boolean isVerified;
    private double hourlyRate;
    private List<Integer> ratings;
    
    public Provider(int providerId, String username, String email, String password, 
                   String phone, String businessName, ServiceType serviceType, 
                   int yearsOfExperience, double averageRating, boolean isVerified, double hourlyRate) {
        this.providerId = providerId;
        this.user = new User(providerId, username, email, password, phone);
        this.businessName = businessName;
        this.serviceType = serviceType;
        this.yearsOfExperience = yearsOfExperience;
        this.averageRating = averageRating;
        this.isVerified = isVerified;
        this.hourlyRate = hourlyRate;
        this.ratings = new ArrayList<>();
    }
    
    public int getProviderId() { return providerId; }
    public User getUser() { return user; }
    public String getBusinessName() { return businessName; }
    public ServiceType getServiceType() { return serviceType; }
    public int getYearsOfExperience() { return yearsOfExperience; }
    public double getAverageRating() { return averageRating; }
    public boolean isVerified() { return isVerified; }
    public double getHourlyRate() { return hourlyRate; }
    public List<Integer> getRatings() { return ratings; }
    
    public void setBusinessName(String businessName) { this.businessName = businessName; }
    public void setServiceType(ServiceType serviceType) { this.serviceType = serviceType; }
    public void setYearsOfExperience(int yearsOfExperience) { this.yearsOfExperience = yearsOfExperience; }
    public void setVerified(boolean verified) { isVerified = verified; }
    public void setHourlyRate(double hourlyRate) { this.hourlyRate = hourlyRate; }
    
    public void addReview(int rating) {
        ratings.add(rating);
        updateAverageRating();
    }
    
    private void updateAverageRating() {
        if (ratings.isEmpty()) return;
        double sum = 0;
        for (int rating : ratings) {
            sum += rating;
        }
        averageRating = sum / ratings.size();
    }
    
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
                    case 1:
                        viewProviderServices(dataManager);
                        break;
                    case 2:
                        updateServiceStatus(scanner, dataManager);
                        break;
                    case 3:
                        viewProviderReviews(dataManager);
                        break;
                    case 4:
                        updateProviderProfile(scanner, dataManager);
                        break;
                    case 5:
                        viewProviderEarnings(dataManager);
                        break;
                    case 6:
                        dataManager.logout();
                        return;
                    default:
                        System.out.println("Invalid choice! Please enter 1-6.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number!");
            }
        }
    }
    
    private void viewProviderServices(DataManager dataManager) {
        System.out.println("\n========== MY SERVICES ==========");
        
        List<Service> providerServices = new ArrayList<>();
        for (Service service : dataManager.getServices()) {
            if (service.getProvider().getProviderId() == this.providerId) {
                providerServices.add(service);
            }
        }
        
        if (providerServices.isEmpty()) {
            System.out.println("No services found.");
            return;
        }
        
        System.out.println("ID  Service          Customer           Date            Status       Price");
        System.out.println("--------------------------------------------------------------------------");
        
        for (Service service : providerServices) {
            System.out.printf("%-3d %-15s %-18s %-15s %-12s ?%-9.2f\n",
                service.getServiceId(),
                service.getServiceType().getServiceName(),
                (service.getCustomer().getFirstName() + " " + service.getCustomer().getLastName().charAt(0) + "."),
                service.getScheduledDate().toLocalDate(),
                service.getStatus(),
                service.getFinalPrice());
        }
        
        try (Scanner scanner = new Scanner(System.in)) {
            System.out.print("\nEnter Service ID for details (0 to go back): ");
            try {
                int serviceId = Integer.parseInt(scanner.nextLine());
                if (serviceId == 0) return;
                
                for (Service service : providerServices) {
                    if (service.getServiceId() == serviceId) {
                        viewServiceDetails(service);
                        return;
                    }
                }
                System.out.println("Service not found!");
            } catch (NumberFormatException e) {
                System.out.println("Invalid input!");
            }
        }
    }
    
    private void viewServiceDetails(Service service) {
        System.out.println("\n========== SERVICE DETAILS ==========");
        System.out.println("Service ID: " + service.getServiceId());
        System.out.println("Type: " + service.getServiceType().getServiceName());
        System.out.println("Customer: " + service.getCustomer().getFirstName() + " " + service.getCustomer().getLastName());
        System.out.println("Description: " + service.getDescription());
        System.out.println("Scheduled Date: " + service.getScheduledDate());
        System.out.println("Address: " + service.getAddress());
        System.out.println("Status: " + service.getStatus());
        System.out.println("Price: ?" + service.getFinalPrice());
    }
    
    private void updateServiceStatus(Scanner scanner, DataManager dataManager) {
        System.out.println("\n========== UPDATE SERVICE STATUS ==========");
        
        List<Service> providerServices = new ArrayList<>();
        for (Service service : dataManager.getServices()) {
            if (service.getProvider().getProviderId() == this.providerId) {
                providerServices.add(service);
            }
        }
        
        if (providerServices.isEmpty()) {
            System.out.println("No services to update.");
            return;
        }
        
        System.out.println("Your Services:");
        for (Service service : providerServices) {
            System.out.println(service.getServiceId() + ". " + service.getServiceType().getServiceName() + " - Status: " + service.getStatus());
        }
        
        System.out.print("Enter Service ID: ");
        int serviceId = Integer.parseInt(scanner.nextLine());
        
        for (Service service : providerServices) {
            if (service.getServiceId() == serviceId) {
                System.out.println("Current Status: " + service.getStatus());
                System.out.println("1. Accepted");
                System.out.println("2. In Progress");
                System.out.println("3. Completed");
                System.out.print("Enter new status (1-3): ");
                
                int choice = Integer.parseInt(scanner.nextLine());
                String newStatus = switch (choice) {
                    case 1 -> "Accepted";
                    case 2 -> "In Progress";
                    case 3 -> "Completed";
                    default -> service.getStatus();
                };
                
                service.setStatus(newStatus);
                System.out.println("? Status updated to: " + newStatus);
                return;
            }
        }
        System.out.println("Service not found!");
    }
    
    private void viewProviderReviews(DataManager dataManager) {
        System.out.println("\n========== MY REVIEWS ==========");
        
        List<Review> providerReviews = new ArrayList<>();
        for (Review review : dataManager.getReviews()) {
            if (review.getProvider().getProviderId() == this.providerId) {
                providerReviews.add(review);
            }
        }
        
        if (providerReviews.isEmpty()) {
            System.out.println("No reviews yet.");
            System.out.println("Average Rating: N/A");
            return;
        }
        
        System.out.println("Total Reviews: " + providerReviews.size());
        System.out.println("Average Rating: ? " + String.format("%.1f", averageRating));
        System.out.println("\nReviews:");
        System.out.println("-------------------------------------------");
        
        for (Review review : providerReviews) {
            System.out.println("Rating: ? " + review.getRating());
            System.out.println("Customer: " + review.getCustomer().getFirstName() + " " + review.getCustomer().getLastName());
            System.out.println("Comment: " + review.getComment());
            System.out.println("-------------------------------------------");
        }
    }
    
    private void updateProviderProfile(Scanner scanner, DataManager dataManager) {
        System.out.println("\n========== UPDATE PROFILE ==========");
        System.out.println("Current Information:");
        System.out.println("Business Name: " + businessName);
        System.out.println("Service Type: " + serviceType.getServiceName());
        System.out.println("Years of Experience: " + yearsOfExperience);
        System.out.println("Hourly Rate: ?" + hourlyRate);
        
        System.out.print("\nEnter new Business Name (or press Enter to skip): ");
        String newName = scanner.nextLine();
        if (!newName.isBlank()) {
            this.businessName = newName;
        }
        
        System.out.print("Enter new Hourly Rate (or press Enter to skip): ");
        String newRate = scanner.nextLine();
        if (!newRate.isBlank()) {
            try {
                this.hourlyRate = Double.parseDouble(newRate);
            } catch (NumberFormatException e) {
                System.out.println("Invalid rate!");
            }
        }
        
        System.out.println("? Profile updated!");
    }
    
    private void viewProviderEarnings(DataManager dataManager) {
        System.out.println("\n========== MY EARNINGS ==========");
        
        double totalEarnings = 0;
        int completedServices = 0;
        
        for (Service service : dataManager.getServices()) {
            if (service.getProvider().getProviderId() == this.providerId && 
                "Completed".equals(service.getStatus())) {
                totalEarnings += service.getFinalPrice();
                completedServices++;
            }
        }
        
        System.out.println("Total Earnings: ?" + String.format("%.2f", totalEarnings));
        System.out.println("Completed Services: " + completedServices);
        
        if (completedServices > 0) {
            System.out.println("Average Earning per Service: ?" + String.format("%.2f", totalEarnings / completedServices));
        }
    }
}
