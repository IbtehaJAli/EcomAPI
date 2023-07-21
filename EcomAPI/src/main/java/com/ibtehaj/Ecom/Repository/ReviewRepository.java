package com.ibtehaj.Ecom.Repository;


import com.ibtehaj.Ecom.Models.CustomerProfile;
import com.ibtehaj.Ecom.Models.Product;
import com.ibtehaj.Ecom.Models.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    // Find reviews by customer
    List<Review> findByCustomer(CustomerProfile customer);

    // Find reviews by product
    List<Review> findByProduct(Product product);

	boolean existsByProduct_IdAndCustomer_Id(Long productId, Long customerId);
}

