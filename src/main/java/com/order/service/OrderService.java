package com.order.service;

import java.util.List;
import static java.util.Objects.isNull;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;

import com.order.bean.Items;
import com.order.bean.Order;
import com.order.bean.repository.OrderRepository;
import com.order.dto.ItemDTO;
import com.order.dto.OrderDTO;
import com.order.exception.ResourceNotFoundException;
import com.order.request.OrderResponse;
import com.order.util.OrderUtil;

import jakarta.persistence.EntityManager;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;

@Service
public class OrderService {

	@Autowired
	private OrderRepository orderRepository;

	@Autowired
	private KafkaTemplate<String, Order> kafkaTemplate;

	@Autowired
	private OrderUtil orderUtil;

	@Autowired
	private EntityManager entityManager;

	@Value("${spring.topics.orderCreate}")
	private String createOrder;

	@Value("${spring.topics.orderFail}")
	private String failOrder;

	@Transactional
	public Order placeOrder(Order order, HttpServletRequest request) {
		OrderDTO orderDTO = null;
		try {
			calculateSellPriceForItems(order);
			calculateTotalAmtPriceForItems(order);
			order.setCreatedBy(orderUtil.extractUsernameFromBearerToken(request));
			orderRepository.save(order);
			order.getItem().stream().forEach(item -> item.setOrders(order));
			entityManager.flush();
			orderDTO = mapToOrderDTO(order);
			kafkaTemplate.send(serilizeOrderResponse(orderDTO, createOrder));
			return order;
		} catch (Exception e) {
			order.setOrdStatus("ORDER_FAILED");
			orderRepository.save(order);
			kafkaTemplate.send(serilizeOrderResponse(orderDTO, failOrder));
			throw e;
		}
	}

	@KafkaListener(topics = "order-status", groupId = "order-group")
	public void handleOrderCancled(Order order) {
		try {
			System.out.println("Listener at order service, starting update order");
			orderRepository.save(order);
		} catch (Exception e) {
			throw e;
		}

	}

	public void calculateSellPriceForItems(Order order) {
		if (isNull(order) || isNull(order.getItem())) {
			throw new ResourceNotFoundException("Order or Items list is null");

		}

		List<Items> updatedItems = order.getItem().stream().peek(item -> calculateSellPrice(item)).toList();
		// If you want to update the order's items list, you can directly set it here
		order.setItem(updatedItems); // Optional: If you want to replace the list

	}

	private void calculateSellPrice(Items item) {
		if (isNull(item.getMrp()) && item.getDiscount() >= 0) {
			throw new IllegalArgumentException("Invalid MRP or discount for item: " + item.getName());

		}
		Double discountAmount = (item.getMrp() * item.getDiscount()) / 100;
		Double sellPrice = item.getMrp() - discountAmount;
		item.setSellPrice(sellPrice);
	}

	public void calculateTotalAmtPriceForItems(Order order) {
		if (isNull(order) && isNull(order.getItem())) {
			throw new ResourceNotFoundException("Order or Items list is null");

		}
		List<Items> updatedItems = order.getItem().stream().peek(item -> calculateTotalAmtForEachItem(item)).toList();
		// If you want to update the order's items list, you can directly set it here
		order.setItem(updatedItems); // Optional: If you want to replace the list
	}

	private void calculateTotalAmtForEachItem(Items item) {
		if (item.getQuantity() < 1 && item.getSellPrice() < 0) {

			throw new IllegalArgumentException(
					"Item selected or sell price is less than or equal to 0 for item: " + item.getName());

		}

		Double totalAmt = item.getQuantity() * item.getSellPrice();
		item.setTotalAmt(totalAmt);
	}

	private Message<OrderDTO> serilizeOrderResponse(OrderDTO orderDTO, String topicName) {
		return MessageBuilder.withPayload(orderDTO).setHeader(KafkaHeaders.TOPIC, topicName).build();
	}

	public static OrderDTO mapToOrderDTO(Order order) {
		OrderDTO orderDTO = new OrderDTO();
		orderDTO.setOrdId(order.getOrdId());
		orderDTO.setOrdStatus(order.getOrdStatus());
		orderDTO.setCreatedBy(order.getCreatedBy());
		orderDTO.setCustMobile(order.getCustMobile());
		List<Items> listOfItem = order.getItem();
		orderDTO.setItem(listOfItem.stream().map(item -> {
			ItemDTO itemDTO = new ItemDTO();
			itemDTO.setId(item.getId());
			itemDTO.setName(item.getName());
			itemDTO.setQuantity(item.getQuantity());
			return itemDTO;
		}).collect(Collectors.toList()));
		return orderDTO;
	}
}
