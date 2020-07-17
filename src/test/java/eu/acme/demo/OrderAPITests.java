package eu.acme.demo;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.acme.demo.domain.Customer;
import eu.acme.demo.domain.Order;
import eu.acme.demo.service.OrderService;
import eu.acme.demo.web.dto.OrderDto;
import eu.acme.demo.web.dto.OrderItemDto;
import eu.acme.demo.web.dto.OrderLiteDto;
import eu.acme.demo.web.dto.OrderRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class OrderAPITests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private OrderService orderService;

    private Customer customer;

    @BeforeEach
    public void init() {
        customer = orderService.createCustomer("John", "Doe");
    }

    @Test
    void testOrderAPI() throws Exception {
        // create order request
        OrderRequest orderRequest = createOrderDtoTestObject("001");
        // convert to json string using Jackson Object Mapper
        String orderRequestAsString = objectMapper.writeValueAsString(orderRequest);
        MvcResult orderResult = postOrderMvcResult(orderRequestAsString, status().isOk());

        // retrieve order dto from response
        String orderResponseAsString = orderResult.getResponse().getContentAsString();
        // convert to OrderDto using Jackson Object Mapper
        OrderDto orderDto = objectMapper.readValue(orderResponseAsString, OrderDto.class);

        // verify OrderDto response object is correct
        assertEquals(orderDto.getClientReferenceCode(), orderRequest.getClientReferenceCode());
        assertEquals(orderDto.getDescription(), orderRequest.getDescription());
        assertEquals(orderDto.getOrderItems().size(), orderRequest.getOrderItemDtoList().size());
        assertEquals(orderDto.getOrderItems().get(0).getUnitPrice(), orderRequest.getOrderItemDtoList().get(0).getUnitPrice());
        assertEquals(orderDto.getOrderItems().get(0).getUnits(), orderRequest.getOrderItemDtoList().get(0).getUnits());
    }

    OrderRequest createOrderDtoTestObject(String clientRefCode) {
        OrderItemDto orderItemDto = new OrderItemDto();
        orderItemDto.setUnitPrice(BigDecimal.valueOf(1.35));
        orderItemDto.setUnits(1);
        orderItemDto.setTotalPrice(orderItemDto.getUnitPrice().multiply(BigDecimal.valueOf(orderItemDto.getUnits())));

        List<OrderItemDto> orderItemDtoList = new ArrayList<>();
        orderItemDtoList.add(orderItemDto);

        OrderRequest orderRequest = new OrderRequest();
        orderRequest.setClientReferenceCode(clientRefCode);
        orderRequest.setDescription("this is a test description");
        orderRequest.setOrderItemDtoList(orderItemDtoList);
        orderRequest.setCustomerId(customer.getId());

        return orderRequest;
    }

    @Test
    void testOrderDoubleSubmission() throws Exception {
        // create order request
        OrderRequest orderRequest = createOrderDtoTestObject("002");
        // convert to json string using Jackson Object Mapper
        String orderRequestAsString = objectMapper.writeValueAsString(orderRequest);
        // submit order request for the first time and expect successful response
        MvcResult orderResult1 = postOrderMvcResult(orderRequestAsString, status().isOk());

        // submit the same order request for the second time and expect 400 error
        MvcResult orderResult2 = postOrderMvcResult(orderRequestAsString, status().isBadRequest());
    }

    private MvcResult postOrderMvcResult(String orderRequestAsString, ResultMatcher status) throws Exception{
        return this.mockMvc.perform(post("http://api.okto-demo.eu/orders")
                .content(orderRequestAsString) // set json string to content param
                .contentType("application/json")
                .accept("application/json"))
                .andExpect(status)
                .andReturn();
    }

    private MvcResult getOrderMvcResult(String urlTemplate, ResultMatcher status) throws Exception{
        return this.mockMvc.perform(get(urlTemplate)
                .contentType("application/json")
                .accept("application/json"))
                .andExpect(status)
                .andReturn();
    }

    @Test
    @Rollback
    void testFetchAllOrders() throws Exception {
        // create 2 orders (by directly saving to database)
        OrderRequest orderRequest1 = createOrderDtoTestObject("003");
        OrderRequest orderRequest2 = createOrderDtoTestObject("004");
        Order order1 = orderService.saveOrderDto(orderRequest1);
        Order order2 = orderService.saveOrderDto(orderRequest2);

        // then invoke API call to fetch all orders
        MvcResult ordersMvcResult =  getOrderMvcResult("http://api.okto-demo.eu/orders", status().isOk());
        List<OrderLiteDto> orderLiteDtoList = objectMapper.readValue(
                ordersMvcResult.getResponse().getContentAsString(),
                objectMapper.getTypeFactory().constructCollectionType(List.class, OrderLiteDto.class));

        // verify returned list in the response contains 2 elements
        assertNotNull(orderLiteDtoList);
        assertEquals(2, orderLiteDtoList.size());

        // verify 1st element has the correct values
        OrderLiteDto orderLiteDto1 = orderLiteDtoList.get(0);
        verifyOrderDtoResponse(order1, orderLiteDto1);

        // verify 2nd element has the correct values
        OrderLiteDto orderLiteDto2 = orderLiteDtoList.get(1);
        verifyOrderDtoResponse(order2, orderLiteDto2);
    }

    @Test
    void testFetchCertainOrder() throws Exception {
        // create 1 order (by directly saving to database)
        OrderRequest orderRequest = createOrderDtoTestObject("005");
        Order order = orderService.saveOrderDto(orderRequest);

        // then invoke API call to fetch the inserted order
        MvcResult ordersMvcResult =  getOrderMvcResult(
                "http://api.okto-demo.eu/orders/" + order.getId(), status().isOk());
        OrderLiteDto orderLiteDto = objectMapper.readValue(
                ordersMvcResult.getResponse().getContentAsString(),
                OrderLiteDto.class);

        // check response contains the correct order
        verifyOrderDtoResponse(order, orderLiteDto);
    }

    private void verifyOrderDtoResponse(Order order, OrderLiteDto orderLiteDto) {
        assertEquals(order.getClientReferenceCode(), orderLiteDto.getClientReferenceCode());
        assertEquals(order.getDescription(), orderLiteDto.getDescription());
        assertEquals(order.getItemCount(), orderLiteDto.getItemCount());
        assertEquals(order.getStatus(), orderLiteDto.getStatus());
    }

    @Test
    void testNonExistingOrderResponse() throws Exception {
        // then invoke API call to fetch an order that does not exist
        MvcResult orderMvcResult =  getOrderMvcResult(
                "http://api.okto-demo.eu/orders/" + UUID.randomUUID(), status().isBadRequest());
    }

    @Test
    void testOrdersOfCustomer() throws Exception {
        // create an order
        OrderRequest orderRequest = createOrderDtoTestObject("006");
        // convert to json string using Jackson Object Mapper
        String orderRequestAsString = objectMapper.writeValueAsString(orderRequest);
        // submit order request for the first time and expect successful response
        MvcResult submitOrderResult = postOrderMvcResult(orderRequestAsString, status().isOk());

        // fetch orders of customer
        MvcResult fetchCustomerOrdersResult = getOrderMvcResult("http://api.okto-demo.eu/orders/c/" + customer.getId(), status().isOk());
        // verify response data is correct, meaning customer has one order
        List<OrderLiteDto> customerOrderLiteDtoList = objectMapper.readValue(
                fetchCustomerOrdersResult.getResponse().getContentAsString(),
                objectMapper.getTypeFactory().constructCollectionType(List.class, OrderLiteDto.class));

        assertEquals(1, customerOrderLiteDtoList.size());
    }
}

