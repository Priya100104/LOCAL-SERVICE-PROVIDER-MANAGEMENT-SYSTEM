package rest;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<BookingEntity, Long> {

    List<BookingEntity> findByCustomer(CustomerEntity customer);
    List<BookingEntity> findByProvider(ServiceProviderEntity provider);
    List<BookingEntity> findByCustomerAndStatus(CustomerEntity customer, String status);

    // Provider: bookings waiting for action (PENDING)
    List<BookingEntity> findByProviderAndStatus(ServiceProviderEntity provider, String status);

    // Customer: bookings where work is done, waiting for confirmation
    List<BookingEntity> findByCustomerAndStatusOrderByBookingDateDesc(CustomerEntity customer, String status);

    @Query("SELECT b FROM BookingEntity b WHERE b.provider = :provider AND b.status = 'COMPLETED' AND b.rating IS NOT NULL")
    List<BookingEntity> findCompletedWithRatingByProvider(ServiceProviderEntity provider);
}
