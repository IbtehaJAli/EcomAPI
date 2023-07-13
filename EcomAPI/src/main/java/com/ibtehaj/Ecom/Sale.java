package com.ibtehaj.Ecom;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "sale")
public class Sale {
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "sale_datetime")
    private LocalDateTime saleDateTime;

    @Column(name = "total_amount")
    private BigDecimal totalAmount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    private CustomerProfile customer;

    @Column(name = "total_tax")
    private BigDecimal totalTax;

    @Column(name = "payment_mode")
    private String paymentMode;
    
    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private SaleStatus status;
    
    public Sale() {};
    
    public Sale(LocalDateTime saleDateTime, BigDecimal totalAmount, CustomerProfile customer,
            BigDecimal totalTax, String paymentMode, SaleStatus status) {
    super();
    this.saleDateTime = saleDateTime;
    this.totalAmount = totalAmount;
    this.customer = customer;
    this.totalTax = totalTax;
    this.paymentMode = paymentMode;
    this.status = status;
}

    // Constructors, getters, and setters
    public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
	
    public LocalDateTime getSaleDateTime() {
		return saleDateTime;
	}

	public void setSaleDateTime(LocalDateTime saleDateTime) {
		this.saleDateTime = saleDateTime;
	}

	public BigDecimal getTotalAmount() {
		return totalAmount;
	}

	public void setTotalAmount(BigDecimal totalAmount) {
		this.totalAmount = totalAmount;
	}

	public CustomerProfile getCustomer() {
		return customer;
	}

	public void setCustomer(CustomerProfile customer) {
		this.customer = customer;
	}

	public BigDecimal getTotalTax() {
		return totalTax;
	}

	public void setTotalTax(BigDecimal totalTax) {
		this.totalTax = totalTax;
	}

	public String getPaymentMode() {
		return paymentMode;
	}

	public void setPaymentMode(String paymentMode) {
		this.paymentMode = paymentMode;
	}
	
	public SaleStatus getStatus() {
		return status;
	}

	public void setStatus(SaleStatus status) {
		this.status = status;
	}
}
