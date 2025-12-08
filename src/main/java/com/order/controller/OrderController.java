package com.order.controller;

import static java.util.Objects.nonNull;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.order.dto.OrderDTO;
import com.order.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.order.bean.Order;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("orders")
public class OrderController {

	@Autowired
	private OrderService orderService;

	@PostMapping("/create")
	public ResponseEntity<Order> placeOrder(@RequestBody Order order, HttpServletRequest request) {
		ResponseEntity<Order> responseEntity = null;
		try {
			Order savedOrder = orderService.placeOrder(order, request);
			if (nonNull(savedOrder)) {
				return responseEntity.status(HttpStatus.CREATED).body(savedOrder);
			}

			return responseEntity.status(HttpStatus.BAD_REQUEST).body(savedOrder);

		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(order);
		}
	}

    @PostMapping//("/orders")
    public ResponseEntity<?> createOrder(@RequestBody OrderDTO order) throws JsonProcessingException {
        orderService.createOrder(order);
        return ResponseEntity.ok("Order Created");
    }

}
