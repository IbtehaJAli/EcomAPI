package com.ibtehaj.Ecom;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "sale_item")
public class SaleItem {
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sale_id")
    private Sale sale;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_stock_id")
    private ProductStock productStock;

    @Column(name = "units_bought")
    private Integer unitsBought;

    @Column(name = "sub_total")
    private BigDecimal subTotal;

    @Column(name = "unit_price")
    private BigDecimal unitPrice;

    private BigDecimal tax;

    // Constructors, getters, and setters
    public SaleItem() {};
    public SaleItem(Sale sale, ProductStock productStock, Integer unitsBought, BigDecimal subTotal,
			BigDecimal unitPrice, BigDecimal tax) {
		super();
		this.sale = sale;
		this.productStock = productStock;
		this.unitsBought = unitsBought;
		this.subTotal = subTotal;
		this.unitPrice = unitPrice;
		this.tax = tax;
	}
    
    public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Sale getSale() {
		return sale;
	}

	public void setSale(Sale sale) {
		this.sale = sale;
	}

	public ProductStock getProductStock() {
		return productStock;
	}

	public void setProductStock(ProductStock productStock) {
		this.productStock = productStock;
	}

	public Integer getUnitsBought() {
		return unitsBought;
	}

	public void setUnitsBought(Integer unitsBought) {
		this.unitsBought = unitsBought;
	}

	public BigDecimal getSubTotal() {
		return subTotal;
	}

	public void setSubTotal(BigDecimal subTotal) {
		this.subTotal = subTotal;
	}

	public BigDecimal getUnitPrice() {
		return unitPrice;
	}

	public void setUnitPrice(BigDecimal unitPrice) {
		this.unitPrice = unitPrice;
	}

	public BigDecimal getTax() {
		return tax;
	}

	public void setTax(BigDecimal tax) {
		this.tax = tax;
	}
}

