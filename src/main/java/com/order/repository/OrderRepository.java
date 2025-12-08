package com.order.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.order.bean.Order;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long>{

}
