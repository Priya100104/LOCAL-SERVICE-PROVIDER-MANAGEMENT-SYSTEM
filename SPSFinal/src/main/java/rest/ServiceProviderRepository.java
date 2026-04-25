package rest;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ServiceProviderRepository extends JpaRepository<ServiceProviderEntity, Long> {
    List<ServiceProviderEntity> findByServiceType(String serviceType);
    List<ServiceProviderEntity> findByIsVerifiedTrue();
    List<ServiceProviderEntity> findByBusinessNameContainingIgnoreCase(String keyword);
    Optional<ServiceProviderEntity> findByUsername(String username);
    boolean existsByUsername(String username);

    @Query("SELECT p FROM ServiceProviderEntity p WHERE p.isVerified = true ORDER BY p.averageRating DESC")
    List<ServiceProviderEntity> findTopRatedProviders();

    @Query("SELECT p FROM ServiceProviderEntity p WHERE p.serviceType = :serviceType AND p.isVerified = true ORDER BY p.averageRating DESC")
    List<ServiceProviderEntity> findVerifiedByServiceType(String serviceType);
}
