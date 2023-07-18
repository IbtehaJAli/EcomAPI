package com.ibtehaj.Ecom.Listener;



import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import com.ibtehaj.Ecom.Service.CartItemService;

@Component
public class CartItemListener {
	private final CartItemService cartItemService;
	
	public CartItemListener(CartItemService cartItemService) {
		this.cartItemService = cartItemService;
	}
    
	@RabbitListener(queues = "checkout")
	public void listen(String id) {
		long cartId = Long.parseLong(id);
		cartItemService.deleteAllCartItemsforCart(cartId);
        System.out.println("Cart items deleted");

}

}