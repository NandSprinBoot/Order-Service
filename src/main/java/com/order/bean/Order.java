package com.order.bean;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name="Orders")
public class Order {
	
	@Id
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator = "order_seq")
	@SequenceGenerator(name = "order_seq", sequenceName = "order_seq", allocationSize = 1)
	@Setter(AccessLevel.NONE)
    @Column(name = "ord_id")
	private Long ordId;
	
	private String ordStatus;
	
	@JsonIgnore
	private String createdBy;
	
	@Setter(AccessLevel.PRIVATE)
	private Date createdOn;
	
	private String custName;
	
	private String custMobile;
	
	@OneToMany(mappedBy = "orders", cascade = CascadeType.ALL, orphanRemoval = true)
	//@JsonManagedReference//it is used of because of In OrderService at line no 60, cyclic relation is used. Because of that kafkaTemplate.send throw error.
	private List<Items> item;

	public Order() {
		super();
		this.createdOn = new Date();
	}
}
