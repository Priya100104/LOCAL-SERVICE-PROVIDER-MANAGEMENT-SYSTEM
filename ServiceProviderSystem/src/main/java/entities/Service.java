package entities;

import java.io.Serializable;
import java.time.LocalDateTime;

public class Service implements Serializable {

    private static final long serialVersionUID = 1L;

    private int           serviceId;
    private Provider      provider;
    private Customer      customer;
    private ServiceType   serviceType;
    private String        description;
    private String        address;
    private LocalDateTime scheduledDate;
    private LocalDateTime completedDate;
    private String        status;
    private double        quotedPrice;
    private double        finalPrice;

    public Service(int serviceId, Provider provider, Customer customer, ServiceType serviceType,
                   String description, String address, LocalDateTime scheduledDate) {
        this.serviceId     = serviceId;
        this.provider      = provider;
        this.customer      = customer;
        this.serviceType   = serviceType;
        this.description   = description;
        this.address       = address;
        this.scheduledDate = scheduledDate;
        this.status        = "requested";
        this.quotedPrice   = 0.0;
        this.finalPrice    = 0.0;
    }

    // Getters
    public int           getServiceId()    { return serviceId; }
    public Provider      getProvider()     { return provider; }
    public Customer      getCustomer()     { return customer; }
    public ServiceType   getServiceType()  { return serviceType; }
    public String        getDescription()  { return description; }
    public String        getAddress()      { return address; }
    public LocalDateTime getScheduledDate(){ return scheduledDate; }
    public LocalDateTime getCompletedDate(){ return completedDate; }
    public String        getStatus()       { return status; }
    public double        getQuotedPrice()  { return quotedPrice; }
    public double        getFinalPrice()   { return finalPrice; }

    // Setters
    public void setStatus(String status) {
        this.status = status;
        if (status.equalsIgnoreCase("completed") || status.equalsIgnoreCase("Completed")) {
            this.completedDate = LocalDateTime.now();
        }
    }
    public void setQuotedPrice(double price) { quotedPrice = price; }
    public void setFinalPrice(double price)  { finalPrice  = price; }
}
