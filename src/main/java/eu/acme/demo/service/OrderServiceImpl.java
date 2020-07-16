package eu.acme.demo.service;

import eu.acme.demo.domain.Order;
import eu.acme.demo.domain.OrderItem;
import eu.acme.demo.repository.OrderItemRepository;
import eu.acme.demo.web.dto.OrderDto;
import eu.acme.demo.web.dto.OrderItemDto;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class OrderServiceImpl implements OrderService {

    private final OrderItemRepository orderItemRepository;
    ModelMapper modelMapper = new ModelMapper();

    public OrderServiceImpl(OrderItemRepository orderItemRepository) {
        this.orderItemRepository = orderItemRepository;
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
}
