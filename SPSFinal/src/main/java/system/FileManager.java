package system;

import entities.*;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class FileManager {

    private static final String DATA_DIR             = "data";
    private static final String USERS_FILE           = DATA_DIR + "/users.dat";
    private static final String CUSTOMERS_FILE       = DATA_DIR + "/customers.dat";
    private static final String PROVIDERS_FILE       = DATA_DIR + "/providers.dat";
    private static final String SERVICES_FILE        = DATA_DIR + "/services.dat";
    private static final String SERVICE_TYPES_FILE   = DATA_DIR + "/servicetypes.dat";
    private static final String REVIEWS_FILE         = DATA_DIR + "/reviews.dat";
    private static final String APPOINTMENTS_FILE    = DATA_DIR + "/appointments.dat";

    static {
        new File(DATA_DIR).mkdirs();
    }

    public static <T> void saveToFile(List<T> data, String filename) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filename))) {
            oos.writeObject(new ArrayList<>(data));
        } catch (IOException e) {
            System.out.println("Error saving to file: " + filename);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> List<T> loadFromFile(String filename) {
        File file = new File(filename);
        if (!file.exists()) return new ArrayList<>();
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filename))) {
            return (List<T>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Error loading from file: " + filename);
            return new ArrayList<>();
        }
    }

    public static void backupAllData(DataManager dataManager) {
        saveToFile(dataManager.getUsers(),        USERS_FILE);
        saveToFile(dataManager.getCustomers(),    CUSTOMERS_FILE);
        saveToFile(dataManager.getProviders(),    PROVIDERS_FILE);
        saveToFile(dataManager.getServices(),     SERVICES_FILE);
        saveToFile(dataManager.getServiceTypes(), SERVICE_TYPES_FILE);
        saveToFile(dataManager.getReviews(),      REVIEWS_FILE);
        saveToFile(dataManager.getAppointments(), APPOINTMENTS_FILE);
    }

    public static void loadAllData(DataManager dataManager) {
        List<User>        users        = loadFromFile(USERS_FILE);
        List<Customer>    customers    = loadFromFile(CUSTOMERS_FILE);
        List<Provider>    providers    = loadFromFile(PROVIDERS_FILE);
        List<Service>     services     = loadFromFile(SERVICES_FILE);
        List<ServiceType> serviceTypes = loadFromFile(SERVICE_TYPES_FILE);
        List<Review>      reviews      = loadFromFile(REVIEWS_FILE);
        List<Appointment> appointments = loadFromFile(APPOINTMENTS_FILE);

        if (!users.isEmpty())        { dataManager.getUsers().clear();        dataManager.getUsers().addAll(users); }
        if (!customers.isEmpty())    { dataManager.getCustomers().clear();    dataManager.getCustomers().addAll(customers); }
        if (!providers.isEmpty())    { dataManager.getProviders().clear();    dataManager.getProviders().addAll(providers); }
        if (!services.isEmpty())     { dataManager.getServices().clear();     dataManager.getServices().addAll(services); }
        if (!serviceTypes.isEmpty()) { dataManager.getServiceTypes().clear(); dataManager.getServiceTypes().addAll(serviceTypes); }
        if (!reviews.isEmpty())      { dataManager.getReviews().clear();      dataManager.getReviews().addAll(reviews); }
        if (!appointments.isEmpty()) { dataManager.getAppointments().clear(); dataManager.getAppointments().addAll(appointments); }

        if (!users.isEmpty()) {
            System.out.println("Data loaded. Users: " + dataManager.getUsers().size() + ", Services: " + dataManager.getServices().size());
        }
    }

    public static void exportProvidersToCSV(List<Provider> providers, String filename) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(DATA_DIR + "/" + filename))) {
            writer.println("Provider ID,Business Name,Service Type,Rating,Experience,Verified,Hourly Rate,Phone,Email");
            for (Provider p : providers) {
                writer.printf("%d,\"%s\",\"%s\",%.1f,%d,%s,%.2f,\"%s\",\"%s\"%n",
                    p.getProviderId(),
                    p.getBusinessName().replace(",", " ").replace("\"", "\"\""),
                    p.getServiceType().getServiceName().replace(",", " "),
                    p.getAverageRating(), p.getYearsOfExperience(),
                    p.isVerified() ? "Yes" : "No", p.getHourlyRate(),
                    p.getUser().getPhone(), p.getUser().getEmail());
            }
            System.out.println("✅ Exported to " + DATA_DIR + "/" + filename);
        } catch (IOException e) { System.out.println("Error exporting: " + e.getMessage()); }
    }

    public static void exportCustomersToCSV(List<Customer> customers, String filename) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(DATA_DIR + "/" + filename))) {
            writer.println("Customer ID,First Name,Last Name,Email,Phone,Address,Join Date");
            for (Customer c : customers) {
                writer.printf("%d,\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",%s%n",
                    c.getCustomerId(), c.getFirstName(), c.getLastName(),
                    c.getUser().getEmail(), c.getUser().getPhone(),
                    c.getAddress().replace(",", " "), c.getJoinDate());
            }
            System.out.println("✅ Exported to " + DATA_DIR + "/" + filename);
        } catch (IOException e) { System.out.println("Error exporting: " + e.getMessage()); }
    }

    public static void exportServicesToCSV(List<Service> services, String filename) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(DATA_DIR + "/" + filename))) {
            writer.println("Service ID,Service Type,Provider,Customer,Status,Date,Final Price");
            for (Service s : services) {
                writer.printf("%d,\"%s\",\"%s\",\"%s %s\",%s,%s,%.2f%n",
                    s.getServiceId(),
                    s.getServiceType().getServiceName().replace(",", " "),
                    s.getProvider().getBusinessName().replace(",", " "),
                    s.getCustomer().getFirstName(), s.getCustomer().getLastName(),
                    s.getStatus(), s.getScheduledDate().toLocalDate(), s.getFinalPrice());
            }
            System.out.println("✅ Exported to " + DATA_DIR + "/" + filename);
        } catch (IOException e) { System.out.println("Error exporting: " + e.getMessage()); }
    }
}
