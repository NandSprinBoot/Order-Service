package com.order.dto;

import java.util.Date;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Component
public class ItemDTO {

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

}
