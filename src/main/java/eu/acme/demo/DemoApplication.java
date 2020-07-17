package eu.acme.demo;

import eu.acme.demo.domain.Order;
import eu.acme.demo.domain.OrderItem;
import eu.acme.demo.domain.enums.OrderStatus;
import eu.acme.demo.repository.OrderItemRepository;
import eu.acme.demo.repository.OrderRepository;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.math.BigDecimal;

@SpringBootApplication
@EnableJpaAuditing
public class DemoApplication {

    private static OrderRepository orderRepository;
    private static OrderItemRepository orderItemRepository;

    public DemoApplication(OrderRepository orderRepository, OrderItemRepository orderItemRepository) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
    }

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);

        /* Create a sample order for testing purposes */
        Order order = new Order();
        order.setClientReferenceCode("0123");
        order.setDescription("test");
        order.setItemCount(5);
        order.setItemTotalAmount(BigDecimal.TEN);
        order.setStatus(OrderStatus.SUBMITTED);
        orderRepository.save(order);

        /* Create an OrderItem for testing purposes */
        OrderItem orderItem = new OrderItem();
        orderItem.setOrder(order);
        orderItem.setTotalPrice(BigDecimal.TEN);
        orderItem.setUnitPrice(BigDecimal.ONE);
        orderItem.setUnits(2);
        orderItemRepository.save(orderItem);
    }

}
