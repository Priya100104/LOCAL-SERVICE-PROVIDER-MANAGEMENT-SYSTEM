package rest;

import exceptions.DuplicateProviderException;
import exceptions.InvalidRatingException;
import exceptions.ProviderNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * ============================================================
 *  CO4 - REST API Controller
 *  Base URL: http://localhost:8080/api/providers
 *
 *  Endpoints:
 *  GET    /api/providers              → get all providers
 *  GET    /api/providers/{id}         → get by ID
 *  GET    /api/providers/top          → top rated verified
 *  GET    /api/providers/type/{type}  → filter by service type
 *  POST   /api/providers              → add new provider
 *  PUT    /api/providers/{id}/verify  → verify provider
 *  PUT    /api/providers/{id}/rating  → add rating
 *  DELETE /api/providers/{id}         → delete provider
 *  GET    /api/providers/stats        → system statistics
 * ============================================================
 */
@RestController
@RequestMapping("/api/providers")
public class ServiceProviderRestController {

    @Autowired
    private ServiceProviderRepository repository;

    // ==================== GET ALL ====================
    @GetMapping
    public ResponseEntity<List<ServiceProviderEntity>> getAllProviders() {
        List<ServiceProviderEntity> providers = repository.findAll();
        return ResponseEntity.ok(providers);
    }

    // ==================== GET BY ID ====================
    @GetMapping("/{id}")
    public ResponseEntity<ServiceProviderEntity> getById(@PathVariable Long id) {
        ServiceProviderEntity provider = repository.findById(id)
            .orElseThrow(() -> new ProviderNotFoundException(id));
        return ResponseEntity.ok(provider);
    }

    // ==================== GET TOP RATED ====================
    @GetMapping("/top")
    public ResponseEntity<List<ServiceProviderEntity>> getTopRated() {
        List<ServiceProviderEntity> top = repository.findTopRatedProviders();
        return ResponseEntity.ok(top);
    }

    // ==================== GET BY SERVICE TYPE ====================
    @GetMapping("/type/{serviceType}")
    public ResponseEntity<List<ServiceProviderEntity>> getByServiceType(@PathVariable String serviceType) {
        List<ServiceProviderEntity> providers = repository.findVerifiedByServiceType(serviceType);
        if (providers.isEmpty()) {
            throw new ProviderNotFoundException("No verified providers found for service type: " + serviceType);
        }
        return ResponseEntity.ok(providers);
    }

    // ==================== SEARCH BY NAME ====================
    @GetMapping("/search")
    public ResponseEntity<List<ServiceProviderEntity>> searchByName(@RequestParam String keyword) {
        List<ServiceProviderEntity> results = repository.findByBusinessNameContainingIgnoreCase(keyword);
        return ResponseEntity.ok(results);
    }

    // ==================== CREATE NEW PROVIDER ====================
    @PostMapping
    public ResponseEntity<ServiceProviderEntity> createProvider(@RequestBody ServiceProviderEntity provider) {
        // Check for duplicate username
        boolean duplicate = repository.findAll().stream()
            .anyMatch(p -> p.getUsername().equalsIgnoreCase(provider.getUsername()));
        if (duplicate) {
            throw new DuplicateProviderException(provider.getUsername());
        }
        provider.setCreatedAt(LocalDate.now());
        provider.setAverageRating(0.0);
        provider.setVerified(false);
        ServiceProviderEntity saved = repository.save(provider);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    // ==================== VERIFY PROVIDER ====================
    @PutMapping("/{id}/verify")
    public ResponseEntity<ServiceProviderEntity> verifyProvider(@PathVariable Long id) {
        ServiceProviderEntity provider = repository.findById(id)
            .orElseThrow(() -> new ProviderNotFoundException(id));
        provider.setVerified(true);
        ServiceProviderEntity updated = repository.save(provider);
        return ResponseEntity.ok(updated);
    }

    // ==================== ADD RATING ====================
    @PutMapping("/{id}/rating")
    public ResponseEntity<ServiceProviderEntity> addRating(@PathVariable Long id,
                                                            @RequestBody Map<String, Integer> body) {
        int rating = body.getOrDefault("rating", 0);
        if (rating < 1 || rating > 5) {
            throw new InvalidRatingException(rating);
        }
        ServiceProviderEntity provider = repository.findById(id)
            .orElseThrow(() -> new ProviderNotFoundException(id));

        // Recalculate average (simple approach: store as current avg)
        double currentAvg = provider.getAverageRating();
        double newAvg = currentAvg == 0.0 ? rating : (currentAvg + rating) / 2.0;
        provider.setAverageRating(Math.round(newAvg * 10.0) / 10.0);
        ServiceProviderEntity updated = repository.save(provider);
        return ResponseEntity.ok(updated);
    }

    // ==================== UPDATE PROVIDER ====================
    @PutMapping("/{id}")
    public ResponseEntity<ServiceProviderEntity> updateProvider(@PathVariable Long id,
                                                                  @RequestBody ServiceProviderEntity updated) {
        ServiceProviderEntity existing = repository.findById(id)
            .orElseThrow(() -> new ProviderNotFoundException(id));
        if (updated.getBusinessName() != null) existing.setBusinessName(updated.getBusinessName());
        if (updated.getPhone()        != null) existing.setPhone(updated.getPhone());
        if (updated.getEmail()        != null) existing.setEmail(updated.getEmail());
        if (updated.getHourlyRate()   > 0)     existing.setHourlyRate(updated.getHourlyRate());
        return ResponseEntity.ok(repository.save(existing));
    }

    // ==================== DELETE PROVIDER ====================
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteProvider(@PathVariable Long id) {
        if (!repository.existsById(id)) {
            throw new ProviderNotFoundException(id);
        }
        repository.deleteById(id);
        return ResponseEntity.ok(Map.of("message", "Provider " + id + " deleted successfully."));
    }

    // ==================== STATS ====================
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        List<ServiceProviderEntity> all = repository.findAll();
        long total    = all.size();
        long verified = all.stream().filter(ServiceProviderEntity::isVerified).count();
        double avgRating = all.stream()
            .filter(p -> p.getAverageRating() > 0)
            .mapToDouble(ServiceProviderEntity::getAverageRating)
            .average().orElse(0.0);

        Map<String, Object> stats = Map.of(
            "totalProviders",    total,
            "verifiedProviders", verified,
            "pendingVerification", total - verified,
            "averageRating",     Math.round(avgRating * 10.0) / 10.0
        );
        return ResponseEntity.ok(stats);
    }
}
