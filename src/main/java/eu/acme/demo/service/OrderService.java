package eu.acme.demo.service;

import eu.acme.demo.domain.Customer;
import eu.acme.demo.domain.Order;
import eu.acme.demo.web.dto.OrderDto;
import eu.acme.demo.web.dto.OrderLiteDto;
import eu.acme.demo.web.dto.OrderRequest;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrderService {

    Order saveOrderDto(OrderRequest orderRequest);

    OrderDto createOrderDtoFromOrder(Order order);

    List<OrderLiteDto> fetchOrders();

    List<OrderLiteDto> fetchCustomerOrders(Customer customer);

    Optional<Order> findOrderById(UUID orderId);

    boolean clientRefExists(String clientReference);

    Customer createCustomer(String firstName, String lastName);

    Optional<Customer> getCustomer(UUID customerId);
}
