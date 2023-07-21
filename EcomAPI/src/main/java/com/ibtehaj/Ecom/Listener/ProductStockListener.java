package com.ibtehaj.Ecom.Listener;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import com.ibtehaj.Ecom.Controller.StockUpdateSSEController;
import com.ibtehaj.Ecom.Exception.NoAvailableStockException;
import com.ibtehaj.Ecom.Models.Cart;
import com.ibtehaj.Ecom.Models.CartItem;
import com.ibtehaj.Ecom.Models.Product;
import com.ibtehaj.Ecom.Models.ProductStockSummary;
import com.ibtehaj.Ecom.Service.CartItemService;
import com.ibtehaj.Ecom.Service.CartService;
import com.ibtehaj.Ecom.Service.ProductService;
import com.ibtehaj.Ecom.Service.StockService;

@Component
public class ProductStockListener {
	private final StockService stockService;
	private final CartItemService cartItemService;
	private final CartService cartService;
	private final ProductService productService;
    private final StockUpdateSSEController sseController;
	
	public ProductStockListener(StockService stockService, CartItemService cartItemService, CartService cartService, ProductService productService,
			StockUpdateSSEController sseController) {
		this.stockService = stockService;
		this.cartItemService = cartItemService;
		this.cartService = cartService;
		this.productService = productService;
		this.sseController = sseController;
	}
    
	@RabbitListener(queues = "product.stock.update")
	public void listen(String code) throws NoAvailableStockException {
		Product product = productService.findProductByCode(code);
		ProductStockSummary productStockSummary = stockService.getProductStockSummary(product);
		// System.out.println(productStockSummary.getWeightedAvgUnitPrice()+"
		// "+productStockSummary.getTotalAvailableUnits());
		List<CartItem> cartItems = cartItemService.findCartItemByProduct(product);
		for (CartItem cartItem : cartItems) {
			int qunatity = cartItem.getQuantity();
			BigDecimal weightedAveragePrice = productStockSummary.getWeightedAvgUnitPrice();
			BigDecimal newSubTotal = weightedAveragePrice.multiply(BigDecimal.valueOf(qunatity));

			Cart cart = cartItem.getCart();
			BigDecimal subTotal = cartItem.getSubTotal();
			BigDecimal totalAmount = cart.getTotalAmount();
			BigDecimal difference = totalAmount.subtract(subTotal);
			BigDecimal newTotalAmount = difference.add(newSubTotal);
			cart.setTotalAmount(newTotalAmount);

			cartItem.setSubTotal(newSubTotal);
			cartItemService.createCartItem(cartItem);
			cartService.saveCart(cart);
			System.out.println("listened");
		}
		 // Trigger SSE update
        sseController.triggerPriceUpdate(productStockSummary.getWeightedAvgUnitPrice().doubleValue());

	}

}
