package entities;

import system.DataManager;
import system.FileManager;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Admin extends User {
    private static final long serialVersionUID = 1L;
    
    public Admin(int userId, String username, String email, String password, String phone) {
        super(userId, username, email, password, phone);
    }
    
    public void showAdminMenu(Scanner scanner, DataManager dataManager) {
        while (dataManager.getCurrentAdmin() != null) {
            System.out.println("\n========== ADMIN DASHBOARD ==========");
            System.out.println("Welcome, Administrator");
            System.out.println("1. Manage Providers (Verify/View)");
            System.out.println("2. View All Users");
            System.out.println("3. View All Services");
            System.out.println("4. View System Statistics");
            System.out.println("5. Manage Service Types");
            System.out.println("6. Export Data to CSV");
            System.out.println("7. Logout");
            System.out.print("Enter your choice (1-7): ");
            
            try {
                int choice = Integer.parseInt(scanner.nextLine());
                
                switch (choice) {
                    case 1:
                        manageProviders(scanner, dataManager);
                        break;
                    case 2:
                        viewAllUsers(dataManager);
                        break;
                    case 3:
                        viewAllServices(dataManager);
                        break;
                    case 4:
                        viewSystemStats(dataManager);
                        break;
                    case 5:
                        manageServiceTypes(scanner, dataManager);
                        break;
                    case 6:
                        exportDataToCSV(dataManager);
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
    
    private void manageProviders(Scanner scanner, DataManager dataManager) {
        System.out.println("\n========== MANAGE PROVIDERS ==========");
        
        System.out.println("1. Verify Pending Providers");
        System.out.println("2. View All Providers");
        System.out.println("3. Back to Admin Menu");
        System.out.print("Select option: ");
        
        try {
            int option = Integer.parseInt(scanner.nextLine());
            
            switch (option) {
                case 1:
                    verifyPendingProviders(scanner, dataManager);
                    break;
                case 2:
                    viewAllProviders(dataManager);
                    break;
                case 3:
                    return;
                default:
                    System.out.println("Invalid option!");
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid input!");
        }
    }
    
    private void verifyPendingProviders(Scanner scanner, DataManager dataManager) {
        List<Provider> pendingProviders = new ArrayList<>();
        for (Provider provider : dataManager.getProviders()) {
            if (!provider.isVerified()) {
                pendingProviders.add(provider);
            }
        }
        
        if (pendingProviders.isEmpty()) {
            System.out.println("No providers pending verification.");
            return;
        }
        
        System.out.println("\nProviders Pending Verification:");
        System.out.println("ID  Business Name          Service        Experience");
        System.out.println("----------------------------------------------------");
        
        for (Provider provider : pendingProviders) {
            System.out.printf("%-3d %-23s %-15s %-11d\n",
                provider.getProviderId(),
                provider.getBusinessName(),
                provider.getServiceType().getServiceName(),
                provider.getYearsOfExperience());
        }
        
        try {
            System.out.print("\nEnter Provider ID to verify (0 to cancel): ");
            int providerId = Integer.parseInt(scanner.nextLine());
            
            if (providerId == 0) return;
            
            for (Provider provider : dataManager.getProviders()) {
                if (provider.getProviderId() == providerId) {
                    provider.setVerified(true);
                    System.out.println("✅ Provider " + provider.getBusinessName() + " verified successfully!");
                    return;
                }
            }
            System.out.println("Provider not found!");
        } catch (NumberFormatException e) {
            System.out.println("Invalid input!");
        }
    }
    
    private void viewAllProviders(DataManager dataManager) {
        System.out.println("\n========== ALL PROVIDERS ==========");
        System.out.println("ID  Business Name          Service        Rating  Verified  Phone");
        System.out.println("-----------------------------------------------------------------");
        
        for (Provider provider : dataManager.getProviders()) {
            System.out.printf("%-3d %-23s %-15s %-7.1f %-9s %-15s\n",
                provider.getProviderId(),
                (provider.getBusinessName().length() > 23 ? provider.getBusinessName().substring(0, 20) + "..." : provider.getBusinessName()),
                provider.getServiceType().getServiceName(),
                provider.getAverageRating(),
                provider.isVerified() ? "Yes" : "No",
                provider.getUser().getPhone());
        }
    }
    
    private void viewAllUsers(DataManager dataManager) {
        System.out.println("\n========== ALL SYSTEM USERS ==========");
        System.out.println("ID  Username            Email                  Type        Status");
        System.out.println("---------------------------------------------------------------");
        
        for (User user : dataManager.getUsers()) {
            String userType = "Unknown";
            if (user instanceof Admin) userType = "Admin";
            
            System.out.printf("%-3d %-20s %-22s %-11s %-8s\n",
                user.getUserId(),
                user.getUsername(),
                (user.getEmail().length() > 22 ? user.getEmail().substring(0, 19) + "..." : user.getEmail()),
                userType,
                user.isActive() ? "Active" : "Inactive");
        }
        
        // Show customers and providers separately
        System.out.println("\n📊 User Statistics:");
        System.out.println("   Total Users: " + dataManager.getUsers().size());
        System.out.println("   Customers: " + dataManager.getCustomers().size());
        System.out.println("   Providers: " + dataManager.getProviders().size());
        System.out.println("   Admins: 1");
    }
    
    private void viewAllServices(DataManager dataManager) {
        System.out.println("\n========== ALL SERVICES ==========");
        System.out.println("ID  Service          Provider              Customer           Status       Date");
        System.out.println("---------------------------------------------------------------------------");
        
        for (Service service : dataManager.getServices()) {
            System.out.printf("%-3d %-15s %-21s %-18s %-12s %-15s\n",
                service.getServiceId(),
                service.getServiceType().getServiceName(),
                (service.getProvider().getBusinessName().length() > 21 ? 
                 service.getProvider().getBusinessName().substring(0, 18) + "..." : 
                 service.getProvider().getBusinessName()),
                (service.getCustomer().getFirstName() + " " + service.getCustomer().getLastName().charAt(0) + "."),
                service.getStatus(),
                service.getScheduledDate().toLocalDate());
        }
        
        System.out.println("\n📊 Service Statistics:");
        System.out.println("Total Services: " + dataManager.getServices().size());
        
        // Count by status
        int requested = 0, inProgress = 0, completed = 0, cancelled = 0;
        for (Service service : dataManager.getServices()) {
            switch (service.getStatus()) {
                case "requested": requested++; break;
                case "in_progress": inProgress++; break;
                case "completed": completed++; break;
                case "cancelled": cancelled++; break;
            }
        }
        
        System.out.println("   Requested: " + requested);
        System.out.println("   In Progress: " + inProgress);
        System.out.println("   Completed: " + completed);
        System.out.println("   Cancelled: " + cancelled);
    }
    
    private void viewSystemStats(DataManager dataManager) {
        System.out.println("\n========== SYSTEM STATISTICS ==========");
        System.out.println("📊 USER STATISTICS");
        System.out.println("   Total Users: " + dataManager.getUsers().size());
        System.out.println("   Customers: " + dataManager.getCustomers().size());
        System.out.println("   Providers: " + dataManager.getProviders().size());
        System.out.println("   Verified Providers: " + getVerifiedProviderCount(dataManager));
        
        System.out.println("\n📊 SERVICE STATISTICS");
        System.out.println("   Total Services: " + dataManager.getServices().size());
        System.out.println("   Completed Services: " + getCompletedServiceCount(dataManager));
        System.out.println("   Total Revenue: ₹" + String.format("%.2f", getTotalRevenue(dataManager)));
        
        System.out.println("\n📊 REVIEW STATISTICS");
        System.out.println("   Total Reviews: " + dataManager.getReviews().size());
        System.out.println("   Average Rating: " + String.format("%.1f", getAverageSystemRating(dataManager)) + "/5");
        
        System.out.println("\n📊 TOP PERFORMERS");
        System.out.println("   Top Rated Provider: " + getTopRatedProvider(dataManager));
        System.out.println("   Most Active Customer: " + getMostActiveCustomer(dataManager));
    }
    
    private void manageServiceTypes(Scanner scanner, DataManager dataManager) {
        System.out.println("\n========== MANAGE SERVICE TYPES ==========");
        dataManager.viewAllServiceTypes();
        
        System.out.println("\n1. Add New Service Type");
        System.out.println("2. Remove Service Type");
        System.out.println("3. Back to Admin Menu");
        System.out.print("Select option: ");
        
        try {
            int option = Integer.parseInt(scanner.nextLine());
            
            switch (option) {
                case 1:
                    addNewServiceType(scanner, dataManager);
                    break;
                case 2:
                    removeServiceType(scanner, dataManager);
                    break;
                case 3:
                    return;
                default:
                    System.out.println("Invalid option!");
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid input!");
        }
    }
    
    private void addNewServiceType(Scanner scanner, DataManager dataManager) {
        System.out.println("\n========== ADD NEW SERVICE TYPE ==========");
        
        try {
            System.out.print("Enter Service Name: ");
            String name = scanner.nextLine();
            System.out.print("Enter Description: ");
            String desc = scanner.nextLine();
            System.out.print("Enter Average Duration (hours): ");
            double duration = Double.parseDouble(scanner.nextLine());
            System.out.print("Enter Price Range (e.g., ₹500-₹3000): ");
            String priceRange = scanner.nextLine();
            
            int newId = dataManager.getServiceTypes().size() + 1;
            ServiceType newType = new ServiceType(newId, name, desc, duration, priceRange);
            dataManager.addServiceType(newType);
            
            System.out.println("✅ New service type added successfully!");
            
        } catch (Exception e) {
            System.out.println("Failed to add service type!");
        }
    }
    
    private void removeServiceType(Scanner scanner, DataManager dataManager) {
        System.out.print("\nEnter Service Type ID to remove (0 to cancel): ");
        
        try {
            int typeId = Integer.parseInt(scanner.nextLine());
            
            if (typeId == 0) return;
            
            // Check if any provider uses this service type
            boolean inUse = false;
            for (Provider provider : dataManager.getProviders()) {
                if (provider.getServiceType().getServiceTypeId() == typeId) {
                    inUse = true;
                    break;
                }
            }
            
            if (inUse) {
                System.out.println("Cannot remove! Service type is in use by providers.");
                return;
            }
            
            ServiceType toRemove = null;
            for (ServiceType type : dataManager.getServiceTypes()) {
                if (type.getServiceTypeId() == typeId) {
                    toRemove = type;
                    break;
                }
            }
            
            if (toRemove != null) {
                dataManager.removeServiceType(toRemove);
                System.out.println("✅ Service type removed successfully!");
            } else {
                System.out.println("Service type not found!");
            }
            
        } catch (NumberFormatException e) {
            System.out.println("Invalid input!");
        }
    }
    
    // Helper methods
    private int getVerifiedProviderCount(DataManager dataManager) {
        int count = 0;
        for (Provider provider : dataManager.getProviders()) {
            if (provider.isVerified()) count++;
        }
        return count;
    }
    
    private int getCompletedServiceCount(DataManager dataManager) {
        int count = 0;
        for (Service service : dataManager.getServices()) {
            if (service.getStatus().equals("completed")) count++;
        }
        return count;
    }
    
    private double getTotalRevenue(DataManager dataManager) {
        double total = 0;
        for (Service service : dataManager.getServices()) {
            if (service.getStatus().equals("completed")) {
                total += service.getFinalPrice();
            }
        }
        return total;
    }
    
    private double getAverageSystemRating(DataManager dataManager) {
        if (dataManager.getReviews().isEmpty()) return 0.0;
        
        double sum = 0;
        for (Review review : dataManager.getReviews()) {
            sum += review.getRating();
        }
        return sum / dataManager.getReviews().size();
    }
    
    private String getTopRatedProvider(DataManager dataManager) {
        if (dataManager.getProviders().isEmpty()) return "None";
        
        Provider topProvider = null;
        double topRating = 0;
        
        for (Provider provider : dataManager.getProviders()) {
            if (provider.isVerified() && provider.getAverageRating() > topRating) {
                topRating = provider.getAverageRating();
                topProvider = provider;
            }
        }
        
        return topProvider != null ? topProvider.getBusinessName() + " (" + String.format("%.1f", topRating) + "/5)" : "None";
    }
    
    private String getMostActiveCustomer(DataManager dataManager) {
        if (dataManager.getCustomers().isEmpty()) return "None";
        
        Customer mostActive = null;
        int maxServices = 0;
        
        for (Customer customer : dataManager.getCustomers()) {
            int count = 0;
            for (Service service : dataManager.getServices()) {
                if (service.getCustomer().getCustomerId() == customer.getCustomerId()) {
                    count++;
                }
            }
            
            if (count > maxServices) {
                maxServices = count;
                mostActive = customer;
            }
        }
        
        return mostActive != null ? mostActive.getFirstName() + " " + mostActive.getLastName() + 
               " (" + maxServices + " services)" : "None";
    }
    
    private void exportDataToCSV(DataManager dataManager) {
        System.out.println("\n========== EXPORT DATA TO CSV ==========");
        System.out.println("This will export all data to CSV files in the 'data' folder:");
        System.out.println("- providers_export.csv");
        System.out.println("- customers_export.csv");
        System.out.println("- services_export.csv");
        
        System.out.print("\nProceed with export? (y/n): ");
        Scanner tempScanner = new Scanner(System.in);
        String confirm = tempScanner.nextLine().trim().toLowerCase();
        
        if (confirm.equals("y") || confirm.equals("yes")) {
            try {
                FileManager.exportProvidersToCSV(dataManager.getProviders(), "providers_export.csv");
                FileManager.exportCustomersToCSV(dataManager.getCustomers(), "customers_export.csv");
                FileManager.exportServicesToCSV(dataManager.getServices(), "services_export.csv");
                
                System.out.println("\n✅ All data exported successfully!");
                System.out.println("📁 Check the 'data' folder for the CSV files.");
            } catch (Exception e) {
                System.out.println("❌ Error during export: " + e.getMessage());
            }
        } else {
            System.out.println("Export cancelled.");
        }
    }
}