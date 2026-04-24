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
    
    private int customerId;
    private User user;
    private String firstName;
    private String lastName;
    private String address;
    private LocalDate joinDate;
    
    public Customer(int customerId, String username, String email, String password, 
                   String phone, String firstName, String lastName, String address) {
        this.customerId = customerId;
        this.user = new User(customerId, username, email, password, phone);
        this.firstName = firstName;
        this.lastName = lastName;
        this.address = address;
        this.joinDate = LocalDate.now();
    }
    
    // Getters
    public int getCustomerId() { return customerId; }
    public User getUser() { return user; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getAddress() { return address; }
    public LocalDate getJoinDate() { return joinDate; }
    
    // Setters
    public void setAddress(String address) { this.address = address; }
    
    // Menu methods
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
                    case 1:
                        bookNewService(scanner, dataManager);
                        break;
                    case 2:
                        viewCustomerBookings(scanner, dataManager);
                        break;
                    case 3:
                        viewAllProviders(dataManager);
                        break;
                    case 4:
                        writeReview(scanner, dataManager);
                        break;
                    case 5:
                        viewCustomerReviews(dataManager);
                        break;
                    case 6:
                        viewCustomerProfile(dataManager);
                        break;
                    case 7:
                        dataManager.logout();
                        return;
                    default:
                        System.out.println("Invalid choice! Please enter 1-7.");
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
            for (ServiceType type : dataManager.getServiceTypes()) {
                if (type.getServiceTypeId() == serviceTypeId) {
                    selectedType = type;
                    break;
                }
            }
            
            if (selectedType == null) {
                System.out.println("Invalid service type!");
                return;
            }
            
            // Show providers for this service
            List<Provider> availableProviders = new ArrayList<>();
            for (Provider provider : dataManager.getProviders()) {
                if (provider.getServiceType().getServiceTypeId() == serviceTypeId && 
                    provider.isVerified()) {
                    availableProviders.add(provider);
                }
            }
            
            if (availableProviders.isEmpty()) {
                System.out.println("No providers available for this service.");
                return;
            }
            
            System.out.println("\nAvailable Providers:");
            System.out.println("ID  Business Name          Rating  Experience  Hourly Rate");
            System.out.println("---------------------------------------------------------");
            
            for (Provider provider : availableProviders) {
                System.out.printf("%-3d %-23s %-7.1f %-11d ₹%-9.0f\n",
                    provider.getProviderId(),
                    (provider.getBusinessName().length() > 23 ? provider.getBusinessName().substring(0, 20) + "..." : provider.getBusinessName()),
                    provider.getAverageRating(),
                    provider.getYearsOfExperience(),
                    provider.getHourlyRate());
            }
            
            System.out.print("\nSelect Provider ID: ");
            int providerId = Integer.parseInt(scanner.nextLine());
            
            Provider selectedProvider = null;
            for (Provider provider : availableProviders) {
                if (provider.getProviderId() == providerId) {
                    selectedProvider = provider;
                    break;
                }
            }
            
            if (selectedProvider == null) {
                System.out.println("Invalid provider selection!");
                return;
            }
            
            System.out.print("Enter Service Description: ");
            String description = scanner.nextLine();
            System.out.print("Enter Service Address: ");
            String address = scanner.nextLine();
            System.out.print("Enter Preferred Date (YYYY-MM-DD): ");
            String dateStr = scanner.nextLine();
            System.out.print("Enter Preferred Time (HH:MM): ");
            String timeStr = scanner.nextLine();
            
            LocalDateTime scheduledDate = LocalDateTime.parse(dateStr + "T" + timeStr + ":00");
            
            int newServiceId = dataManager.getServices().size() + 1;
            Service newService = new Service(newServiceId, selectedProvider, this,
                                            selectedType, description, address, scheduledDate);
            
            dataManager.addService(newService);
            
            // Create appointment
            int newAppointmentId = dataManager.getAppointments().size() + 1;
            Appointment newAppointment = new Appointment(newAppointmentId, newService, scheduledDate);
            dataManager.addAppointment(newAppointment);
            
            System.out.println("\n✅ Service booked successfully!");
            System.out.println("Service ID: " + newServiceId);
            System.out.println("Appointment scheduled for: " + scheduledDate);
            System.out.println("Provider will contact you soon.");
            
        } catch (Exception e) {
            System.out.println("Booking failed! Please check your inputs.");
        }
    }
    
    private void viewCustomerBookings(Scanner scanner, DataManager dataManager) {
        System.out.println("\n========== MY BOOKINGS ==========");
        
        List<Service> customerServices = new ArrayList<>();
        for (Service service : dataManager.getServices()) {
            if (service.getCustomer().getCustomerId() == this.customerId) {
                customerServices.add(service);
            }
        }
        
        if (customerServices.isEmpty()) {
            System.out.println("No bookings found.");
            return;
        }
        
        System.out.println("ID  Service          Provider              Date            Status       Price");
        System.out.println("---------------------------------------------------------------------------");
        
        for (Service service : customerServices) {
            System.out.printf("%-3d %-15s %-21s %-15s %-12s ₹%-9.2f\n",
                service.getServiceId(),
                service.getServiceType().getServiceName(),
                (service.getProvider().getBusinessName().length() > 21 ? 
                 service.getProvider().getBusinessName().substring(0, 18) + "..." : 
                 service.getProvider().getBusinessName()),
                service.getScheduledDate().toLocalDate(),
                service.getStatus(),
                service.getFinalPrice());
        }
        
        System.out.print("\nEnter Service ID for details (0 to go back): ");
        try {
            int serviceId = Integer.parseInt(scanner.nextLine());
            if (serviceId == 0) return;
            
            for (Service service : customerServices) {
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
    
    private void viewServiceDetails(Service service) {
        System.out.println("\n========== SERVICE DETAILS ==========");
        System.out.println("Service ID: " + service.getServiceId());
        System.out.println("Service Type: " + service.getServiceType().getServiceName());
        System.out.println("Provider: " + service.getProvider().getBusinessName());
        System.out.println("Description: " + service.getDescription());
        System.out.println("Address: " + service.getAddress());
        System.out.println("Scheduled: " + service.getScheduledDate());
        System.out.println("Status: " + service.getStatus());
        System.out.println("Quoted Price: ₹" + service.getQuotedPrice());
        System.out.println("Final Price: ₹" + service.getFinalPrice());
        
        if (service.getStatus().equals("completed")) {
            System.out.println("\nThis service has been completed.");
            System.out.println("You can write a review for this service.");
        }
    }
    
    private void viewAllProviders(DataManager dataManager) {
        System.out.println("\n========== ALL PROVIDERS ==========");
        System.out.println("ID  Business Name          Service        Rating  Experience  Verified");
        System.out.println("---------------------------------------------------------------------");
        
        for (Provider provider : dataManager.getProviders()) {
            if (provider.isVerified()) {
                System.out.printf("%-3d %-23s %-15s %-7.1f %-11d %-8s\n",
                    provider.getProviderId(),
                    (provider.getBusinessName().length() > 23 ? provider.getBusinessName().substring(0, 20) + "..." : provider.getBusinessName()),
                    provider.getServiceType().getServiceName(),
                    provider.getAverageRating(),
                    provider.getYearsOfExperience(),
                    provider.isVerified() ? "Yes" : "No");
            }
        }
    }
    
    private void writeReview(Scanner scanner, DataManager dataManager) {
        System.out.println("\n========== WRITE A REVIEW ==========");
        
        // Get completed services by this customer
        List<Service> completedServices = new ArrayList<>();
        for (Service service : dataManager.getServices()) {
            if (service.getCustomer().getCustomerId() == this.customerId &&
                service.getStatus().equals("completed")) {
                completedServices.add(service);
            }
        }
        
        if (completedServices.isEmpty()) {
            System.out.println("No completed services to review.");
            return;
        }
        
        System.out.println("Completed Services:");
        System.out.println("ID  Service          Provider              Date");
        System.out.println("-----------------------------------------------");
        
        for (Service service : completedServices) {
            System.out.printf("%-3d %-15s %-21s %-15s\n",
                service.getServiceId(),
                service.getServiceType().getServiceName(),
                (service.getProvider().getBusinessName().length() > 21 ? 
                 service.getProvider().getBusinessName().substring(0, 18) + "..." : 
                 service.getProvider().getBusinessName()),
                service.getScheduledDate().toLocalDate());
        }
        
        try {
            System.out.print("\nSelect Service ID to review: ");
            int serviceId = Integer.parseInt(scanner.nextLine());
            
            Service selectedService = null;
            for (Service service : completedServices) {
                if (service.getServiceId() == serviceId) {
                    selectedService = service;
                    break;
                }
            }
            
            if (selectedService == null) {
                System.out.println("Service not found!");
                return;
            }
            
            // Check if already reviewed
            for (Review review : dataManager.getReviews()) {
                if (review.getService().getServiceId() == serviceId &&
                    review.getCustomer().getCustomerId() == this.customerId) {
                    System.out.println("You have already reviewed this service!");
                    return;
                }
            }
            
            System.out.print("Enter Rating (1-5): ");
            int rating = Integer.parseInt(scanner.nextLine());
            
            if (rating < 1 || rating > 5) {
                System.out.println("Rating must be between 1 and 5!");
                return;
            }
            
            System.out.print("Enter Review Title: ");
            String title = scanner.nextLine();
            System.out.print("Enter Review Comments: ");
            String comment = scanner.nextLine();
            
            int newReviewId = dataManager.getReviews().size() + 1;
            Review newReview = new Review(newReviewId, this, selectedService.getProvider(),
                                         selectedService, rating, title, comment);
            
            dataManager.addReview(newReview);
            selectedService.getProvider().addReview(rating);
            
            System.out.println("\n✅ Review submitted successfully!");
            System.out.println("Thank you for your feedback!");
            
        } catch (Exception e) {
            System.out.println("Review submission failed!");
        }
    }
    
    private void viewCustomerReviews(DataManager dataManager) {
        System.out.println("\n========== MY REVIEWS ==========");
        
        List<Review> customerReviews = new ArrayList<>();
        for (Review review : dataManager.getReviews()) {
            if (review.getCustomer().getCustomerId() == this.customerId) {
                customerReviews.add(review);
            }
        }
        
        if (customerReviews.isEmpty()) {
            System.out.println("No reviews found.");
            return;
        }
        
        for (Review review : customerReviews) {
            System.out.println("Review ID: " + review.getReviewId());
            System.out.println("Provider: " + review.getProvider().getBusinessName());
            System.out.println("Service: " + review.getService().getServiceType().getServiceName());
            System.out.println("Rating: " + review.getRating() + "/5");
            System.out.println("Title: " + review.getTitle());
            System.out.println("Comment: " + review.getComment());
            System.out.println("Date: " + review.getReviewDate());
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
        System.out.println("Total Bookings: " + getCustomerBookingCount(dataManager));
    }
    
    private int getCustomerBookingCount(DataManager dataManager) {
        int count = 0;
        for (Service service : dataManager.getServices()) {
            if (service.getCustomer().getCustomerId() == this.customerId) {
                count++;
            }
        }
        return count;
    }
}