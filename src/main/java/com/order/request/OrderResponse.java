package com.order.request;

import org.springframework.stereotype.Component;

import com.order.bean.Order;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Component
public class OrderResponse {

	private Order order;
}
