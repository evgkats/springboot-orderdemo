package eu.acme.demo;


import eu.acme.demo.domain.Customer;
import eu.acme.demo.domain.Order;
import eu.acme.demo.domain.OrderItem;
import eu.acme.demo.domain.enums.OrderStatus;
import eu.acme.demo.repository.CustomerRepository;
import eu.acme.demo.repository.OrderItemRepository;
import eu.acme.demo.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.Assert;

import java.math.BigDecimal;
import java.util.UUID;

@SpringBootTest
public class OrderDataTests {

    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private OrderItemRepository orderItemRepository;
    @Autowired
    private CustomerRepository customerRepository;

    @Test
    public void testCreateOrder() {
        Order o = new Order();
        o.setStatus(OrderStatus.SUBMITTED);
        o.setClientReferenceCode("ORDER-1");
        o.setDescription("first order");
        o.setItemCount(10);
        o.setItemTotalAmount(BigDecimal.valueOf(100.23));
        orderRepository.save(o);

        Assert.isTrue(orderRepository.findById(o.getId()).isPresent(), "order not found");
        Assert.isTrue(!orderRepository.findById(UUID.randomUUID()).isPresent(), "non existing order found");
    }

    @Test
    public void testCreateOrderWithOrderItem() {
        Order o = new Order();
        o.setStatus(OrderStatus.SUBMITTED);
        o.setClientReferenceCode("ORDER-2");
        o.setDescription("second order");
        o.setItemCount(1);
        o.setItemTotalAmount(BigDecimal.valueOf(100.23));
        orderRepository.save(o);

        OrderItem orderItem = new OrderItem();
        orderItem.setOrder(o);
        orderItem.setUnits(2);
        orderItem.setUnitPrice(BigDecimal.valueOf(2.3));
        orderItem.setTotalPrice(BigDecimal.valueOf(4.6));
        orderItemRepository.save(orderItem);

        Assert.isTrue(orderItemRepository.findById(orderItem.getId()).isPresent(), "OrderItem not found");
        Assert.isTrue(!orderItemRepository.findById(UUID.randomUUID()).isPresent(), "non existing OrderItem found");
    }

    @Test
    public void testCreateCustomer() {
        Customer customer = new Customer();
        customer.setFirstName("John");
        customer.setLastName("Doe");
        customerRepository.save(customer);

        Assert.isTrue(customerRepository.findById(customer.getId()).isPresent(), "order not found");
        Assert.isTrue(!customerRepository.findById(UUID.randomUUID()).isPresent(), "non existing order found");
    }

    @Test
    public void testCreateOrderWithOrderItemAndCustomer() {
        Customer customer = new Customer();
        customer.setFirstName("John");
        customer.setLastName("Doe");
        customerRepository.save(customer);

        Order o = new Order();
        o.setStatus(OrderStatus.SUBMITTED);
        o.setClientReferenceCode("ORDER-2");
        o.setDescription("second order");
        o.setItemCount(1);
        o.setItemTotalAmount(BigDecimal.valueOf(100.23));
        o.setCustomer(customer);
        orderRepository.save(o);

        OrderItem orderItem = new OrderItem();
        orderItem.setOrder(o);
        orderItem.setUnits(2);
        orderItem.setUnitPrice(BigDecimal.valueOf(2.3));
        orderItem.setTotalPrice(BigDecimal.valueOf(4.6));
        orderItemRepository.save(orderItem);

        Assert.isTrue(orderRepository.findByCustomer(customer).size() > 0, "Orders for customer not found");
    }

}
