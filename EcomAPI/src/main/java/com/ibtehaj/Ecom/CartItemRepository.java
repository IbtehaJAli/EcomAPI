package com.ibtehaj.Ecom;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {

	List<CartItem> findByCart(Cart cart);

	void deleteAllByCart(Cart cart);
	CartItem findByProductAndCart(Product product, Cart cart);
    
}
