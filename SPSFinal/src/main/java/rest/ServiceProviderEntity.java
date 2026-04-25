package rest;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "service_providers")
public class ServiceProviderEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String businessName;

    @Column(nullable = false)
    private String serviceType;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;          // ← NEW: provider logs in with this

    @Column(nullable = false)
    private String email;

    private String phone;
    private int    yearsOfExperience;
    private double averageRating;
    private boolean isVerified;
    private double  hourlyRate;

    @Column(name = "created_at")
    private LocalDate createdAt;

    public ServiceProviderEntity() {}

    // Constructor used by seeder (no password)
    public ServiceProviderEntity(String businessName, String serviceType, String username,
                                  String email, String phone, int yearsOfExperience, double hourlyRate) {
        this(businessName, serviceType, username, "pass123", email, phone, yearsOfExperience, hourlyRate);
    }

    // Full constructor with password
    public ServiceProviderEntity(String businessName, String serviceType, String username,
                                  String password, String email, String phone,
                                  int yearsOfExperience, double hourlyRate) {
        this.businessName      = businessName;
        this.serviceType       = serviceType;
        this.username          = username;
        this.password          = password;
        this.email             = email;
        this.phone             = phone;
        this.yearsOfExperience = yearsOfExperience;
        this.hourlyRate        = hourlyRate;
        this.averageRating     = 0.0;
        this.isVerified        = false;
        this.createdAt         = LocalDate.now();
    }

    public Long      getId()                { return id; }
    public String    getBusinessName()      { return businessName; }
    public String    getServiceType()       { return serviceType; }
    public String    getUsername()          { return username; }
    public String    getPassword()          { return password; }
    public String    getEmail()             { return email; }
    public String    getPhone()             { return phone; }
    public int       getYearsOfExperience() { return yearsOfExperience; }
    public double    getAverageRating()     { return averageRating; }
    public boolean   isVerified()           { return isVerified; }
    public double    getHourlyRate()        { return hourlyRate; }
    public LocalDate getCreatedAt()         { return createdAt; }

    public void setId(Long id)                   { this.id               = id; }
    public void setBusinessName(String n)        { this.businessName     = n; }
    public void setServiceType(String t)         { this.serviceType      = t; }
    public void setUsername(String u)            { this.username         = u; }
    public void setPassword(String p)            { this.password         = p; }
    public void setEmail(String e)               { this.email            = e; }
    public void setPhone(String p)               { this.phone            = p; }
    public void setYearsOfExperience(int y)      { this.yearsOfExperience= y; }
    public void setAverageRating(double r)       { this.averageRating    = r; }
    public void setVerified(boolean v)           { this.isVerified       = v; }
    public void setHourlyRate(double r)          { this.hourlyRate       = r; }
    public void setCreatedAt(LocalDate d)        { this.createdAt        = d; }
}
