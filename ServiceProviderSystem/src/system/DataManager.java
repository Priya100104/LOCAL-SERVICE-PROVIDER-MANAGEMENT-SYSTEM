package system;

import entities.*;
import java.util.*;
import java.util.stream.Collectors;

public class DataManager {
    // Using Collection Framework - Map for faster lookups
    private Map<Integer, User> usersMap;
    private Map<Integer, Customer> customersMap;
    private Map<Integer, Provider> providersMap;
    private Map<Integer, ServiceType> serviceTypesMap;
    private Map<Integer, Service> servicesMap;
    private Map<Integer, Review> reviewsMap;
    private Map<Integer, Appointment> appointmentsMap;
    
    // For sorting and searching
    private List<User> users;
    private List<Customer> customers;
    private List<Provider> providers;
    private List<ServiceType> serviceTypes;
    private List<Service> services;
    private List<Review> reviews;
    private List<Appointment> appointments;
    
    // Current session users
    private User currentUser;
    private Customer currentCustomer;
    private Provider currentProvider;
    private Admin currentAdmin;
    
    public DataManager() {
        // Initialize with ConcurrentHashMap for thread safety
        usersMap = new HashMap<>();
        customersMap = new HashMap<>();
        providersMap = new HashMap<>();
        serviceTypesMap = new HashMap<>();
        servicesMap = new HashMap<>();
        reviewsMap = new HashMap<>();
        appointmentsMap = new HashMap<>();
        
        users = new ArrayList<>();
        customers = new ArrayList<>();
        providers = new ArrayList<>();
        serviceTypes = new ArrayList<>();
        services = new ArrayList<>();
        reviews = new ArrayList<>();
        appointments = new ArrayList<>();
        
        // Load data from files first
        FileManager.loadAllData(this);
        
        // If no data, initialize with sample data
        if (users.isEmpty()) {
            initializeData();
        }
    }
    
    private void initializeData() {
        // Create service types
        addServiceType(new ServiceType(1, "Plumbing", "Fix pipes, leaks, installations", 2.5, "₹500-₹3000"));
        addServiceType(new ServiceType(2, "Electrical", "Wiring, repairs, installations", 3.0, "₹300-₹2500"));
        addServiceType(new ServiceType(3, "Cleaning", "Home/office deep cleaning", 4.0, "₹1000-₹5000"));
        addServiceType(new ServiceType(4, "Carpentry", "Furniture, repairs, custom work", 5.0, "₹800-₹4000"));
        addServiceType(new ServiceType(5, "Painting", "Wall painting, interior/exterior", 6.0, "₹2000-₹10000"));
        
        // Create admin
        Admin admin = new Admin(1, "admin", "admin@system.com", "admin123", "9876543210");
        addUser(admin);
        
        // Create sample customers
        Customer cust1 = new Customer(2, "john_doe", "john@email.com", "pass123", "9876543211", 
                                      "John", "Doe", "123 Main St");
        Customer cust2 = new Customer(3, "jane_smith", "jane@email.com", "pass123", "9876543212", 
                                      "Jane", "Smith", "456 Oak Ave");
        
        addUser(cust1.getUser());
        addUser(cust2.getUser());
        addCustomer(cust1);
        addCustomer(cust2);
        
        // Create sample providers
        Provider provider1 = new Provider(4, "plumber_raj", "raj@service.com", "pass123", "9876543213", 
                                          "Raj Plumbing", serviceTypesMap.get(1), 8, 4.5, true, 400);
        Provider provider2 = new Provider(5, "electric_shyam", "shyam@service.com", "pass123", "9876543214", 
                                          "Shyam Electricals", serviceTypesMap.get(2), 5, 4.2, true, 350);
        
        addUser(provider1.getUser());
        addUser(provider2.getUser());
        addProvider(provider1);
        addProvider(provider2);
        
        // Save to files
        FileManager.backupAllData(this);
        
        System.out.println("System initialized with sample data!");
        System.out.println("Admin Login: admin / admin123");
        System.out.println("Customer Login: john_doe / pass123");
        System.out.println("Provider Login: plumber_raj / pass123");
        System.out.println("================================================");
    }
    
