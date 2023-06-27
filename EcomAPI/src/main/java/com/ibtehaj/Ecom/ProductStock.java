package com.ibtehaj.Ecom;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.persistence.*;

@Entity
@Table(name = "product_stock")
public class ProductStock {
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    @Column(name = "available_units")
    private Long availableUnits;

    @Column(name = "stock_date")
    private LocalDate stockDate;

    @Column(name = "unit_price")
    private BigDecimal unitPrice;

    @Column(name = "unit_cost")
    private BigDecimal unitCost;

    // Constructors, getters, and setters
    public ProductStock() {};
    
    public ProductStock(Product product, Long availableUnits, LocalDate stockDate, BigDecimal unitPrice,
			BigDecimal unitCost) {
		super();
		this.product = product;
		this.availableUnits = availableUnits;
		this.stockDate = stockDate;
		this.unitPrice = unitPrice;
		this.unitCost = unitCost;
	}
    
    public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Product getProduct() {
		return product;
	}

	public void setProduct(Product product) {
		this.product = product;
	}

	public Long getAvailableUnits() {
		return availableUnits;
	}

	public void setAvailableUnits(Long availableUnits) {
		this.availableUnits = availableUnits;
	}

	public LocalDate getStockDate() {
		return stockDate;
	}

	public void setStockDate(LocalDate localDate) {
		this.stockDate = localDate;
	}

	public BigDecimal getUnitPrice() {
		return unitPrice;
	}

	public void setUnitPrice(BigDecimal unitPrice) {
		this.unitPrice = unitPrice;
	}

	public BigDecimal getUnitCost() {
		return unitCost;
	}

	public void setUnitCost(BigDecimal unitCost) {
		this.unitCost = unitCost;
	}
}

