package com.order.bean.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.order.bean.Order;

public interface OrderRepository extends JpaRepository<Order, Long>{

}