    // Enhanced view methods with sorting
    public void viewAllServiceTypes() {
        System.out.println("\n========== AVAILABLE SERVICES ==========");
        System.out.println("ID  Service         Description                  Duration(hrs)  Price Range");
        System.out.println("-------------------------------------------------------------------------");
        
        // Using stream API for sorting
        serviceTypes.stream()
            .sorted(Comparator.comparing(ServiceType::getServiceName))
            .forEach(type -> {
                System.out.printf("%-3d %-15s %-30s %-12.1f %-15s\n",
                    type.getServiceTypeId(),
                    type.getServiceName(),
                    (type.getDescription().length() > 30 ? type.getDescription().substring(0, 27) + "..." : type.getDescription()),
                    type.getAverageDuration(),
                    type.getBasePriceRange());
            });
    }
    
    public void viewTopProviders() {
        System.out.println("\n========== TOP RATED PROVIDERS ==========");
        System.out.println("ID  Business Name          Service        Rating  Experience  Hourly Rate");
        System.out.println("------------------------------------------------------------------------");
        
        // Using stream API for filtering and sorting
        providers.stream()
            .filter(Provider::isVerified)
            .sorted((p1, p2) -> Double.compare(p2.getAverageRating(), p1.getAverageRating()))
            .limit(10)
            .forEach(provider -> {
                System.out.printf("%-3d %-23s %-15s %-7.1f %-11d ₹%-9.0f\n",
                    provider.getProviderId(),
                    (provider.getBusinessName().length() > 23 ? provider.getBusinessName().substring(0, 20) + "..." : provider.getBusinessName()),
                    provider.getServiceType().getServiceName(),
                    provider.getAverageRating(),
                    provider.getYearsOfExperience(),
                    provider.getHourlyRate());
            });
    }
    
    // Search methods using Collection Framework
    public List<Provider> searchProvidersByServiceType(int serviceTypeId) {
        return providers.stream()
            .filter(p -> p.isVerified() && p.getServiceType().getServiceTypeId() == serviceTypeId)
            .collect(Collectors.toList());
    }
    
    public List<Provider> searchProvidersByRating(double minRating) {
        return providers.stream()
            .filter(p -> p.isVerified() && p.getAverageRating() >= minRating)
            .sorted((p1, p2) -> Double.compare(p2.getAverageRating(), p1.getAverageRating()))
            .collect(Collectors.toList());
    }
    
    public List<Provider> searchProvidersByPriceRange(double maxHourlyRate) {
        return providers.stream()
            .filter(p -> p.isVerified() && p.getHourlyRate() <= maxHourlyRate)
            .sorted(Comparator.comparingDouble(Provider::getHourlyRate))
            .collect(Collectors.toList());
    }
    
    public List<Service> getCustomerServices(int customerId) {
        return services.stream()
            .filter(s -> s.getCustomer().getCustomerId() == customerId)
            .sorted((s1, s2) -> s2.getScheduledDate().compareTo(s1.getScheduledDate()))
            .collect(Collectors.toList());
    }
    
    public List<Service> getProviderServices(int providerId) {
        return services.stream()
            .filter(s -> s.getProvider().getProviderId() == providerId)
            .sorted((s1, s2) -> s2.getScheduledDate().compareTo(s1.getScheduledDate()))
            .collect(Collectors.toList());
    }
    
    // Statistics methods using Collection Framework
    public Map<String, Long> getServiceStatusCounts() {
        return services.stream()
            .collect(Collectors.groupingBy(Service::getStatus, Collectors.counting()));
    }
    
    public Map<Integer, Double> getProviderAverageRatings() {
        Map<Integer, Double> ratings = new HashMap<>();
        reviews.stream()
            .collect(Collectors.groupingBy(r -> r.getProvider().getProviderId(),
                     Collectors.averagingInt(Review::getRating)))
            .forEach(ratings::put);
        return ratings;
    }
    
