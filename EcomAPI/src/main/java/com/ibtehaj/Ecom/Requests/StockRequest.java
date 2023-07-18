package com.ibtehaj.Ecom.Requests;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;

public class StockRequest {
	@NotNull(message = "Please provide number of available units")
    private Long  availableUnits;
	@NotNull(message = "Please provide stock date")
	@PastOrPresent(message = "Stock date cannot be in the future")
    private LocalDate stockDate;
	@NotNull(message = "Please provide price of unit")
    private BigDecimal unitPrice;
	@NotNull(message = "Please provide unit cost")
    private BigDecimal unitCost;
	
	public Long getUnits() {
		return availableUnits;
	}
	public void setUnits(Long availableUnits) {
		this.availableUnits = availableUnits;
	}
	public LocalDate getStockDate() {
		return stockDate;
	}
	public void setStockDate(LocalDate stockDate) {
		this.stockDate = stockDate;
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
