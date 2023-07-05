package com.ibtehaj.Ecom;

import java.math.BigDecimal;

public class ProductSummary {
    
	private Long id;
    private String productName;
    private String code;
    private String attributes;
    private Long totalAvailableUnits;
    private BigDecimal weightedAvgUnitPrice;
	

    // Constructors, getters, and setters
    public ProductSummary(){};
    
    public ProductSummary(String productName, String code, String attributes, Long totalAvailableUnits,
			BigDecimal weightedAvgUnitPrice) {
		super();
		this.productName = productName;
		this.code = code;
		this.attributes = attributes;
		this.totalAvailableUnits = totalAvailableUnits;
		this.weightedAvgUnitPrice = weightedAvgUnitPrice;
	}
    
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getProductName() {
		return productName;
	}
	public void setProductName(String productName) {
		this.productName = productName;
	}
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	public String getAttributes() {
		return attributes;
	}
	public void setAttributes(String attributes) {
		this.attributes = attributes;
	}
	public Long getTotalAvailableUnits() {
		return totalAvailableUnits;
	}
	public void setTotalAvailableUnits(Long totalAvailableUnits) {
		this.totalAvailableUnits = totalAvailableUnits;
	}
	public BigDecimal getWeightedAvgUnitPrice() {
		return weightedAvgUnitPrice;
	}
	public void setWeightedAvgUnitPrice(BigDecimal weightedAvgUnitPrice) {
		this.weightedAvgUnitPrice = weightedAvgUnitPrice;
	}
}
