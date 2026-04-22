package rest;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<BookingEntity, Long> {

    List<BookingEntity> findByCustomerIdOrderByScheduledDateDesc(Long customerId);

    List<BookingEntity> findByProviderIdOrderByScheduledDateDesc(Long providerId);

    List<BookingEntity> findByStatus(String status);

    @Query("SELECT b FROM BookingEntity b WHERE b.customer.id = :customerId AND b.status = 'COMPLETED' AND b.rating = 0")
    List<BookingEntity> findUnreviewedCompletedByCustomer(Long customerId);

    @Query("SELECT b FROM BookingEntity b WHERE b.customer.id = :customerId AND b.status = 'COMPLETED'")
    List<BookingEntity> findCompletedByCustomer(Long customerId);
}
