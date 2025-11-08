package com.order.bean.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.order.bean.Items;

public interface ItemsRepository extends JpaRepository<Items, Long>{

}
