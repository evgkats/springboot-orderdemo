package eu.acme.demo.web;

import eu.acme.demo.domain.Order;
import eu.acme.demo.domain.OrderItem;
import eu.acme.demo.domain.enums.OrderStatus;
import eu.acme.demo.repository.OrderItemRepository;
import eu.acme.demo.repository.OrderRepository;
import eu.acme.demo.service.OrderService;
import eu.acme.demo.web.dto.OrderDto;
import eu.acme.demo.web.dto.OrderLiteDto;
import eu.acme.demo.web.dto.OrderRequest;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/orders")
public class OrderAPI {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderService orderService;

    ModelMapper modelMapper = new ModelMapper();

    public OrderAPI(OrderRepository orderRepository, OrderItemRepository orderItemRepository, OrderService orderService) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.orderService = orderService;
    }

    @GetMapping
    public List<OrderLiteDto> fetchOrders() {
        List<OrderLiteDto> orderLiteDtoList = new ArrayList<>();

        orderRepository.findAll().forEach(order -> {
            orderLiteDtoList.add(modelMapper.map(order, OrderLiteDto.class));
        });

        return orderLiteDtoList;
    }

    @GetMapping("/{orderId}")
    public OrderDto fetchOrder(@PathVariable UUID orderId) {
        Optional<Order> optionalOrder = orderRepository.findById(orderId);

        if (optionalOrder.isPresent()) {
            return orderService.createOrderDtoFromOrder(optionalOrder.get());
        } else {
            //TODO: add a proper payload that contains an error code and an error message
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The given orderId does not exist in the db!");
        }
    }

    @PostMapping
    public OrderDto submitOrder(@RequestBody OrderRequest orderRequest) {
        String clientReferenceCode = orderRequest.getClientReferenceCode();
        Optional<Order> optionalOrder = orderRepository.findByClientReferenceCode(clientReferenceCode);
        if (optionalOrder.isPresent()) {
            //TODO: add a proper payload that contains an error code and an error message
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The given client reference code already exists in the db");
        } else {
            Order order = new Order();
            order.setClientReferenceCode(clientReferenceCode);
            order.setStatus(OrderStatus.SUBMITTED);
            order.setItemCount(orderRequest.getOrderItemDtoList().size());
            order.setDescription(orderRequest.getDescription());
            order.setItemTotalAmount(BigDecimal.ZERO);
            orderRepository.save(order);
            orderRequest.getOrderItemDtoList().forEach(orderItemDto -> {
                OrderItem orderItem = new OrderItem();
                orderItem.setUnits(orderItemDto.getUnits());
                orderItem.setUnitPrice(orderItemDto.getUnitPrice());
                orderItem.setTotalPrice(orderItemDto.getTotalPrice());
                orderItem.setOrder(order);
                orderItemRepository.save(orderItem);
            });
            /* return saved order */
            return orderService.createOrderDtoFromOrder(order);
        }
    }

}
