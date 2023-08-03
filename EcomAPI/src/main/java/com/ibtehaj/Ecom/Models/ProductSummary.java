package com.ibtehaj.Ecom.Models;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class ProductSummary {
    
	private Long id;
    private String productName;
    private String code;
    private String attributes;
    private Long totalAvailableUnits;
    private BigDecimal weightedAvgUnitPrice;
    private int reviewCount;
    private double averageRating;
    private int totalUnitsSold;
    @JsonIgnore
    private LocalDate lastestDate;
    
    // Constructors, getters, and setters
    public ProductSummary(){};
    
    /**
	 * @param productName
	 * @param code
	 * @param attributes
	 * @param totalAvailableUnits
	 * @param weightedAvgUnitPrice
	 * @param reviewCount
	 * @param averageRating
	 * @param totalUnitsSold
	 * @param lastestDate
	 */
	public ProductSummary(String productName, String code, String attributes, Long totalAvailableUnits,
			BigDecimal weightedAvgUnitPrice, int reviewCount, double averageRating, int totalUnitsSold,
			LocalDate lastestDate) {
		this.productName = productName;
		this.code = code;
		this.attributes = attributes;
		this.totalAvailableUnits = totalAvailableUnits;
		this.weightedAvgUnitPrice = weightedAvgUnitPrice;
		this.reviewCount = reviewCount;
		this.averageRating = averageRating;
		this.totalUnitsSold = totalUnitsSold;
		this.lastestDate = lastestDate;
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
	public int getReviewCount() {
        return reviewCount;
    }
	public void setReviewCount(int reviewCount) {
        this.reviewCount = reviewCount;
    }
	public double getAverageRating() {
		return averageRating;
	}
	public void setAverageRating(double averageRating) {
		this.averageRating = averageRating;
	}
	public int getTotalUnitsSold() {
		return totalUnitsSold;
	}
	public void setTotalUnitsSold(int totalUnitsSold) {
		this.totalUnitsSold = totalUnitsSold;
	}

	public LocalDate getLastestDate() {
		return lastestDate;
	}

	public void setLastestDate(LocalDate lastestDate) {
		this.lastestDate = lastestDate;
	}
}
