package entities;

import java.io.Serializable;
import java.time.LocalDate;

public class Review implements Serializable {

    private static final long serialVersionUID = 1L;

    private int       reviewId;
    private Customer  customer;
    private Provider  provider;
    private Service   service;
    private int       rating;
    private String    title;
    private String    comment;
    private LocalDate reviewDate;

    public Review(int reviewId, Customer customer, Provider provider, Service service,
                  int rating, String title, String comment) {
        this.reviewId   = reviewId;
        this.customer   = customer;
        this.provider   = provider;
        this.service    = service;
        this.rating     = rating;
        this.title      = title;
        this.comment    = comment;
        this.reviewDate = LocalDate.now();
    }

    public int      getReviewId()  { return reviewId; }
    public Customer getCustomer()  { return customer; }
    public Provider getProvider()  { return provider; }
    public Service  getService()   { return service; }
    public int      getRating()    { return rating; }
    public String   getTitle()     { return title; }
    public String   getComment()   { return comment; }
    public LocalDate getReviewDate(){ return reviewDate; }
}
