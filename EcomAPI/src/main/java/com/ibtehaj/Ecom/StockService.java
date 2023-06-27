package com.ibtehaj.Ecom;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;

@Service
public class StockService {
	private final ProductStockRepository productStockRepository;
	private final ProductRepository productRepository;

	public StockService(ProductRepository productRepository, ProductStockRepository productStockRepository) {
		this.productRepository = productRepository;
		this.productStockRepository = productStockRepository;
	}

	public boolean createProductStock(Long productId, StockRequest request) {
		Optional<Product> optionalproduct = productRepository.findById(productId);
		if (optionalproduct.isPresent()) {
			Product product = optionalproduct.get();
			// Create ProductStock entity
			ProductStock productStock = new ProductStock();
			productStock.setProduct(product);
			productStock.setAvailableUnits(request.getUnits());
			productStock.setStockDate(request.getStockDate());
			productStock.setUnitPrice(request.getUnitPrice());
			productStock.setUnitCost(request.getUnitCost());
			productStockRepository.save(productStock);
			return true;
		} else {
			return false;
		}
	}

	public ProductStock getStockById(Long stockId) {
		// Retrieve the stock by ID
		return productStockRepository.findById(stockId).orElse(null);
	}

	public List<ProductStock> getAllStocks() {
		return productStockRepository.findAll();
	}
	// to-do also implement get all stocks for a product
	public List<ProductStock>getStocksByProdcut(Product product){
		return productStockRepository.findByProduct(product);
	}

	public boolean updateStockforProduct(Long stockId, StockRequest request) {
		// Retrieve the stock by ID
		Optional<ProductStock> optionalProductStock = productStockRepository.findById(stockId);
		if (optionalProductStock.isPresent()) {
			ProductStock productStock = optionalProductStock.get();
			// Update the stock properties
			productStock.setId(stockId);
			productStock.setAvailableUnits(request.getUnits());
			productStock.setStockDate(request.getStockDate());
			productStock.setUnitPrice(request.getUnitPrice());
			productStock.setUnitCost(request.getUnitCost());
			// Save the updated stock
			productStockRepository.save(productStock);
			return true;
		} else {
			return false;
		}
	}

	public boolean deleteStockforProduct(Long stockId) {
		// Retrieve the stock by ID
		Optional<ProductStock> optionalProductStock = productStockRepository.findById(stockId);
		if (optionalProductStock.isPresent()) {
			ProductStock productStock = optionalProductStock.get();
			// Delete the product
			productStockRepository.delete(productStock);
			return true;
		} else {
			return false;
		}
	}
	// to-do also implement the method to delete all the stocks for a product
	@Transactional
	public boolean deleteAllStockforProduct(Long productId) {
	    // Retrieve the product by ID
	    Optional<Product> optionalProduct = productRepository.findById(productId);
	    if (optionalProduct.isPresent()) {
	        Product product = optionalProduct.get();
	        // Delete all the stock entries for the product
	        productStockRepository.deleteAllByProduct(product);
	        return true;
	    } else {
	        return false;
	    }
	}
	
}
