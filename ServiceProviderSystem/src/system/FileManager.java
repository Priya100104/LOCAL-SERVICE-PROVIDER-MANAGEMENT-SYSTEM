package system;

import entities.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class FileManager {
    private static final String DATA_DIR = "data";
    private static final String USERS_FILE = DATA_DIR + "/users.dat";
    private static final String CUSTOMERS_FILE = DATA_DIR + "/customers.dat";
    private static final String PROVIDERS_FILE = DATA_DIR + "/providers.dat";
    private static final String SERVICES_FILE = DATA_DIR + "/services.dat";
    private static final String SERVICE_TYPES_FILE = DATA_DIR + "/servicetypes.dat";
    private static final String REVIEWS_FILE = DATA_DIR + "/reviews.dat";
    private static final String APPOINTMENTS_FILE = DATA_DIR + "/appointments.dat";
    
    static {
        // Create data directory if it doesn't exist
        new File(DATA_DIR).mkdirs();
    }
    
    // Generic save method using serialization
    public static <T> void saveToFile(List<T> data, String filename) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filename))) {
            oos.writeObject(new ArrayList<>(data));
        } catch (IOException e) {
            System.out.println("Error saving to file: " + filename);
        }
    }
    
    // Generic load method using serialization
    @SuppressWarnings("unchecked")
    public static <T> List<T> loadFromFile(String filename) {
        File file = new File(filename);
        if (!file.exists()) {
            return new ArrayList<>();
        }
        
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filename))) {
            return (List<T>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Error loading from file: " + filename);
            return new ArrayList<>();
        }
    }
    
    // Text file export methods
    
    // Backup all data
    public static void backupAllData(DataManager dataManager) {
        saveToFile(dataManager.getUsers(), USERS_FILE);
        saveToFile(dataManager.getCustomers(), CUSTOMERS_FILE);
        saveToFile(dataManager.getProviders(), PROVIDERS_FILE);
        saveToFile(dataManager.getServices(), SERVICES_FILE);
        saveToFile(dataManager.getServiceTypes(), SERVICE_TYPES_FILE);
        saveToFile(dataManager.getReviews(), REVIEWS_FILE);
        saveToFile(dataManager.getAppointments(), APPOINTMENTS_FILE);
        System.out.println("All data backed up successfully!");
    }
    
    // Load all data
    public static void loadAllData(DataManager dataManager) {
        List<User> users = loadFromFile(USERS_FILE);
        List<Customer> customers = loadFromFile(CUSTOMERS_FILE);
        List<Provider> providers = loadFromFile(PROVIDERS_FILE);
        List<Service> services = loadFromFile(SERVICES_FILE);
        List<ServiceType> serviceTypes = loadFromFile(SERVICE_TYPES_FILE);
        List<Review> reviews = loadFromFile(REVIEWS_FILE);
        List<Appointment> appointments = loadFromFile(APPOINTMENTS_FILE);
        
        if (!users.isEmpty()) {
            dataManager.getUsers().clear();
            dataManager.getUsers().addAll(users);
        }
        if (!customers.isEmpty()) {
            dataManager.getCustomers().clear();
            dataManager.getCustomers().addAll(customers);
        }
        if (!providers.isEmpty()) {
            dataManager.getProviders().clear();
            dataManager.getProviders().addAll(providers);
        }
        if (!services.isEmpty()) {
            dataManager.getServices().clear();
            dataManager.getServices().addAll(services);
        }
        if (!serviceTypes.isEmpty()) {
            dataManager.getServiceTypes().clear();
            dataManager.getServiceTypes().addAll(serviceTypes);
        }
        if (!reviews.isEmpty()) {
            dataManager.getReviews().clear();
            dataManager.getReviews().addAll(reviews);
        }
        if (!appointments.isEmpty()) {
            dataManager.getAppointments().clear();
            dataManager.getAppointments().addAll(appointments);
        }
        
        System.out.println("Data loaded from files. Total records loaded:");
        System.out.println("Users: " + dataManager.getUsers().size());
        System.out.println("Services: " + dataManager.getServices().size());
    }
    // Add this method for exporting providers
    public static void exportProvidersToCSV(List<Provider> providers, String filename) {
    try (PrintWriter writer = new PrintWriter(new FileWriter(DATA_DIR + "/" + filename))) {
        writer.println("Provider ID,Business Name,Service Type,Rating,Experience,Verified,Hourly Rate,Phone,Email");
        
        for (Provider p : providers) {
            writer.printf("%d,\"%s\",\"%s\",%.1f,%d,%s,%.2f,\"%s\",\"%s\"%n",
                p.getProviderId(),
                p.getBusinessName().replace(",", " ").replace("\"", "\"\""),
                p.getServiceType().getServiceName().replace(",", " ").replace("\"", "\"\""),
                p.getAverageRating(),
                p.getYearsOfExperience(),
                p.isVerified() ? "Yes" : "No",
                p.getHourlyRate(),
                p.getUser().getPhone().replace(",", " "),
                p.getUser().getEmail().replace(",", " ")
            );
        }
        System.out.println("✅ Data exported to " + DATA_DIR + "/" + filename);
    } catch (IOException e) {
        System.out.println("Error exporting to CSV: " + e.getMessage());
    }
    }

    // Add this method for exporting customers
    public static void exportCustomersToCSV(List<Customer> customers, String filename) {
    try (PrintWriter writer = new PrintWriter(new FileWriter(DATA_DIR + "/" + filename))) {
        writer.println("Customer ID,First Name,Last Name,Email,Phone,Address,Join Date");
        
        for (Customer c : customers) {
            writer.printf("%d,\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",%s%n",
                c.getCustomerId(),
                c.getFirstName().replace(",", " ").replace("\"", "\"\""),
                c.getLastName().replace(",", " ").replace("\"", "\"\""),
                c.getUser().getEmail().replace(",", " "),
                c.getUser().getPhone().replace(",", " "),
                c.getAddress().replace(",", " ").replace("\"", "\"\""),
                c.getJoinDate()
            );
        }
        System.out.println("✅ Data exported to " + DATA_DIR + "/" + filename);
    } catch (IOException e) {
        System.out.println("Error exporting to CSV: " + e.getMessage());
    }
    }

    // Also make sure you have the service export method
    public static void exportServicesToCSV(List<Service> services, String filename) {
    try (PrintWriter writer = new PrintWriter(new FileWriter(DATA_DIR + "/" + filename))) {
        writer.println("Service ID,Service Type,Provider,Customer,Status,Date,Final Price");
        
        for (Service s : services) {
            writer.printf("%d,\"%s\",\"%s\",\"%s %s\",%s,%s,%.2f%n",
                s.getServiceId(),
                s.getServiceType().getServiceName().replace(",", " ").replace("\"", "\"\""),
                s.getProvider().getBusinessName().replace(",", " ").replace("\"", "\"\""),
                s.getCustomer().getFirstName().replace(",", " "),
                s.getCustomer().getLastName().replace(",", " "),
                s.getStatus(),
                s.getScheduledDate().toLocalDate(),
                s.getFinalPrice()
            );
        }
        System.out.println("✅ Data exported to " + DATA_DIR + "/" + filename);
    } catch (IOException e) {
        System.out.println("Error exporting to CSV: " + e.getMessage());
    }
    }
}