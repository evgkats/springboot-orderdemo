package eu.acme.demo.service;

import eu.acme.demo.domain.Customer;
import eu.acme.demo.domain.Order;
import eu.acme.demo.domain.OrderItem;
import eu.acme.demo.domain.enums.OrderStatus;
import eu.acme.demo.repository.CustomerRepository;
import eu.acme.demo.repository.OrderItemRepository;
import eu.acme.demo.repository.OrderRepository;
import eu.acme.demo.web.dto.OrderDto;
import eu.acme.demo.web.dto.OrderItemDto;
import eu.acme.demo.web.dto.OrderLiteDto;
import eu.acme.demo.web.dto.OrderRequest;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CustomerRepository customerRepository;
    ModelMapper modelMapper = new ModelMapper();

    public OrderServiceImpl(OrderRepository orderRepository, OrderItemRepository orderItemRepository, CustomerRepository customerRepositoryRepository) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.customerRepository = customerRepositoryRepository;
    }

    @Override
    public Order saveOrderDto(OrderRequest orderRequest) {
        Optional<Customer> optionalCustomer = customerRepository.findById(orderRequest.getCustomerId());
        Order order = new Order();
        order.setClientReferenceCode(orderRequest.getClientReferenceCode());
        order.setStatus(OrderStatus.SUBMITTED);
        order.setItemCount(orderRequest.getOrderItemDtoList().size());
        order.setDescription(orderRequest.getDescription());
        order.setItemTotalAmount(BigDecimal.ZERO);
        order.setCustomer(optionalCustomer.isPresent() ? optionalCustomer.get() : null);
        orderRepository.save(order);
        BigDecimal orderSumAmount = BigDecimal.ZERO;
        for (OrderItemDto orderItemDto : orderRequest.getOrderItemDtoList()) {
            OrderItem orderItem = new OrderItem();
            orderItem.setUnits(orderItemDto.getUnits());
            orderItem.setUnitPrice(orderItemDto.getUnitPrice());
            BigDecimal itemSum = orderItem.getUnitPrice().multiply(BigDecimal.valueOf(orderItem.getUnits()));
            orderSumAmount = orderSumAmount.add(itemSum);
            orderItem.setTotalPrice(itemSum);
            orderItem.setOrder(order);
            orderItemRepository.save(orderItem);
        }
        /* update the order's total price */
        order.setItemTotalAmount(orderSumAmount);
        orderRepository.save(order);
        return order;
    }

    @Override
    public OrderDto createOrderDtoFromOrder(Order order) {
        OrderDto orderDto = modelMapper.map(order, OrderDto.class);
        // Get the order items that belong to this order from the db
        List<OrderItem> orderItems = orderItemRepository.findByOrder(order);
        List<OrderItemDto> orderItemDtos = new ArrayList<>();
        orderItems.forEach(orderItem -> {
            orderItemDtos.add(modelMapper.map(orderItem, OrderItemDto.class));
        });
        orderDto.setOrderItems(orderItemDtos);
        return orderDto;
    }

    @Override
    public List<OrderLiteDto> fetchOrders() {
        List<OrderLiteDto> orderLiteDtoList = new ArrayList<>();

        orderRepository.findAll().forEach(order -> {
            orderLiteDtoList.add(modelMapper.map(order, OrderLiteDto.class));
        });

        return orderLiteDtoList;
    }

    @Override
    public List<OrderLiteDto> fetchCustomerOrders(Customer customer) {
        List<OrderLiteDto> customerOrdersDto = new ArrayList<>();

        List<Order> customerOrders = orderRepository.findByCustomer(customer);
        customerOrders.forEach(order -> {
            customerOrdersDto.add(createOrderDtoFromOrder(order));
        });

        return customerOrdersDto;
    }

    @Override
    public Optional<Order> findOrderById(UUID orderId) {
        return orderRepository.findById(orderId);
    }

    @Override
    public boolean clientRefExists(String clientReference) {
        Optional<Order> optionalOrder = orderRepository.findByClientReferenceCode(clientReference);
        return optionalOrder.isPresent();
    }

    @Override
    public Customer createCustomer(String firstName, String lastName) {
        Customer customer = new Customer();
        customer.setFirstName(firstName);
        customer.setLastName(lastName);
        customerRepository.save(customer);
        return customer;
    }

    @Override
    public Optional<Customer> getCustomer(UUID customerId) {
        return customerRepository.findById(customerId);
    }
}
