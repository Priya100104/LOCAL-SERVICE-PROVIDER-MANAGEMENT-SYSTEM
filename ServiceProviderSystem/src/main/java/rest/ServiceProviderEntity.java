package rest;

import jakarta.persistence.*;
import java.time.LocalDate;

/**
 * ============================================================
 *  CO5 - JPA Entity: ServiceProvider
 *  Uses @Entity, @Id, @GeneratedValue, @Column annotations
 *  Stored in H2 in-memory database (auto-created by Spring Boot)
 * ============================================================
 */
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

    @Column(nullable = false)
    private String username;

    @Column(nullable = false)
    private String email;

    private String phone;

    private int yearsOfExperience;

    private double averageRating;

    private boolean isVerified;

    private double hourlyRate;

    @Column(name = "created_at")
    private LocalDate createdAt;

    // ==================== CONSTRUCTORS ====================
    public ServiceProviderEntity() {}

    public ServiceProviderEntity(String businessName, String serviceType, String username,
                                  String email, String phone, int yearsOfExperience,
                                  double hourlyRate) {
        this.businessName      = businessName;
        this.serviceType       = serviceType;
        this.username          = username;
        this.email             = email;
        this.phone             = phone;
        this.yearsOfExperience = yearsOfExperience;
        this.hourlyRate        = hourlyRate;
        this.averageRating     = 0.0;
        this.isVerified        = false;
        this.createdAt         = LocalDate.now();
    }

    // ==================== GETTERS / SETTERS ====================
    public Long   getId()               { return id; }
    public String getBusinessName()     { return businessName; }
    public String getServiceType()      { return serviceType; }
    public String getUsername()         { return username; }
    public String getEmail()            { return email; }
    public String getPhone()            { return phone; }
    public int    getYearsOfExperience(){ return yearsOfExperience; }
    public double getAverageRating()    { return averageRating; }
    public boolean isVerified()         { return isVerified; }
    public double getHourlyRate()       { return hourlyRate; }
    public LocalDate getCreatedAt()     { return createdAt; }

    public void setId(Long id)                      { this.id               = id; }
    public void setBusinessName(String n)           { this.businessName     = n; }
    public void setServiceType(String t)            { this.serviceType      = t; }
    public void setUsername(String u)               { this.username         = u; }
    public void setEmail(String e)                  { this.email            = e; }
    public void setPhone(String p)                  { this.phone            = p; }
    public void setYearsOfExperience(int y)         { this.yearsOfExperience= y; }
    public void setAverageRating(double r)          { this.averageRating    = r; }
    public void setVerified(boolean v)              { this.isVerified       = v; }
    public void setHourlyRate(double r)             { this.hourlyRate       = r; }
    public void setCreatedAt(LocalDate d)           { this.createdAt        = d; }
}
