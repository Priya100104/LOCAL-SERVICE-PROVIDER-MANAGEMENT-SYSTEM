# Local Service Provider Management System
## Complete Setup & Run Guide

---

## STEP 0 — Fix Maven Error (Do This First!)

Your error: `source release 7 no longer supported use 8 or higher`

**Root Cause:** Maven defaults to Java 7 unless told otherwise.  
**Fix:** Already done in this project's `pom.xml` and `build.gradle`:

```xml
<!-- pom.xml — already included -->
<properties>
    <maven.compiler.source>21</maven.compiler.source>
    <maven.compiler.target>21</maven.compiler.target>
</properties>
```

For your **old Maven projects**, open each `pom.xml` and add those 2 lines.

---

## PROJECT STRUCTURE

```
ServiceProviderSystem/
├── pom.xml                          ← Maven build file (Java 21 fixed)
├── build.gradle                     ← Gradle build file (Java 21 fixed)
├── settings.gradle
└── src/main/java/
    ├── main/
    │   └── MainApp.java             ← Console entry point (YOUR ORIGINAL)
    ├── entities/                    ← YOUR ORIGINAL CODE (unchanged)
    │   ├── User.java
    │   ├── Admin.java
    │   ├── Customer.java
    │   ├── Provider.java
    │   ├── ServiceType.java
    │   ├── Service.java
    │   ├── Review.java
    │   └── Appointment.java
    ├── system/                      ← YOUR ORIGINAL CODE (unchanged)
    │   ├── ServiceProviderSystem.java
    │   ├── DataManager.java
    │   └── FileManager.java
    ├── database/
    │   └── SQLiteDatabase.java      ← NEW: SQLite JDBC (CO2)
    ├── rest/
    │   ├── ServiceProviderEntity.java   ← NEW: JPA Entity (CO5)
    │   ├── ServiceProviderRepository.java ← NEW: JPA Repository
    │   └── ServiceProviderRestController.java ← NEW: REST API (CO4)
    ├── web/
    │   ├── SpringApp.java           ← NEW: Spring Boot App
    │   └── WebController.java       ← NEW: HTML Controller (CO5)
    └── exceptions/
        ├── ProviderNotFoundException.java  ← NEW: Custom Exceptions
        ├── InvalidRatingException.java
        ├── DuplicateProviderException.java
        └── GlobalExceptionHandler.java     ← NEW: @ControllerAdvice
```

---

## MODE 1 — CONSOLE APP (Your Original Code)

This runs your original menu-driven program exactly as before.

### Using Maven:
```bash
# Step 1: Compile
mvn clean compile

# Step 2: Run
mvn exec:java -Dexec.mainClass="main.MainApp"

# OR build JAR and run:
mvn clean package
java -cp target/ServiceProviderSystem-1.0.0.jar main.MainApp
```

### Using Gradle:
```bash
# Step 1: Build
./gradlew build

# Step 2: Run
./gradlew run

# On Windows:
gradlew.bat run
```

**Login Credentials:**
- Admin:    `admin / admin123`
- Customer: `john_doe / pass123`
- Provider: `plumber_raj / pass123`

---

## MODE 2 — SQLITE (CO2)

Runs the SQLite JDBC demo — creates `data/serviceprovider.db`.

```bash
mvn exec:java -Dexec.mainClass="database.SQLiteDatabase"
```

This will:
1. Create SQLite DB file at `data/serviceprovider.db`
2. Create all tables (users, providers, customers, services, etc.)
3. Insert sample data
4. Display all records from SQLite

---

## MODE 3 — SPRING BOOT (REST API + HTML + JPA)

This starts the web server with REST API and HTML pages.

```bash
# Run Spring Boot
mvn spring-boot:run

# OR with Gradle:
./gradlew bootRun
```

Then open your browser:

| URL | What it shows |
|-----|---------------|
| `http://localhost:8080/web/` | Home page (HTML) |
| `http://localhost:8080/web/providers` | All providers (HTML table) |
| `http://localhost:8080/web/providers/add` | Add provider form (HTML) |
| `http://localhost:8080/web/providers/top` | Top rated providers |
| `http://localhost:8080/api/providers` | REST API - all providers (JSON) |
| `http://localhost:8080/api/providers/1` | REST API - provider by ID (JSON) |
| `http://localhost:8080/api/providers/top` | REST API - top rated (JSON) |
| `http://localhost:8080/api/providers/stats` | REST API - statistics (JSON) |
| `http://localhost:8080/h2-console` | H2 database console |

