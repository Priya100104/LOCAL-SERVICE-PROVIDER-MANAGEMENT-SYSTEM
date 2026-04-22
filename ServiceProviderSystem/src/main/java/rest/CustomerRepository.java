package rest;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<CustomerEntity, Long> {
    Optional<CustomerEntity> findByUsername(String username);
    boolean existsByUsername(String username);
    Optional<CustomerEntity> findByUsernameAndPassword(String username, String password);
}
