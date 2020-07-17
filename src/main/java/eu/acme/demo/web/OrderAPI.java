package eu.acme.demo.web;

import eu.acme.demo.domain.Customer;
import eu.acme.demo.domain.Order;
import eu.acme.demo.service.OrderService;
import eu.acme.demo.web.dto.OrderDto;
import eu.acme.demo.web.dto.OrderLiteDto;
import eu.acme.demo.web.dto.OrderRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/orders")
public class OrderAPI {

    private final OrderService orderService;

    public OrderAPI(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    public List<OrderLiteDto> fetchOrders() {
        return orderService.fetchOrders();
    }

    @GetMapping("/c/{customerId}")
    public List<OrderLiteDto> fetchCustomerOrders(@PathVariable UUID customerId) {
        Optional<Customer> optionalCustomer = orderService.getCustomer(customerId);
        if (optionalCustomer.isPresent()) {
            return orderService.fetchCustomerOrders(optionalCustomer.get());
        } else {
            //TODO: add a proper payload that contains an error code and an error message
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The given customerId does not exist in the db!");
        }
    }

    @GetMapping("/{orderId}")
    public OrderDto fetchOrder(@PathVariable UUID orderId) {
        Optional<Order> optionalOrder = orderService.findOrderById(orderId);

        if (optionalOrder.isPresent()) {
            return orderService.createOrderDtoFromOrder(optionalOrder.get());
        } else {
            //TODO: add a proper payload that contains an error code and an error message
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The given orderId does not exist in the db!");
        }
    }

    @PostMapping
    public OrderDto submitOrder(@RequestBody OrderRequest orderRequest) {
        if (orderService.clientRefExists(orderRequest.getClientReferenceCode())) {
            //TODO: add a proper payload that contains an error code and an error message
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The given client reference code already exists in the db");
        } else {
            Order order = orderService.saveOrderDto(orderRequest);
            return orderService.createOrderDtoFromOrder(order);
        }
    }

}
