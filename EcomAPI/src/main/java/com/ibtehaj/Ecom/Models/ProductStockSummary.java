package com.ibtehaj.Ecom.Models;
import java.math.BigDecimal;
public class ProductStockSummary {
    /**
	 * 
	 */
	public ProductStockSummary() {
	}

	private BigDecimal weightedAvgUnitPrice;
    private Long totalAvailableUnits;

    public ProductStockSummary(BigDecimal weightedAvgUnitPrice, Long totalAvailableUnits) {
        this.weightedAvgUnitPrice = weightedAvgUnitPrice;
        this.totalAvailableUnits = totalAvailableUnits;
    }

    public BigDecimal getWeightedAvgUnitPrice() {
        return weightedAvgUnitPrice;
    }

    public void setWeightedAvgUnitPrice(BigDecimal weightedAvgUnitPrice) {
        this.weightedAvgUnitPrice = weightedAvgUnitPrice;
    }

    public Long getTotalAvailableUnits() {
        return totalAvailableUnits;
    }

    public void setTotalAvailableUnits(Long totalAvailableUnits) {
        this.totalAvailableUnits = totalAvailableUnits;
    }
}
