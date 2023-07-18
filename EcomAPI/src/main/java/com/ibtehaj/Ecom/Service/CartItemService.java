package com.ibtehaj.Ecom.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.ibtehaj.Ecom.Exception.CustomAccessDeniedException;
import com.ibtehaj.Ecom.Exception.NoAvailableStockException;
import com.ibtehaj.Ecom.Models.Cart;
import com.ibtehaj.Ecom.Models.CartItem;
import com.ibtehaj.Ecom.Models.Product;
import com.ibtehaj.Ecom.Models.ProductStockSummary;
import com.ibtehaj.Ecom.Models.User;
import com.ibtehaj.Ecom.Repository.CartItemRepository;
import com.ibtehaj.Ecom.Repository.CartRepository;

import jakarta.transaction.Transactional;

@Service 
public class CartItemService {
	private final CartItemRepository cartItemRepository;
	private final CartRepository cartRepository;
	private final StockService stockService;
	
	public CartItemService(CartItemRepository cartItemRepository, CartRepository cartRepository,StockService stockService ) {
		this.cartItemRepository = cartItemRepository;
		this.cartRepository = cartRepository;
		this.stockService = stockService;
	}

	public void createCartItem(CartItem cartItem) {
		cartItemRepository.save(cartItem);
	}
	
	public CartItem getCartItemById(Long Id) {
		return cartItemRepository.findById(Id).orElse(null);
	}
	
	public List<CartItem> getAllCartItemsByCart (Cart cart){
		return cartItemRepository.findByCart(cart);
	}
	
	public List<CartItem> getAllCartItems (){
		return cartItemRepository.findAll();
	}
	
	public boolean updateCartItem(Long Id, int quantity) throws NoAvailableStockException {
		Optional<CartItem> optionalCartItem = cartItemRepository.findById(Id);
		if(optionalCartItem.isPresent()) {
			CartItem cartItem = optionalCartItem.get();
			Product product = cartItem.getProduct();
			
			ProductStockSummary productStockSummary = stockService.getProductStockSummary(product);
			BigDecimal unitPrice = productStockSummary.getWeightedAvgUnitPrice();
			BigDecimal newSubTotal = unitPrice.multiply(BigDecimal.valueOf(quantity));
			
			Cart cart = cartItem.getCart();
			BigDecimal subTotal = cartItem.getSubTotal();
			BigDecimal totalAmount = cart.getTotalAmount();
			BigDecimal difference = totalAmount.subtract(subTotal);
			BigDecimal newTotalAmount = difference.add(newSubTotal);
			cart.setTotalAmount(newTotalAmount);
			
			cartItem.setId(Id);
			cartItem.setQuantity(quantity);
			cartItem.setSubTotal(newSubTotal);
			cartItemRepository.save(cartItem);
			cartRepository.save(cart);
			return true;
		}else {
			return false;
		}
	}
	
	public boolean deleteCartItemById(Long Id, User user) throws CustomAccessDeniedException {
		Optional<CartItem> optionalCartItem = cartItemRepository.findById(Id);
		if(optionalCartItem.isPresent()) {
			CartItem cartItem = optionalCartItem.get();
			Cart cart = cartItem.getCart();
			User cartUser = cart.getUser();
			if(cartUser!= user) {
				throw new CustomAccessDeniedException("you can not allowed to perform this action");
			}
			BigDecimal subTotal = cartItem.getSubTotal();
			BigDecimal totalAmount = cart.getTotalAmount();
			BigDecimal difference = totalAmount.subtract(subTotal);
			cart.setTotalAmount(difference);
			cartRepository.save(cart);
			cartItemRepository.delete(cartItem);
			return true;
			
		}else {
			return false;
		}
	}
	
	@Transactional
	public boolean deleteAllCartItemsforCart(Long cartId) {
	    // Retrieve the Cart by ID
	    Optional<Cart> optionalCart = cartRepository.findById(cartId);
	    if (optionalCart.isPresent()) {
	        Cart cart = optionalCart.get();
	        cart.setTotalAmount(BigDecimal.valueOf(0));
	        cartRepository.save(cart);
	        // Delete all the cartItems entries for the Cart
	        cartItemRepository.deleteAllByCart(cart);
	        System.out.println("deleted");
	        return true;
	    } else {
	        return false;
	    }
	}
	
	public CartItem findCartItemByProductAndCart(Product product, Cart cart) {
		CartItem cartItem = cartItemRepository.findByProductAndCart(product,cart);
		return cartItem;
	}
	
	public List<CartItem> findCartItemByProduct(Product product) {
		List<CartItem> cartItems = cartItemRepository.findByProduct(product);
		return cartItems;
	}

}
