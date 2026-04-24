package entities;

import java.io.Serializable;
import java.time.LocalDateTime;

public class Appointment implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private int appointmentId;
    private Service service;
    private LocalDateTime appointmentTime;
    private String status;
    private String notes;
    
    public Appointment(int appointmentId, Service service, LocalDateTime appointmentTime) {
        this.appointmentId = appointmentId;
        this.service = service;
        this.appointmentTime = appointmentTime;
        this.status = "scheduled";
        this.notes = "";
    }
    
    // Getters
    public int getAppointmentId() { return appointmentId; }
    public Service getService() { return service; }
    public LocalDateTime getAppointmentTime() { return appointmentTime; }
    public String getStatus() { return status; }
    public String getNotes() { return notes; }
    
    // Setters
    public void setStatus(String status) { this.status = status; }
    public void setNotes(String notes) { this.notes = notes; }
}