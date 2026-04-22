package web;

import rest.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import java.time.LocalDate;

@SpringBootApplication
@ComponentScan(basePackages = {"web", "rest", "exceptions"})
@EntityScan(basePackages = "rest")
@EnableJpaRepositories(basePackages = "rest")
public class SpringApp {

    public static void main(String[] args) {
        SpringApplication.run(SpringApp.class, args);
    }

    @Bean
    CommandLineRunner seedData(ServiceProviderRepository providerRepo,
                               CustomerRepository customerRepo) {
        return args -> {
            // Seed providers
            if (providerRepo.count() == 0) {
                ServiceProviderEntity p1 = new ServiceProviderEntity("Raj Plumbing",      "Plumbing",   "plumber_raj",   "raj@service.com",   "9876543213", 8,  400.0);
                ServiceProviderEntity p2 = new ServiceProviderEntity("Shyam Electricals", "Electrical", "electric_shyam","shyam@service.com", "9876543214", 5,  350.0);
                ServiceProviderEntity p3 = new ServiceProviderEntity("CleanPro Services", "Cleaning",   "cleanpro",      "clean@service.com", "9876543215", 3,  250.0);
                ServiceProviderEntity p4 = new ServiceProviderEntity("Karthik Carpentry", "Carpentry",  "karthik_wood",  "karthik@service.com","9876543216", 10, 500.0);
                ServiceProviderEntity p5 = new ServiceProviderEntity("ColorMaster",       "Painting",   "colormaster",   "color@service.com", "9876543217", 6,  300.0);
                p1.setVerified(true); p1.setAverageRating(4.5);
                p2.setVerified(true); p2.setAverageRating(4.2);
                p3.setVerified(true); p3.setAverageRating(4.0);
                p4.setVerified(false);
                p5.setVerified(true); p5.setAverageRating(3.8);
                p1.setCreatedAt(LocalDate.now()); p2.setCreatedAt(LocalDate.now());
                p3.setCreatedAt(LocalDate.now()); p4.setCreatedAt(LocalDate.now());
                p5.setCreatedAt(LocalDate.now());
                providerRepo.save(p1); providerRepo.save(p2); providerRepo.save(p3);
                providerRepo.save(p4); providerRepo.save(p5);
                System.out.println("✅ Sample providers seeded!");
            }
            // Seed a demo customer
            if (customerRepo.count() == 0) {
                customerRepo.save(new CustomerEntity("john_doe","pass123","John","Doe","john@email.com","9876543211","123 Main St"));
                customerRepo.save(new CustomerEntity("jane_smith","pass123","Jane","Smith","jane@email.com","9876543212","456 Oak Ave"));
                System.out.println("✅ Sample customers seeded!");
            }
            System.out.println("🌐 Web UI     : http://localhost:8080/web/");
            System.out.println("🔐 Admin Login: admin / admin123");
            System.out.println("👤 Customer   : john_doe / pass123");
            System.out.println("📡 REST API   : http://localhost:8080/api/providers");
            System.out.println("🗄️  H2 Console : http://localhost:8080/h2-console");
        };
    }
}
