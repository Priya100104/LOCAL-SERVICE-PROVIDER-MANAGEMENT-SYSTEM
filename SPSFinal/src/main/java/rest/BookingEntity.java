package rest;

import jakarta.persistence.*;
import java.time.LocalDate;

/**
 * Booking Status Flow:
 *   PENDING    → Customer booked, waiting for provider
 *   ACCEPTED   → Provider accepted the booking
 *   IN_PROGRESS→ Provider started the work
 *   WORK_DONE  → Provider marked work as done, waiting for customer to confirm
 *   COMPLETED  → Customer confirmed service is done (rating unlocked)
 *   CANCELLED  → Cancelled by either party
 */
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

    @Column(nullable = false)
    private LocalDate bookingDate;

    private String notes;

    @Column(nullable = false)
    private String status;

    // Set when provider marks WORK_DONE
    private LocalDate workDoneDate;

    // Set when customer confirms COMPLETED
    private LocalDate completedDate;

    // Rating + feedback — only after COMPLETED
    private Integer rating;
    private String  feedback;

    public BookingEntity() {}

    public BookingEntity(CustomerEntity customer, ServiceProviderEntity provider,
                         String serviceType, LocalDate bookingDate, String notes) {
        this.customer    = customer;
        this.provider    = provider;
        this.serviceType = serviceType;
        this.bookingDate = bookingDate;
        this.notes       = notes;
        this.status      = "PENDING";
    }

    // Getters
    public Long                  getId()           { return id; }
    public CustomerEntity        getCustomer()     { return customer; }
    public ServiceProviderEntity getProvider()     { return provider; }
    public String                getServiceType()  { return serviceType; }
    public LocalDate             getBookingDate()  { return bookingDate; }
    public String                getNotes()        { return notes; }
    public String                getStatus()       { return status; }
    public LocalDate             getWorkDoneDate() { return workDoneDate; }
    public LocalDate             getCompletedDate(){ return completedDate; }
    public Integer               getRating()       { return rating; }
    public String                getFeedback()     { return feedback; }

    // Setters
    public void setStatus(String s)      { this.status       = s; }
    public void setWorkDoneDate(LocalDate d) { this.workDoneDate = d; }
    public void setCompletedDate(LocalDate d){ this.completedDate = d; }
    public void setRating(Integer r)     { this.rating       = r; }
    public void setFeedback(String f)    { this.feedback     = f; }

    // Convenience helpers used in templates
    public boolean isRatable() {
        return "COMPLETED".equals(status) && rating == null;
    }
    public boolean isReviewed() {
        return rating != null;
    }
}
