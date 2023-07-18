package com.ibtehaj.Ecom.Repository;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ibtehaj.Ecom.Models.Product;
import com.ibtehaj.Ecom.Models.ProductStock;

@Repository
public interface ProductStockRepository extends JpaRepository<ProductStock, Long> {
	List<ProductStock> findByProduct(Product product);
	void deleteAllByProduct(Product product);
	List<ProductStock> findByProductOrderByStockDateDesc(Product product);
	List<ProductStock> findByProductOrderByStockDateAsc(Product product);
	List<ProductStock> findByProductId(Long productId);
 
}