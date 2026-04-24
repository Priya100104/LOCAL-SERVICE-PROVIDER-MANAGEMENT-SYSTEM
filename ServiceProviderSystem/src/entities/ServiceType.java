package entities;

import java.io.Serializable;

public class ServiceType implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private int serviceTypeId;
    private String serviceName;
    private String description;
    private double averageDuration;
    private String basePriceRange;
    
    public ServiceType(int serviceTypeId, String serviceName, String description, 
                      double averageDuration, String basePriceRange) {
        this.serviceTypeId = serviceTypeId;
        this.serviceName = serviceName;
        this.description = description;
        this.averageDuration = averageDuration;
        this.basePriceRange = basePriceRange;
    }
    
    // Getters
    public int getServiceTypeId() { return serviceTypeId; }
    public String getServiceName() { return serviceName; }
    public String getDescription() { return description; }
    public double getAverageDuration() { return averageDuration; }
    public String getBasePriceRange() { return basePriceRange; }
}