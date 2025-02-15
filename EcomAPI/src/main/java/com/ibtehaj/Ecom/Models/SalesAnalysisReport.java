package com.ibtehaj.Ecom.Models;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SalesAnalysisReport {

    private BigDecimal totalSales;
    private List<SaleItem> saleItems;
    private Integer totalUnitsSold;
    private Product productWithMaxUnits;
    private Product productWithMinUnits;
    private Map<LocalDate, BigDecimal> revenueByDate;
    private LocalDate dateWithHighestRevenue;
    private LocalDate dateWithLowestRevenue;
    private LocalDate dateWithMostUnitsBought;
    private LocalDate dateWithLeastUnitsBought;
    private Map<Product, BigDecimal> totalRevenueByProduct;
	private Map<Product, Integer> unitsBoughtByProduct; 
	private Product productWithHighestRevenue;
	private Product productWithLowestRevenue;
	private Product mostReviewedProduct;// later 
	private Product leastReviewedProduct; // later
	private Product productWithHighestRating; 
	private Product productWithLowestRating;

    // Default constructor
    public SalesAnalysisReport() {
    }

    // Getters and setters for all fields

    public BigDecimal getTotalSales() {
        return totalSales;
    }

    public void setTotalSales(BigDecimal totalSales) {
        this.totalSales = totalSales;
    }

    public List<SaleItem> getSaleItems() {
        return saleItems;
    }

    public void setSaleItems(List<SaleItem> saleItems) {
        this.saleItems = saleItems;
    }

    public Integer getTotalUnitsSold() {
        return totalUnitsSold;
    }

    public void setTotalUnitsSold(Integer totalUnitsSold) {
        this.totalUnitsSold = totalUnitsSold;
    }

    public Product getProductWithMaxUnits() {
        return productWithMaxUnits;
    }

    public void setProductWithMaxUnits(Product productWithMaxUnits) {
        this.productWithMaxUnits = productWithMaxUnits;
    }

    public Product getProductWithMinUnits() {
        return productWithMinUnits;
    }

    public void setProductWithMinUnits(Product productWithMinUnits) {
        this.productWithMinUnits = productWithMinUnits;
    }

    public Map<LocalDate, BigDecimal> getRevenueByDate() {
        return revenueByDate;
    }

    public void setRevenueByDate(Map<LocalDate, BigDecimal> revenueByDate) {
        this.revenueByDate = revenueByDate;
    }

    public LocalDate getDateWithHighestRevenue() {
        return dateWithHighestRevenue;
    }

    public void setDateWithHighestRevenue(LocalDate dateWithHighestRevenue) {
        this.dateWithHighestRevenue = dateWithHighestRevenue;
    }

    public LocalDate getDateWithLowestRevenue() {
        return dateWithLowestRevenue;
    }

    public void setDateWithLowestRevenue(LocalDate dateWithLowestRevenue) {
        this.dateWithLowestRevenue = dateWithLowestRevenue;
    }

    public LocalDate getDateWithMostUnitsBought() {
        return dateWithMostUnitsBought;
    }

    public void setDateWithMostUnitsBought(LocalDate dateWithMostUnitsBought) {
        this.dateWithMostUnitsBought = dateWithMostUnitsBought;
    }

    public LocalDate getDateWithLeastUnitsBought() {
        return dateWithLeastUnitsBought;
    }

    public void setDateWithLeastUnitsBought(LocalDate dateWithLeastUnitsBought) {
        this.dateWithLeastUnitsBought = dateWithLeastUnitsBought;
    }

	public Map<Product, BigDecimal> getTotalRevenueByProduct() {
		return totalRevenueByProduct;
	}

	public void setTotalRevenueByProduct(Map<Product, BigDecimal> totalRevenueByProduct) {
		this.totalRevenueByProduct = totalRevenueByProduct;
	}

	public Map<Product, Integer> getUnitsBoughtByProduct() {
		return unitsBoughtByProduct;
	}

	public void setUnitsBoughtByProduct(Map<Product, Integer> unitsBoughtByProduct) {
		this.unitsBoughtByProduct = unitsBoughtByProduct;
	}

	public Product getProductWithHighestRevenue() {
		return productWithHighestRevenue;
	}

	public void setProductWithHighestRevenue(Product productWithHighestRevenue) {
		this.productWithHighestRevenue = productWithHighestRevenue;
	}

	public Product getProductWithLowestRevenue() {
		return productWithLowestRevenue;
	}

	public void setProductWithLowestRevenue(Product productWithLowestRevenue) {
		this.productWithLowestRevenue = productWithLowestRevenue;
	}

	public Product getMostReviewedProduct() {
		return mostReviewedProduct;
	}

	public void setMostReviewedProduct(Product mostReviewedProduct) {
		this.mostReviewedProduct = mostReviewedProduct;
	}

	public Product getLeastReviewedProduct() {
		return leastReviewedProduct;
	}

	public void setLeastReviewedProduct(Product leastReviewedProduct) {
		this.leastReviewedProduct = leastReviewedProduct;
	}

	public Product getProductWithHighestRating() {
		return productWithHighestRating;
	}

	public void setProductWithHighestRating(Product productWithHighestRating) {
		this.productWithHighestRating = productWithHighestRating;
	}

	public Product getProductWithLowestRating() {
		return productWithLowestRating;
	}

	public void setProductWithLowestRating(Product productWithLowestRating) {
		this.productWithLowestRating = productWithLowestRating;
	}



}
