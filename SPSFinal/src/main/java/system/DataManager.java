package system;

import entities.*;

import java.util.*;
import java.util.stream.Collectors;

public class DataManager {

    private Map<Integer, User>        usersMap;
    private Map<Integer, Customer>    customersMap;
    private Map<Integer, Provider>    providersMap;
    private Map<Integer, ServiceType> serviceTypesMap;
    private Map<Integer, Service>     servicesMap;
    private Map<Integer, Review>      reviewsMap;
    private Map<Integer, Appointment> appointmentsMap;

    private List<User>        users;
    private List<Customer>    customers;
    private List<Provider>    providers;
    private List<ServiceType> serviceTypes;
    private List<Service>     services;
    private List<Review>      reviews;
    private List<Appointment> appointments;

    private User     currentUser;
    private Customer currentCustomer;
    private Provider currentProvider;
    private Admin    currentAdmin;

    public DataManager() {
        usersMap        = new HashMap<>();
        customersMap    = new HashMap<>();
        providersMap    = new HashMap<>();
        serviceTypesMap = new HashMap<>();
        servicesMap     = new HashMap<>();
        reviewsMap      = new HashMap<>();
        appointmentsMap = new HashMap<>();

        users        = new ArrayList<>();
        customers    = new ArrayList<>();
        providers    = new ArrayList<>();
        serviceTypes = new ArrayList<>();
        services     = new ArrayList<>();
        reviews      = new ArrayList<>();
        appointments = new ArrayList<>();

        FileManager.loadAllData(this);

        if (users.isEmpty()) {
            initializeData();
        }
    }

    private void initializeData() {
        addServiceType(new ServiceType(1, "Plumbing",   "Fix pipes, leaks, installations",    2.5, "₹500-₹3000"));
        addServiceType(new ServiceType(2, "Electrical", "Wiring, repairs, installations",     3.0, "₹300-₹2500"));
        addServiceType(new ServiceType(3, "Cleaning",   "Home/office deep cleaning",          4.0, "₹1000-₹5000"));
        addServiceType(new ServiceType(4, "Carpentry",  "Furniture, repairs, custom work",    5.0, "₹800-₹4000"));
        addServiceType(new ServiceType(5, "Painting",   "Wall painting, interior/exterior",   6.0, "₹2000-₹10000"));

        Admin admin = new Admin(1, "admin", "admin@system.com", "admin123", "9876543210");
        addUser(admin);

        Customer cust1 = new Customer(2, "john_doe",   "john@email.com", "pass123", "9876543211", "John", "Doe",   "123 Main St");
        Customer cust2 = new Customer(3, "jane_smith", "jane@email.com", "pass123", "9876543212", "Jane", "Smith", "456 Oak Ave");
        addUser(cust1.getUser()); addCustomer(cust1);
        addUser(cust2.getUser()); addCustomer(cust2);

        Provider p1 = new Provider(4, "plumber_raj",    "raj@service.com",  "pass123", "9876543213", "Raj Plumbing",     serviceTypesMap.get(1), 8, 4.5, true, 400);
        Provider p2 = new Provider(5, "electric_shyam", "shyam@service.com","pass123", "9876543214", "Shyam Electricals",serviceTypesMap.get(2), 5, 4.2, true, 350);
        addUser(p1.getUser()); addProvider(p1);
        addUser(p2.getUser()); addProvider(p2);

        FileManager.backupAllData(this);

        System.out.println("System initialized with sample data!");
        System.out.println("Admin Login    : admin / admin123");
        System.out.println("Customer Login : john_doe / pass123");
        System.out.println("Provider Login : plumber_raj / pass123");
        System.out.println("================================================");
    }

    // ==================== VIEW METHODS ====================
    public void viewAllServiceTypes() {
        System.out.println("\n========== AVAILABLE SERVICES ==========");
        System.out.println("ID  Service         Description                  Duration(hrs)  Price Range");
        System.out.println("-------------------------------------------------------------------------");
        serviceTypes.stream()
            .sorted(Comparator.comparing(ServiceType::getServiceName))
            .forEach(t -> System.out.printf("%-3d %-15s %-30s %-12.1f %-15s%n",
                t.getServiceTypeId(), t.getServiceName(),
                (t.getDescription().length() > 30 ? t.getDescription().substring(0, 27) + "..." : t.getDescription()),
                t.getAverageDuration(), t.getBasePriceRange()));
    }

