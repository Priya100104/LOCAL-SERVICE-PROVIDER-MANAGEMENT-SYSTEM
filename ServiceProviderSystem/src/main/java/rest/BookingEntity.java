package rest;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "bookings")
public class BookingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "customer_id", nullable = false)
    private CustomerEntity customer;

    @ManyToOne
    @JoinColumn(name = "provider_id", nullable = false)
    private ServiceProviderEntity provider;

    @Column(nullable = false)
    private String serviceType;

    private String description;
    private String address;

    @Column(nullable = false)
    private LocalDateTime scheduledDate;

    private LocalDateTime completedDate;

    // Status: PENDING, ACCEPTED, IN_PROGRESS, COMPLETED, CANCELLED
    @Column(nullable = false)
    private String status = "PENDING";

    private double finalPrice = 0.0;

    // Rating given after completion (1-5), 0 = not rated yet
    private int rating = 0;
    private String reviewTitle;
    private String reviewComment;

    public BookingEntity() {}

    public BookingEntity(CustomerEntity customer, ServiceProviderEntity provider,
                         String serviceType, String description,
                         String address, LocalDateTime scheduledDate) {
        this.customer      = customer;
        this.provider      = provider;
        this.serviceType   = serviceType;
        this.description   = description;
        this.address       = address;
        this.scheduledDate = scheduledDate;
        this.status        = "PENDING";
    }

    // Getters
    public Long                  getId()            { return id; }
    public CustomerEntity        getCustomer()      { return customer; }
    public ServiceProviderEntity getProvider()      { return provider; }
    public String                getServiceType()   { return serviceType; }
    public String                getDescription()   { return description; }
    public String                getAddress()       { return address; }
    public LocalDateTime         getScheduledDate() { return scheduledDate; }
    public LocalDateTime         getCompletedDate() { return completedDate; }
    public String                getStatus()        { return status; }
    public double                getFinalPrice()    { return finalPrice; }
    public int                   getRating()        { return rating; }
    public String                getReviewTitle()   { return reviewTitle; }
    public String                getReviewComment() { return reviewComment; }
    public boolean               isReviewed()       { return rating > 0; }

    // Setters
    public void setId(Long id)                        { this.id            = id; }
    public void setCustomer(CustomerEntity c)         { this.customer      = c; }
    public void setProvider(ServiceProviderEntity p)  { this.provider      = p; }
    public void setServiceType(String s)              { this.serviceType   = s; }
    public void setDescription(String d)              { this.description   = d; }
    public void setAddress(String a)                  { this.address       = a; }
    public void setScheduledDate(LocalDateTime d)     { this.scheduledDate = d; }
    public void setFinalPrice(double p)               { this.finalPrice    = p; }
    public void setRating(int r)                      { this.rating        = r; }
    public void setReviewTitle(String t)              { this.reviewTitle   = t; }
    public void setReviewComment(String c)            { this.reviewComment = c; }

    public void setStatus(String status) {
        this.status = status;
        if ("COMPLETED".equals(status)) {
            this.completedDate = LocalDateTime.now();
        }
    }
}