    public Optional<Provider> getHighestRatedProvider() {
        return providers.stream()
            .filter(Provider::isVerified)
            .max(Comparator.comparingDouble(Provider::getAverageRating));
    }
    
    public double getAverageRatingForProvider(int providerId) {
        return reviews.stream()
            .filter(r -> r.getProvider().getProviderId() == providerId)
            .mapToInt(Review::getRating)
            .average()
            .orElse(0.0);
    }
    
    // Add methods with auto-save
    public void addUser(User user) { 
        usersMap.put(user.getUserId(), user);
        users.add(user);
        FileManager.backupAllData(this);
    }
    
    public void addCustomer(Customer customer) { 
        customersMap.put(customer.getCustomerId(), customer);
        customers.add(customer);
        FileManager.backupAllData(this);
    }
    
    public void addProvider(Provider provider) { 
        providersMap.put(provider.getProviderId(), provider);
        providers.add(provider);
        FileManager.backupAllData(this);
    }
    
    public void addServiceType(ServiceType serviceType) { 
        serviceTypesMap.put(serviceType.getServiceTypeId(), serviceType);
        serviceTypes.add(serviceType);
        FileManager.backupAllData(this);
    }
    
    public void addService(Service service) { 
        servicesMap.put(service.getServiceId(), service);
        services.add(service);
        FileManager.backupAllData(this);
    }
    
    public void addReview(Review review) { 
        reviewsMap.put(review.getReviewId(), review);
        reviews.add(review);
        FileManager.backupAllData(this);
    }
    
    public void addAppointment(Appointment appointment) { 
        appointmentsMap.put(appointment.getAppointmentId(), appointment);
        appointments.add(appointment);
        FileManager.backupAllData(this);
    }
    
    // Remove methods
    public void removeServiceType(ServiceType serviceType) { 
        serviceTypesMap.remove(serviceType.getServiceTypeId());
        serviceTypes.remove(serviceType);
        FileManager.backupAllData(this);
    }
    
    // Getters
    public List<User> getUsers() { return new ArrayList<>(users); }
    public List<Customer> getCustomers() { return new ArrayList<>(customers); }
    public List<Provider> getProviders() { return new ArrayList<>(providers); }
    public List<ServiceType> getServiceTypes() { return new ArrayList<>(serviceTypes); }
    public List<Service> getServices() { return new ArrayList<>(services); }
    public List<Review> getReviews() { return new ArrayList<>(reviews); }
    public List<Appointment> getAppointments() { return new ArrayList<>(appointments); }
    
    // Quick lookup methods using Map
    public User getUserById(int id) { return usersMap.get(id); }
    public Customer getCustomerById(int id) { return customersMap.get(id); }
    public Provider getProviderById(int id) { return providersMap.get(id); }
    public ServiceType getServiceTypeById(int id) { return serviceTypesMap.get(id); }
    public Service getServiceById(int id) { return servicesMap.get(id); }
    public Review getReviewById(int id) { return reviewsMap.get(id); }
    public Appointment getAppointmentById(int id) { return appointmentsMap.get(id); }
    
    // Session management
    public User getCurrentUser() { return currentUser; }
    public Customer getCurrentCustomer() { return currentCustomer; }
    public Provider getCurrentProvider() { return currentProvider; }
    public Admin getCurrentAdmin() { return currentAdmin; }
    
    public void setCurrentUser(User user) { this.currentUser = user; }
    public void setCurrentCustomer(Customer customer) { this.currentCustomer = customer; }
    public void setCurrentProvider(Provider provider) { this.currentProvider = provider; }
    public void setCurrentAdmin(Admin admin) { this.currentAdmin = admin; }
    
    public void logout() {
        currentUser = null;
        currentCustomer = null;
        currentProvider = null;
        currentAdmin = null;
        System.out.println("\n✅ Logged out successfully!");
    }
}