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
                    case 1 -> manageProviders(scanner, dataManager);
                    case 2 -> viewAllUsers(dataManager);
                    case 3 -> viewAllServices(dataManager);
                    case 4 -> viewSystemStats(dataManager);
                    case 5 -> manageServiceTypes(scanner, dataManager);
                    case 6 -> exportDataToCSV(dataManager, scanner);
                    case 7 -> { dataManager.logout(); return; }
                    default -> System.out.println("Invalid choice! Please enter 1-7.");
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
        System.out.println("3. Back");
        System.out.print("Select option: ");
        try {
            int option = Integer.parseInt(scanner.nextLine());
            switch (option) {
                case 1 -> verifyPendingProviders(scanner, dataManager);
                case 2 -> viewAllProviders(dataManager);
            }
        } catch (NumberFormatException e) { System.out.println("Invalid input!"); }
    }

    private void verifyPendingProviders(Scanner scanner, DataManager dataManager) {
        List<Provider> pending = new ArrayList<>();
        for (Provider p : dataManager.getProviders()) { if (!p.isVerified()) pending.add(p); }
        if (pending.isEmpty()) { System.out.println("No providers pending verification."); return; }

        System.out.println("\nProviders Pending Verification:");
        System.out.println("ID  Business Name          Service        Experience");
        System.out.println("----------------------------------------------------");
        for (Provider p : pending) {
            System.out.printf("%-3d %-23s %-15s %-11d%n",
                p.getProviderId(), p.getBusinessName(),
                p.getServiceType().getServiceName(), p.getYearsOfExperience());
        }
        System.out.print("\nEnter Provider ID to verify (0 to cancel): ");
        try {
            int providerId = Integer.parseInt(scanner.nextLine());
            if (providerId == 0) return;
            for (Provider p : dataManager.getProviders()) {
                if (p.getProviderId() == providerId) {
                    p.setVerified(true);
                    System.out.println("✅ Provider " + p.getBusinessName() + " verified successfully!");
                    return;
                }
            }
            System.out.println("Provider not found!");
        } catch (NumberFormatException e) { System.out.println("Invalid input!"); }
    }

    private void viewAllProviders(DataManager dataManager) {
        System.out.println("\n========== ALL PROVIDERS ==========");
        System.out.println("ID  Business Name          Service        Rating  Verified  Phone");
        System.out.println("-----------------------------------------------------------------");
        for (Provider p : dataManager.getProviders()) {
            System.out.printf("%-3d %-23s %-15s %-7.1f %-9s %-15s%n",
                p.getProviderId(),
                (p.getBusinessName().length() > 23 ? p.getBusinessName().substring(0, 20) + "..." : p.getBusinessName()),
                p.getServiceType().getServiceName(),
                p.getAverageRating(), p.isVerified() ? "Yes" : "No", p.getUser().getPhone());
        }
    }

    private void viewAllUsers(DataManager dataManager) {
        System.out.println("\n========== ALL SYSTEM USERS ==========");
        System.out.println("ID  Username            Email                  Type        Status");
        System.out.println("---------------------------------------------------------------");
        for (User user : dataManager.getUsers()) {
            String userType = (user instanceof Admin) ? "Admin" : "User";
            System.out.printf("%-3d %-20s %-22s %-11s %-8s%n",
                user.getUserId(), user.getUsername(),
                (user.getEmail().length() > 22 ? user.getEmail().substring(0, 19) + "..." : user.getEmail()),
                userType, user.isActive() ? "Active" : "Inactive");
        }
        System.out.println("\n📊 User Statistics:");
        System.out.println("   Total Users: " + dataManager.getUsers().size());
        System.out.println("   Customers: " + dataManager.getCustomers().size());
        System.out.println("   Providers: " + dataManager.getProviders().size());
    }

    private void viewAllServices(DataManager dataManager) {
        System.out.println("\n========== ALL SERVICES ==========");
        System.out.println("ID  Service          Provider              Customer           Status       Date");
        System.out.println("---------------------------------------------------------------------------");
        for (Service s : dataManager.getServices()) {
            System.out.printf("%-3d %-15s %-21s %-18s %-12s %-15s%n",
                s.getServiceId(), s.getServiceType().getServiceName(),
                (s.getProvider().getBusinessName().length() > 21 ? s.getProvider().getBusinessName().substring(0, 18) + "..." : s.getProvider().getBusinessName()),
                (s.getCustomer().getFirstName() + " " + s.getCustomer().getLastName().charAt(0) + "."),
                s.getStatus(), s.getScheduledDate().toLocalDate());
        }
        System.out.println("\n📊 Service Statistics:");
        System.out.println("Total Services: " + dataManager.getServices().size());
    }

    private void viewSystemStats(DataManager dataManager) {
        System.out.println("\n========== SYSTEM STATISTICS ==========");
        System.out.println("📊 USER STATISTICS");
        System.out.println("   Total Users: " + dataManager.getUsers().size());
        System.out.println("   Customers: " + dataManager.getCustomers().size());
        System.out.println("   Providers: " + dataManager.getProviders().size());

        System.out.println("\n📊 SERVICE STATISTICS");
        System.out.println("   Total Services: " + dataManager.getServices().size());
        double revenue = 0;
        int completed = 0;
        for (Service s : dataManager.getServices()) {
            if ("Completed".equalsIgnoreCase(s.getStatus())) { revenue += s.getFinalPrice(); completed++; }
        }
        System.out.println("   Completed: " + completed);
        System.out.println("   Total Revenue: ₹" + String.format("%.2f", revenue));

        System.out.println("\n📊 REVIEW STATISTICS");
        System.out.println("   Total Reviews: " + dataManager.getReviews().size());
        double ratingSum = 0;
        for (Review r : dataManager.getReviews()) ratingSum += r.getRating();
        double avg = dataManager.getReviews().isEmpty() ? 0 : ratingSum / dataManager.getReviews().size();
        System.out.println("   Average Rating: " + String.format("%.1f", avg) + "/5");
    }

    private void manageServiceTypes(Scanner scanner, DataManager dataManager) {
        System.out.println("\n========== MANAGE SERVICE TYPES ==========");
        dataManager.viewAllServiceTypes();
        System.out.println("\n1. Add New Service Type\n2. Remove Service Type\n3. Back");
        System.out.print("Select option: ");
        try {
            int option = Integer.parseInt(scanner.nextLine());
            switch (option) {
                case 1 -> {
                    System.out.print("Enter Service Name: "); String name = scanner.nextLine();
                    System.out.print("Enter Description: "); String desc = scanner.nextLine();
                    System.out.print("Enter Avg Duration (hours): "); double dur = Double.parseDouble(scanner.nextLine());
                    System.out.print("Enter Price Range: "); String price = scanner.nextLine();
                    int newId = dataManager.getServiceTypes().size() + 1;
                    dataManager.addServiceType(new ServiceType(newId, name, desc, dur, price));
                    System.out.println("✅ New service type added!");
                }
                case 2 -> {
                    System.out.print("Enter Service Type ID to remove (0 to cancel): ");
                    int typeId = Integer.parseInt(scanner.nextLine());
                    if (typeId == 0) return;
                    boolean inUse = false;
                    for (Provider p : dataManager.getProviders()) {
                        if (p.getServiceType().getServiceTypeId() == typeId) { inUse = true; break; }
                    }
                    if (inUse) { System.out.println("Cannot remove! Service type is in use."); return; }
                    ServiceType toRemove = null;
                    for (ServiceType t : dataManager.getServiceTypes()) { if (t.getServiceTypeId() == typeId) { toRemove = t; break; } }
                    if (toRemove != null) { dataManager.removeServiceType(toRemove); System.out.println("✅ Removed!"); }
                    else System.out.println("Not found!");
                }
            }
        } catch (Exception e) { System.out.println("Invalid input!"); }
    }

    private void exportDataToCSV(DataManager dataManager, Scanner scanner) {
        System.out.println("\n========== EXPORT DATA TO CSV ==========");
        System.out.print("Proceed with export? (y/n): ");
        String confirm = scanner.nextLine().trim().toLowerCase();
        if (confirm.equals("y")) {
            try {
                FileManager.exportProvidersToCSV(dataManager.getProviders(), "providers_export.csv");
                FileManager.exportCustomersToCSV(dataManager.getCustomers(), "customers_export.csv");
                FileManager.exportServicesToCSV(dataManager.getServices(), "services_export.csv");
                System.out.println("\n✅ All data exported to 'data' folder.");
            } catch (Exception e) { System.out.println("❌ Error: " + e.getMessage()); }
        } else { System.out.println("Export cancelled."); }
    }
}
