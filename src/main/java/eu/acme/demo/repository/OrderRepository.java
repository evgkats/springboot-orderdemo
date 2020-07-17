package eu.acme.demo.repository;

import eu.acme.demo.domain.Customer;
import eu.acme.demo.domain.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, UUID> {
    Optional<Order> findByClientReferenceCode(String refCode);

    List<Order> findByCustomer(Customer customer);
}
