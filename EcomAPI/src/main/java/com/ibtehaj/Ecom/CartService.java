package com.ibtehaj.Ecom;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

@Service
public class CartService {
	private final CartRepository cartRepository;
	
	public CartService(CartRepository cartRepository) {
		this.cartRepository = cartRepository;
	}

	public void saveCart(Cart cart) {
		cartRepository.save(cart);
	}
	public void createCart(User user) {
		Cart cart = new Cart(user,BigDecimal.valueOf(0));
		cartRepository.save(cart);
	}
	public Cart getCartById(Long Id) {
		return cartRepository.findById(Id).orElse(null);
	}
	
	public Cart getCartByUser(User user) {
		return cartRepository.findByUser(user).orElse(null);
	}
	
	public List<Cart> getAllCarts(){
		return cartRepository.findAll();
	}

	public Cart createOrGetCartByUser(User user) {
		Optional<Cart> optionalCart = cartRepository.findByUser(user);
		if(optionalCart.isPresent()) {
			Cart cart = optionalCart.get();
			return cart;
		}else {
			Cart cart = new Cart(user,BigDecimal.valueOf(0));
			cartRepository.save(cart);
			return cart;
		}
	}
	
}
