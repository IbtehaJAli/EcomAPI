package com.ibtehaj.Ecom;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;

@Service
public class StockService {
	private final ProductStockRepository productStockRepository;
	private final ProductRepository productRepository;
	
	@Autowired
    RabbitTemplate rabbitTemplate;

	public StockService(ProductRepository productRepository, ProductStockRepository productStockRepository) {
		this.productRepository = productRepository;
		this.productStockRepository = productStockRepository;
	}
	
	public void saveStock(ProductStock productStock) {
		productStockRepository.save(productStock);
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
			// System.out.println(productStock.getId());
			System.out.println(productStock.getProduct().getCode());
			rabbitTemplate.convertAndSend("product.stock.update", productStock.getProduct().getCode());
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
	public List<ProductStock> getProductStocksByProductId(Long productId) {
	    // Retrieve the product stocks by product ID
	    return productStockRepository.findByProductId(productId);
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
			//send the code to the listener to update the subTotal and Total amounts for all cart items and their carts respectively.
			rabbitTemplate.convertAndSend("product.stock.update", productStock.getProduct().getCode());
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
			rabbitTemplate.convertAndSend("product.stock.update", productStock.getProduct().getCode());
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
	        rabbitTemplate.convertAndSend("product.stock.update", product.getCode());
	        return true;
	    } else {
	        return false;
	    }
	}
	
	public ProductStock getLatestAvailableStockByProduct(Product product) throws NoAvailableStockException {
	    // Retrieve all the stocks for the given product and order them by stockDate in descending order
	    List<ProductStock> stocks = productStockRepository.findByProductOrderByStockDateDesc(product);

	    // If there are no stocks, throw a custom exception
	    if (stocks.isEmpty()) {
	        throw new NoAvailableStockException("No available stock found for product " + product.getProductName());
	    }

	    // Iterate over the list of stocks and return the first stock with available units
	    boolean foundStockWithAvailableUnits = false;
	    for (ProductStock stock : stocks) {
	        if (stock.getAvailableUnits() > 0) {
	            foundStockWithAvailableUnits = true;
	            return stock;
	        }
	    }

	    // If all stocks have zero available units, throw a custom exception
	    if (!foundStockWithAvailableUnits) {
	        throw new NoAvailableStockException("All stocks for product " + product.getProductName() + " have zero available units");
	    }

	    // We shouldn't reach this point, but we need to return something to make the compiler happy
	    return null;
	}
	
	public ProductStock getOldestAvailableStockByProduct(Product product) throws NoAvailableStockException {
	    List<ProductStock> stocks = productStockRepository.findByProductOrderByStockDateAsc(product);

	    if (stocks.isEmpty()) {
	        throw new NoAvailableStockException("No available stock found for product " + product.getProductName());
	    }

	    for (ProductStock stock : stocks) {
	        if (stock.getAvailableUnits() > 0) {
	            return stock;
	        }
	    }

	    throw new NoAvailableStockException("All stocks for product " + product.getProductName() + " have zero available units");
	}
	
	public ProductStockSummary getProductStockSummary(Product product) throws NoAvailableStockException {
        List<ProductStock> productStocks = productStockRepository.findByProduct(product);
        BigDecimal totalUnitPrice = BigDecimal.ZERO;
        Long totalAvailableUnits = 0L;
        boolean foundStockWithAvailableUnits = false;
        for (ProductStock productStock : productStocks) {
            BigDecimal unitPrice = productStock.getUnitPrice();
            Long availableUnits = productStock.getAvailableUnits();

            totalUnitPrice = totalUnitPrice.add(unitPrice.multiply(BigDecimal.valueOf(availableUnits)));
            totalAvailableUnits += availableUnits;
            if (availableUnits > 0) {
	            foundStockWithAvailableUnits = true;
	        }
        }
        // If all stocks have zero available units, throw a custom exception
	    if (!foundStockWithAvailableUnits) {
	        throw new NoAvailableStockException("All stocks for product " + product.getProductName() + " have zero available units");
	    }
	    
        BigDecimal weightedAvgUnitPrice = BigDecimal.ZERO;
        
        if (totalAvailableUnits != 0L) {
            weightedAvgUnitPrice = totalUnitPrice.divide(BigDecimal.valueOf(totalAvailableUnits), 2, RoundingMode.HALF_UP);
        }

        return new ProductStockSummary(weightedAvgUnitPrice, totalAvailableUnits);
    }

	
}
