package com.order.bean;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name="Product")
public class Items {
	
	@Id
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator = "order_seq")
	@SequenceGenerator(name = "order_seq", sequenceName = "order_seq", allocationSize = 1)
	@Setter(AccessLevel.NONE)
	@Column(name = "prd_Id")
	private Long id;
	
	private String name;
	
	private Long quantity;
	
	private Double mrp;
	
	@JsonIgnore
	private Double sellPrice;
	
	private int discount;
	
	private String description;
	
	private Date sellDate;
	
	private Date expiryDate;
	
	private String code;
	
	private Double totalAmt;
	
	@ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ord_Id")
	//@JsonBackReference//it is used of because of In OrderService at line no 60, cyclic relation is used. Because of that kafkaTemplate.send throw error.
    @JsonIgnore 
	private Order orders;
}
