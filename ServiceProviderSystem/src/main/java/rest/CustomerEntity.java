package rest;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "customers")
public class CustomerEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column(nullable = false)
    private String email;

    private String phone;
    private String address;
    private LocalDate joinDate;

    public CustomerEntity() {}

    public CustomerEntity(String username, String password, String firstName,
                          String lastName, String email, String phone, String address) {
        this.username  = username;
        this.password  = password;
        this.firstName = firstName;
        this.lastName  = lastName;
        this.email     = email;
        this.phone     = phone;
        this.address   = address;
        this.joinDate  = LocalDate.now();
    }

    public Long      getId()        { return id; }
    public String    getUsername()  { return username; }
    public String    getPassword()  { return password; }
    public String    getFirstName() { return firstName; }
    public String    getLastName()  { return lastName; }
    public String    getEmail()     { return email; }
    public String    getPhone()     { return phone; }
    public String    getAddress()   { return address; }
    public LocalDate getJoinDate()  { return joinDate; }
    public String    getFullName()  { return firstName + " " + lastName; }

    public void setId(Long id)              { this.id        = id; }
    public void setUsername(String u)       { this.username  = u; }
    public void setPassword(String p)       { this.password  = p; }
    public void setFirstName(String f)      { this.firstName = f; }
    public void setLastName(String l)       { this.lastName  = l; }
    public void setEmail(String e)          { this.email     = e; }
    public void setPhone(String p)          { this.phone     = p; }
    public void setAddress(String a)        { this.address   = a; }
    public void setJoinDate(LocalDate d)    { this.joinDate  = d; }
}
