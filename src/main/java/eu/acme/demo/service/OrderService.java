package eu.acme.demo.service;

import eu.acme.demo.domain.Order;
import eu.acme.demo.web.dto.OrderDto;

public interface OrderService {
    OrderDto createOrderDtoFromOrder(Order order);
}
