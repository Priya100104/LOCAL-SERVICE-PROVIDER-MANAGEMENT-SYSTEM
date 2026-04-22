package rest;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * ============================================================
 *  CO5 - JPA Repository
 *  Spring Data JPA - no SQL needed, just method names!
 *  Extends JpaRepository gives: save, findAll, findById,
 *  deleteById, count, existsById - all automatically!
 * ============================================================
 */
@Repository
public interface ServiceProviderRepository extends JpaRepository<ServiceProviderEntity, Long> {

    // Custom query methods - Spring auto-generates SQL from method name
    List<ServiceProviderEntity> findByServiceType(String serviceType);

    List<ServiceProviderEntity> findByIsVerifiedTrue();

    List<ServiceProviderEntity> findByAverageRatingGreaterThanEqual(double minRating);

    List<ServiceProviderEntity> findByHourlyRateLessThanEqual(double maxRate);

    List<ServiceProviderEntity> findByBusinessNameContainingIgnoreCase(String keyword);

    // Custom JPQL query
    @Query("SELECT p FROM ServiceProviderEntity p WHERE p.isVerified = true ORDER BY p.averageRating DESC")
    List<ServiceProviderEntity> findTopRatedProviders();

    @Query("SELECT p FROM ServiceProviderEntity p WHERE p.serviceType = :serviceType AND p.isVerified = true ORDER BY p.averageRating DESC")
    List<ServiceProviderEntity> findVerifiedByServiceType(String serviceType);
}
