package com.ibtehaj.Ecom.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ibtehaj.Ecom.Models.Product;
import com.ibtehaj.Ecom.Models.Sale;
import com.ibtehaj.Ecom.Models.SaleItem;

@Repository
public interface SaleItemRepository extends JpaRepository<SaleItem, Long> {

	List<SaleItem> findBySale(Sale sale);

	void deleteAllBySale(Sale sale);

	List<SaleItem> findBySaleId(Long saleId);

	// The method findByProductStock_Product(product) in the SaleItemRepository
	// translates to "find sale items by ProductStock where the Product of the
	// ProductStock is equal to the given product".
	List<SaleItem> findByProductStock_Product(Product product);

}