    public void viewTopProviders() {
        System.out.println("\n========== TOP RATED PROVIDERS ==========");
        System.out.println("ID  Business Name          Service        Rating  Experience  Hourly Rate");
        System.out.println("------------------------------------------------------------------------");
        providers.stream()
            .filter(Provider::isVerified)
            .sorted((a, b) -> Double.compare(b.getAverageRating(), a.getAverageRating()))
            .limit(10)
            .forEach(p -> System.out.printf("%-3d %-23s %-15s %-7.1f %-11d ₹%-9.0f%n",
                p.getProviderId(),
                (p.getBusinessName().length() > 23 ? p.getBusinessName().substring(0, 20) + "..." : p.getBusinessName()),
                p.getServiceType().getServiceName(),
                p.getAverageRating(), p.getYearsOfExperience(), p.getHourlyRate()));
    }

    // ==================== SEARCH METHODS ====================
    public List<Provider> searchProvidersByServiceType(int serviceTypeId) {
        return providers.stream()
            .filter(p -> p.isVerified() && p.getServiceType().getServiceTypeId() == serviceTypeId)
            .collect(Collectors.toList());
    }

    public List<Provider> searchProvidersByRating(double minRating) {
        return providers.stream()
            .filter(p -> p.isVerified() && p.getAverageRating() >= minRating)
            .sorted((a, b) -> Double.compare(b.getAverageRating(), a.getAverageRating()))
            .collect(Collectors.toList());
    }

    public List<Service> getCustomerServices(int customerId) {
        return services.stream()
            .filter(s -> s.getCustomer().getCustomerId() == customerId)
            .sorted((a, b) -> b.getScheduledDate().compareTo(a.getScheduledDate()))
            .collect(Collectors.toList());
    }

    public Map<String, Long> getServiceStatusCounts() {
        return services.stream().collect(Collectors.groupingBy(Service::getStatus, Collectors.counting()));
    }

    public Optional<Provider> getHighestRatedProvider() {
        return providers.stream()
            .filter(Provider::isVerified)
            .max(Comparator.comparingDouble(Provider::getAverageRating));
    }

    // ==================== ADD METHODS ====================
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

    public void removeServiceType(ServiceType serviceType) {
        serviceTypesMap.remove(serviceType.getServiceTypeId());
        serviceTypes.remove(serviceType);
        FileManager.backupAllData(this);
    }

    // ==================== GETTERS ====================
    public List<User>        getUsers()        { return new ArrayList<>(users); }
    public List<Customer>    getCustomers()    { return new ArrayList<>(customers); }
    public List<Provider>    getProviders()    { return new ArrayList<>(providers); }
    public List<ServiceType> getServiceTypes() { return new ArrayList<>(serviceTypes); }
    public List<Service>     getServices()     { return new ArrayList<>(services); }
    public List<Review>      getReviews()      { return new ArrayList<>(reviews); }
    public List<Appointment> getAppointments() { return new ArrayList<>(appointments); }

    public Map<Integer, User>        getUsersMap()        { return usersMap; }
    public Map<Integer, Customer>    getCustomersMap()    { return customersMap; }
    public Map<Integer, Provider>    getProvidersMap()    { return providersMap; }
    public Map<Integer, ServiceType> getServiceTypesMap() { return serviceTypesMap; }
    public Map<Integer, Service>     getServicesMap()     { return servicesMap; }

    public User         getUserById(int id)        { return usersMap.get(id); }
    public Customer     getCustomerById(int id)    { return customersMap.get(id); }
    public Provider     getProviderById(int id)    { return providersMap.get(id); }
    public ServiceType  getServiceTypeById(int id) { return serviceTypesMap.get(id); }
    public Service      getServiceById(int id)     { return servicesMap.get(id); }

    // ==================== SESSION ====================
    public User     getCurrentUser()     { return currentUser; }
    public Customer getCurrentCustomer() { return currentCustomer; }
    public Provider getCurrentProvider() { return currentProvider; }
    public Admin    getCurrentAdmin()    { return currentAdmin; }

    public void setCurrentUser(User u)         { this.currentUser     = u; }
    public void setCurrentCustomer(Customer c) { this.currentCustomer = c; }
    public void setCurrentProvider(Provider p) { this.currentProvider = p; }
    public void setCurrentAdmin(Admin a)       { this.currentAdmin    = a; }

    public void logout() {
        currentUser     = null;
        currentCustomer = null;
        currentProvider = null;
        currentAdmin    = null;
        System.out.println("\n✅ Logged out successfully!");
    }
}
