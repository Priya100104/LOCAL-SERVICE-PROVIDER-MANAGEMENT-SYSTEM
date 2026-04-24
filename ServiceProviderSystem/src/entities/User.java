package entities;

import system.DataManager;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Scanner;

public class User implements Serializable {
    private static final long serialVersionUID = 1L;

    private int userId;
    private String username;
    private String email;
    private String password;
    private String phone;
    private boolean isActive;
    private LocalDateTime createdAt;

    public User(int userId, String username, String email, String password, String phone) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.password = password;
        this.phone = phone;
        this.isActive = true;
        this.createdAt = LocalDateTime.now();
    }

    // Getters
    public int getUserId()              { return userId; }
    public String getUsername()         { return username; }
    public String getEmail()            { return email; }
    public String getPassword()         { return password; }
    public String getPhone()            { return phone; }
    public boolean isActive()           { return isActive; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    // Setters
    public void setEmail(String email)    { this.email = email; }
    public void setPhone(String phone)    { this.phone = phone; }
    public void setActive(boolean active) { isActive = active; }

    // ===================== MAIN MENU =====================
    public static void showMainMenu(Scanner scanner, DataManager dataManager) {
        while (true) {
            System.out.println("\n========== MAIN MENU ==========");
            System.out.println("1. Login");
            System.out.println("2. Register as Customer");
            System.out.println("3. Register as Service Provider");
            System.out.println("4. View Available Services");
            System.out.println("5. View Top Providers");
            System.out.println("6. Exit System");
            System.out.print("Enter your choice (1-6): ");

            try {
                int choice = Integer.parseInt(scanner.nextLine());
                switch (choice) {
                    case 1: loginUser(scanner, dataManager);       break;
                    case 2: registerCustomer(scanner, dataManager); break;
                    case 3: registerProvider(scanner, dataManager); break;
                    case 4: dataManager.viewAllServiceTypes();      break;
                    case 5: dataManager.viewTopProviders();         break;
                    case 6:
                        System.out.println("\nThank you for using our Service Provider System!");
                        System.out.println("Goodbye!");
                        return;
                    default:
                        System.out.println("Invalid choice! Please enter 1-6.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number!");
            }
        }
    }

    // ===================== LOGIN (BUG FIXED) =====================
    private static void loginUser(Scanner scanner, DataManager dataManager) {
        System.out.println("\n========== LOGIN ==========");
        System.out.print("Username: ");
        String username = scanner.nextLine().trim();
        System.out.print("Password: ");
        String password = scanner.nextLine().trim();

        // --- Step 1: Check Admin (Admin is in users list) ---
        for (User user : dataManager.getUsers()) {
            if (user instanceof Admin admin && admin.getUsername().equals(username)) {
                if (!admin.getPassword().equals(password)) {
                    System.out.println("Login failed! Invalid username or password.");
                    return;
                }
                dataManager.setCurrentUser(admin);
                dataManager.setCurrentAdmin(admin);
                admin.showAdminMenu(scanner, dataManager);
                return;
            }
        }

        // --- Step 2: Check Customers via their OWN User object (not global users list) ---
        // This is the root cause fix: each Customer has its own User object internally.
        // Searching the global users list caused mismatches when passwords were the same.
        for (Customer customer : dataManager.getCustomers()) {
            if (customer.getUser().getUsername().equals(username)) {
                if (!customer.getUser().getPassword().equals(password)) {
                    System.out.println("Login failed! Invalid username or password.");
                    return;
                }
                dataManager.setCurrentUser(customer.getUser());
                dataManager.setCurrentCustomer(customer);
                customer.showCustomerMenu(scanner, dataManager);
                return;
            }
        }

        // --- Step 3: Check Providers via their OWN User object (not global users list) ---
        for (Provider provider : dataManager.getProviders()) {
            if (provider.getUser().getUsername().equals(username)) {
                if (!provider.getUser().getPassword().equals(password)) {
                    System.out.println("Login failed! Invalid username or password.");
                    return;
                }
                dataManager.setCurrentUser(provider.getUser());
                dataManager.setCurrentProvider(provider);
                provider.showProviderMenu(scanner, dataManager);
                return;
            }
        }

        System.out.println("Login failed! Invalid username or password.");
    }

    // ===================== REGISTER CUSTOMER =====================
    private static void registerCustomer(Scanner scanner, DataManager dataManager) {
        System.out.println("\n========== CUSTOMER REGISTRATION ==========");

        try {
            System.out.print("Enter Username: ");
            String username = scanner.nextLine().trim();

            // Check uniqueness across ALL account types, not just users list
            if (isUsernameTaken(username, dataManager)) {
                System.out.println("Username already exists! Try another.");
                return;
            }

            System.out.print("Enter Email: ");
            String email = scanner.nextLine().trim();
            System.out.print("Enter Password: ");
            String password = scanner.nextLine().trim();
            System.out.print("Enter Phone: ");
            String phone = scanner.nextLine().trim();
            System.out.print("Enter First Name: ");
            String firstName = scanner.nextLine().trim();
            System.out.print("Enter Last Name: ");
            String lastName = scanner.nextLine().trim();
            System.out.print("Enter Address: ");
            String address = scanner.nextLine().trim();

            // Safe ID: max existing ID + 1 prevents collision if records are deleted
            int newId = dataManager.getCustomers().stream()
                            .mapToInt(Customer::getCustomerId).max().orElse(0) + 1;

            Customer newCustomer = new Customer(newId, username, email, password,
                                                phone, firstName, lastName, address);
            dataManager.addUser(newCustomer.getUser());
            dataManager.addCustomer(newCustomer);

            System.out.println("\n✅ Registration successful!");
            System.out.println("Customer ID: " + newCustomer.getCustomerId());
            System.out.println("Please login to continue.");

        } catch (Exception e) {
            System.out.println("Registration failed! Please try again.");
        }
    }

    // ===================== REGISTER PROVIDER =====================
    private static void registerProvider(Scanner scanner, DataManager dataManager) {
        System.out.println("\n========== PROVIDER REGISTRATION ==========");
        dataManager.viewAllServiceTypes();

        try {
            System.out.print("Enter Username: ");
            String username = scanner.nextLine().trim();

            // Check uniqueness across ALL account types, not just users list
            if (isUsernameTaken(username, dataManager)) {
                System.out.println("Username already exists! Try another.");
                return;
            }

            System.out.print("Enter Email: ");
            String email = scanner.nextLine().trim();
            System.out.print("Enter Password: ");
            String password = scanner.nextLine().trim();
            System.out.print("Enter Phone: ");
            String phone = scanner.nextLine().trim();
            System.out.print("Enter Business Name: ");
            String businessName = scanner.nextLine().trim();

            System.out.print("Select Service Type ID: ");
            int serviceTypeId = Integer.parseInt(scanner.nextLine().trim());

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

            System.out.print("Years of Experience: ");
            int experience = Integer.parseInt(scanner.nextLine().trim());
            System.out.print("Hourly Rate (₹): ");
            double hourlyRate = Double.parseDouble(scanner.nextLine().trim());

            // Safe ID: max existing ID + 1 prevents collision if records are deleted
            int newId = dataManager.getProviders().stream()
                            .mapToInt(Provider::getProviderId).max().orElse(0) + 1;

            Provider newProvider = new Provider(newId, username, email, password, phone,
                                                businessName, selectedType, experience, 0.0, false, hourlyRate);
            dataManager.addUser(newProvider.getUser());
            dataManager.addProvider(newProvider);

            System.out.println("\n✅ Registration successful!");
            System.out.println("Provider ID: " + newProvider.getProviderId());
            System.out.println("Your account is pending verification by admin.");
            System.out.println("Please login after verification.");

        } catch (Exception e) {
            System.out.println("Registration failed! Please enter valid data.");
        }
    }

    // ===================== HELPER: Username uniqueness check =====================
    // Checks across all 3 account types separately — avoids the global users list issue
    private static boolean isUsernameTaken(String username, DataManager dataManager) {
        for (User user : dataManager.getUsers()) {
            if (user instanceof Admin admin && admin.getUsername().equals(username)) return true;
        }
        for (Customer customer : dataManager.getCustomers()) {
            if (customer.getUser().getUsername().equals(username)) return true;
        }
        for (Provider provider : dataManager.getProviders()) {
            if (provider.getUser().getUsername().equals(username)) return true;
        }
        return false;
    }
}