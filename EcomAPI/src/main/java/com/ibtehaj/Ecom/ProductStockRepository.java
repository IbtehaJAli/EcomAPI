package com.ibtehaj.Ecom;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductStockRepository extends JpaRepository<ProductStock, Long> {
	List<ProductStock> findByProduct(Product product);
	void deleteAllByProduct(Product product);
 
}