---

## REST API - All Endpoints (CO4)

Use **Postman** or **curl** or the **Thunder Client** extension in VS Code:

```bash
# GET all providers
curl http://localhost:8080/api/providers

# GET provider by ID
curl http://localhost:8080/api/providers/1

# GET top rated providers
curl http://localhost:8080/api/providers/top

# GET by service type
curl http://localhost:8080/api/providers/type/Plumbing

# GET statistics
curl http://localhost:8080/api/providers/stats

# POST - add new provider
curl -X POST http://localhost:8080/api/providers \
  -H "Content-Type: application/json" \
  -d '{
    "businessName": "Test Plumber",
    "serviceType": "Plumbing",
    "username": "test_plumb",
    "email": "test@plumb.com",
    "phone": "9999999999",
    "yearsOfExperience": 3,
    "hourlyRate": 300
  }'

# PUT - verify provider
curl -X PUT http://localhost:8080/api/providers/1/verify

# PUT - add rating (1-5)
curl -X PUT http://localhost:8080/api/providers/1/rating \
  -H "Content-Type: application/json" \
  -d '{"rating": 5}'

# DELETE provider
curl -X DELETE http://localhost:8080/api/providers/1
```

---

## JPA (CO5)

JPA is already active when you run Spring Boot.  
The `ServiceProviderEntity.java` uses:
- `@Entity`  — marks it as a database table
- `@Table`   — table name = `service_providers`
- `@Id`      — primary key
- `@GeneratedValue` — auto-increment ID
- `@Column`  — column mapping

The `ServiceProviderRepository.java` uses:
- `extends JpaRepository<ServiceProviderEntity, Long>`
- Custom methods: `findByServiceType()`, `findByIsVerifiedTrue()`, etc.
- `@Query` — custom JPQL queries

---

## EXCEPTION HANDLING (CO5)

Custom exceptions in `exceptions/` package:

| Exception | When thrown | HTTP Status |
|-----------|-------------|-------------|
| `ProviderNotFoundException` | Provider ID not found | 404 |
| `InvalidRatingException`    | Rating outside 1-5  | 400 |
| `DuplicateProviderException`| Username exists     | 409 |

**GlobalExceptionHandler** (`@ControllerAdvice`):
- Returns **JSON** for REST API requests (`/api/...`)
- Returns **HTML error page** for web browser requests (`/web/...`)

Test exception handling:
```bash
# 404 - provider not found
curl http://localhost:8080/api/providers/99999

# 409 - duplicate username (add same provider twice)
# 400 - invalid rating
curl -X PUT http://localhost:8080/api/providers/1/rating \
  -H "Content-Type: application/json" \
  -d '{"rating": 10}'
```

---

## H2 CONSOLE (View JPA Database)

1. Open: `http://localhost:8080/h2-console`
2. JDBC URL: `jdbc:h2:mem:serviceproviderdb`
3. Username: `sa`  |  Password: *(leave blank)*
4. Click Connect
5. Run: `SELECT * FROM SERVICE_PROVIDERS;`

---

## VS CODE TIPS

1. **Fix Java version:** `Ctrl+Shift+P` → "Java: Configure Java Runtime" → select JDK 21
2. **Run Console:** Right-click `MainApp.java` → "Run Java"
3. **Run Spring Boot:** Right-click `SpringApp.java` → "Run Java"  
   OR use Spring Boot Dashboard (from Spring Boot Extension Pack)
4. **Test REST API:** Install "Thunder Client" extension in VS Code

---

## CONCEPT MAP

| Your Task | File | Concept |
|-----------|------|---------|
| Console App (original) | `main/MainApp.java` | OOP, Serialization, Collections |
| Maven Build | `pom.xml` | Maven, Java 21 fix |
| Gradle Build | `build.gradle` | Gradle |
| SQLite DB | `database/SQLiteDatabase.java` | JDBC, SQL, PreparedStatement |
| JPA Entity | `rest/ServiceProviderEntity.java` | @Entity, @Id, @Column |
| JPA Repository | `rest/ServiceProviderRepository.java` | Spring Data JPA, @Repository |
| REST API | `rest/ServiceProviderRestController.java` | @RestController, @GetMapping, @PostMapping |
| HTML Pages | `web/WebController.java` + templates/ | @Controller, Thymeleaf |
| Exception Handling | `exceptions/GlobalExceptionHandler.java` | @ControllerAdvice, @ExceptionHandler |
