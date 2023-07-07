package com.ibtehaj.Ecom;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class ProductStockListener {
	private final StockService stockService;
	private final CartItemService cartItemService;
	private final CartService cartService;
	private final ProductService productService;
	
	public ProductStockListener(StockService stockService, CartItemService cartItemService, CartService cartService, ProductService productService) {
		this.stockService = stockService;
		this.cartItemService = cartItemService;
		this.cartService = cartService;
		this.productService = productService;
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

	}

}
