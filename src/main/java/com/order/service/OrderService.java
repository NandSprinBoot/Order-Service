package com.order.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.order.bean.Order;
import com.order.dto.OrderDTO;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

public interface OrderService {
    public Order placeOrder(Order order, HttpServletRequest request);

    public void createOrder(OrderDTO orderDTO) throws JsonProcessingException;
}
