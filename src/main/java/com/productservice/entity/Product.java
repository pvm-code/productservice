package com.productservice.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;

@Entity
public class Product {
	
	
	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@Column(name = "id" ,nullable = false,updatable = false)
	private UUID id;
	
	@Column(name = "name",nullable = false)
	private String name;
	
	@Column(name = "description")
	private String description;
	
	@Column(name = "price",nullable = false,precision = 19,scale = 2)
	private BigDecimal price;
	
	@Column(name = "stock",nullable = false)
	private Integer stock;
	
	@Column(name = "created_at", nullable = false)
	private LocalDateTime createdAt;
	
	@Column(name = "updated_at", nullable = false)
	private LocalDateTime updatedAt;
	
	@PrePersist
	protected void onCreate() {
		LocalDateTime now = LocalDateTime.now();
		createdAt= now;
		updatedAt= now;
		
	}
	
	@PreUpdate
	protected void onUpdate() {
		
		updatedAt = LocalDateTime.now();	
		
	}

	public UUID getId() {
		return id;
	}

	

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public BigDecimal getPrice() {
		return price;
	}

	public void setPrice(BigDecimal price) {
		this.price = price;
	}

	public Integer getStock() {
		return stock;
	}

	public void setStock(Integer stock) {
		this.stock = stock;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}



	public LocalDateTime getUpdatedAt() {
		return updatedAt;
	}

	
	
}
