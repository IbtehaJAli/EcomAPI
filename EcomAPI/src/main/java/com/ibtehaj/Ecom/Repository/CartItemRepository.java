package com.ibtehaj.Ecom.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ibtehaj.Ecom.Models.Cart;
import com.ibtehaj.Ecom.Models.CartItem;
import com.ibtehaj.Ecom.Models.Product;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {

	List<CartItem> findByCart(Cart cart);

	void deleteAllByCart(Cart cart);
	CartItem findByProductAndCart(Product product, Cart cart);

	List<CartItem> findByProduct(Product product);
    
}
