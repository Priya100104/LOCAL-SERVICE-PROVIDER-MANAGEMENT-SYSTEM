package database;

import java.sql.*;

/**
 * ============================================================
 *  CO2 - SQLite Database Layer
 *  Connects to SQLite file: data/serviceprovider.db
 *  Mirrors all data from the console system into SQLite tables.
 *  Run independently using: DatabaseDemo.main()
 * ============================================================
 */
public class SQLiteDatabase {

    private static final String DB_URL = "jdbc:sqlite:data/serviceprovider.db";
    private Connection connection;

    // ==================== CONNECT ====================
    public void connect() throws SQLException {
        new java.io.File("data").mkdirs();
        connection = DriverManager.getConnection(DB_URL);
        connection.createStatement().execute("PRAGMA foreign_keys = ON");
        System.out.println("✅ Connected to SQLite database: data/serviceprovider.db");
    }

    public void disconnect() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
            System.out.println("✅ Disconnected from SQLite.");
        }
    }

    public Connection getConnection() { return connection; }

    // ==================== CREATE TABLES ====================
    public void createTables() throws SQLException {
        String[] ddl = {
            // service_types table
            """
            CREATE TABLE IF NOT EXISTS service_types (
                service_type_id   INTEGER PRIMARY KEY,
                service_name      TEXT    NOT NULL,
                description       TEXT,
                average_duration  REAL,
                base_price_range  TEXT
            )
            """,
            // users table
            """
            CREATE TABLE IF NOT EXISTS users (
                user_id   INTEGER PRIMARY KEY,
                username  TEXT    NOT NULL UNIQUE,
                email     TEXT    NOT NULL,
                password  TEXT    NOT NULL,
                phone     TEXT,
                is_active INTEGER DEFAULT 1,
                role      TEXT    DEFAULT 'customer'
            )
            """,
            // customers table
            """
            CREATE TABLE IF NOT EXISTS customers (
                customer_id INTEGER PRIMARY KEY,
                user_id     INTEGER REFERENCES users(user_id),
                first_name  TEXT,
                last_name   TEXT,
                address     TEXT,
                join_date   TEXT
            )
            """,
            // providers table
            """
            CREATE TABLE IF NOT EXISTS providers (
                provider_id         INTEGER PRIMARY KEY,
                user_id             INTEGER REFERENCES users(user_id),
                business_name       TEXT,
                service_type_id     INTEGER REFERENCES service_types(service_type_id),
                years_of_experience INTEGER,
                average_rating      REAL    DEFAULT 0.0,
                is_verified         INTEGER DEFAULT 0,
                hourly_rate         REAL
            )
            """,
            // services table
            """
            CREATE TABLE IF NOT EXISTS services (
                service_id      INTEGER PRIMARY KEY,
                provider_id     INTEGER REFERENCES providers(provider_id),
                customer_id     INTEGER REFERENCES customers(customer_id),
                service_type_id INTEGER REFERENCES service_types(service_type_id),
                description     TEXT,
                address         TEXT,
                scheduled_date  TEXT,
                completed_date  TEXT,
                status          TEXT    DEFAULT 'requested',
                quoted_price    REAL    DEFAULT 0.0,
                final_price     REAL    DEFAULT 0.0
            )
            """,
            // reviews table
            """
            CREATE TABLE IF NOT EXISTS reviews (
                review_id   INTEGER PRIMARY KEY,
                customer_id INTEGER REFERENCES customers(customer_id),
                provider_id INTEGER REFERENCES providers(provider_id),
                service_id  INTEGER REFERENCES services(service_id),
                rating      INTEGER,
                title       TEXT,
                comment     TEXT,
                review_date TEXT
            )
            """,
            // appointments table
            """
            CREATE TABLE IF NOT EXISTS appointments (
                appointment_id   INTEGER PRIMARY KEY,
                service_id       INTEGER REFERENCES services(service_id),
                appointment_time TEXT,
                status           TEXT DEFAULT 'scheduled',
                notes            TEXT
            )
            """
        };

        try (Statement stmt = connection.createStatement()) {
            for (String sql : ddl) {
                stmt.execute(sql);
            }
        }
        System.out.println("✅ All SQLite tables created successfully!");
    }

    // ==================== INSERT SAMPLE DATA ====================
    public void insertSampleData() throws SQLException {
        connection.setAutoCommit(false);
        try {
            // Service Types
            String insertST = "INSERT OR IGNORE INTO service_types VALUES (?,?,?,?,?)";
            try (PreparedStatement ps = connection.prepareStatement(insertST)) {
                Object[][] types = {
                    {1, "Plumbing",   "Fix pipes, leaks, installations",    2.5, "₹500-₹3000"},
                    {2, "Electrical", "Wiring, repairs, installations",     3.0, "₹300-₹2500"},
                    {3, "Cleaning",   "Home/office deep cleaning",          4.0, "₹1000-₹5000"},
                    {4, "Carpentry",  "Furniture, repairs, custom work",    5.0, "₹800-₹4000"},
                    {5, "Painting",   "Wall painting, interior/exterior",   6.0, "₹2000-₹10000"}
                };
                for (Object[] row : types) {
                    ps.setInt(1, (int) row[0]); ps.setString(2, (String) row[1]);
                    ps.setString(3, (String) row[2]); ps.setDouble(4, (double) row[3]);
                    ps.setString(5, (String) row[4]); ps.addBatch();
                }
                ps.executeBatch();
            }

            // Users
            String insertUser = "INSERT OR IGNORE INTO users(user_id,username,email,password,phone,role) VALUES(?,?,?,?,?,?)";
            try (PreparedStatement ps = connection.prepareStatement(insertUser)) {
                Object[][] usersData = {
                    {1, "admin",          "admin@system.com",  "admin123", "9876543210", "admin"},
                    {2, "john_doe",       "john@email.com",    "pass123",  "9876543211", "customer"},
                    {3, "jane_smith",     "jane@email.com",    "pass123",  "9876543212", "customer"},
                    {4, "plumber_raj",    "raj@service.com",   "pass123",  "9876543213", "provider"},
                    {5, "electric_shyam","shyam@service.com", "pass123",  "9876543214", "provider"}
                };
                for (Object[] row : usersData) {
                    ps.setInt(1,(int)row[0]); ps.setString(2,(String)row[1]); ps.setString(3,(String)row[2]);
                    ps.setString(4,(String)row[3]); ps.setString(5,(String)row[4]); ps.setString(6,(String)row[5]);
                    ps.addBatch();
                }
                ps.executeBatch();
            }

            // Customers
            String insertCust = "INSERT OR IGNORE INTO customers VALUES(?,?,?,?,?,?)";
            try (PreparedStatement ps = connection.prepareStatement(insertCust)) {
                ps.setInt(1, 2); ps.setInt(2, 2); ps.setString(3, "John"); ps.setString(4, "Doe");
                ps.setString(5, "123 Main St"); ps.setString(6, java.time.LocalDate.now().toString()); ps.addBatch();
                ps.setInt(1, 3); ps.setInt(2, 3); ps.setString(3, "Jane"); ps.setString(4, "Smith");
                ps.setString(5, "456 Oak Ave"); ps.setString(6, java.time.LocalDate.now().toString()); ps.addBatch();
                ps.executeBatch();
            }

            // Providers
            String insertProv = "INSERT OR IGNORE INTO providers VALUES(?,?,?,?,?,?,?,?)";
            try (PreparedStatement ps = connection.prepareStatement(insertProv)) {
                ps.setInt(1,4); ps.setInt(2,4); ps.setString(3,"Raj Plumbing");    ps.setInt(4,1); ps.setInt(5,8); ps.setDouble(6,4.5); ps.setInt(7,1); ps.setDouble(8,400); ps.addBatch();
                ps.setInt(1,5); ps.setInt(2,5); ps.setString(3,"Shyam Electricals");ps.setInt(4,2); ps.setInt(5,5); ps.setDouble(6,4.2); ps.setInt(7,1); ps.setDouble(8,350); ps.addBatch();
                ps.executeBatch();
            }

            connection.commit();
            System.out.println("✅ Sample data inserted into SQLite!");
        } catch (SQLException e) {
            connection.rollback();
            throw e;
        } finally {
            connection.setAutoCommit(true);
        }
    }

    // ==================== QUERY METHODS ====================
    public void showAllProviders() throws SQLException {
        System.out.println("\n========== PROVIDERS FROM SQLITE ==========");
        System.out.println("ID  Business Name          Service Type  Rating  Verified  Rate");
        System.out.println("----------------------------------------------------------------");
        String sql = """
            SELECT p.provider_id, p.business_name, s.service_name,
                   p.average_rating, p.is_verified, p.hourly_rate
            FROM providers p
            JOIN service_types s ON p.service_type_id = s.service_type_id
            ORDER BY p.average_rating DESC
            """;
        try (Statement stmt = connection.createStatement();
             ResultSet rs   = stmt.executeQuery(sql)) {
            while (rs.next()) {
                System.out.printf("%-3d %-23s %-13s %-7.1f %-9s ₹%.0f%n",
                    rs.getInt("provider_id"), rs.getString("business_name"),
                    rs.getString("service_name"), rs.getDouble("average_rating"),
                    rs.getInt("is_verified") == 1 ? "Yes" : "No", rs.getDouble("hourly_rate"));
            }
        }
    }

    public void showAllCustomers() throws SQLException {
        System.out.println("\n========== CUSTOMERS FROM SQLITE ==========");
        System.out.println("ID  Name            Email             Phone");
        System.out.println("--------------------------------------------");
        String sql = """
            SELECT c.customer_id, c.first_name, c.last_name, u.email, u.phone
            FROM customers c JOIN users u ON c.user_id = u.user_id
            ORDER BY c.customer_id
            """;
        try (Statement stmt = connection.createStatement();
             ResultSet rs   = stmt.executeQuery(sql)) {
            while (rs.next()) {
                System.out.printf("%-3d %-15s %-17s %-15s%n",
                    rs.getInt("customer_id"),
                    rs.getString("first_name") + " " + rs.getString("last_name"),
                    rs.getString("email"), rs.getString("phone"));
            }
        }
    }

    public void showAllServiceTypes() throws SQLException {
        System.out.println("\n========== SERVICE TYPES FROM SQLITE ==========");
        String sql = "SELECT * FROM service_types ORDER BY service_name";
        try (Statement stmt = connection.createStatement();
             ResultSet rs   = stmt.executeQuery(sql)) {
            while (rs.next()) {
                System.out.printf("ID: %d | %-12s | Duration: %.1f hrs | Price: %s%n",
                    rs.getInt("service_type_id"), rs.getString("service_name"),
                    rs.getDouble("average_duration"), rs.getString("base_price_range"));
            }
        }
    }

    // ==================== DEMO MAIN ====================
    public static void main(String[] args) {
        SQLiteDatabase db = new SQLiteDatabase();
        try {
            db.connect();
            db.createTables();
            db.insertSampleData();
            db.showAllServiceTypes();
            db.showAllProviders();
            db.showAllCustomers();
            db.disconnect();
        } catch (SQLException e) {
            System.out.println("❌ SQLite Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
