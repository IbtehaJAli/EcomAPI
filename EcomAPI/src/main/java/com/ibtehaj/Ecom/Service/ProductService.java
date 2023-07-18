package com.ibtehaj.Ecom.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;
import java.util.ArrayList;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ibtehaj.Ecom.Models.Product;
import com.ibtehaj.Ecom.Models.ProductStock;
import com.ibtehaj.Ecom.Models.ProductSummary;
import com.ibtehaj.Ecom.Repository.ProductRepository;
import com.ibtehaj.Ecom.Repository.ProductStockRepository;
import com.ibtehaj.Ecom.Requests.ProductRequest;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductStockRepository productStockRepository;

    public ProductService(ProductRepository productRepository, ProductStockRepository productStockRepository) {
        this.productRepository = productRepository;
        this.productStockRepository = productStockRepository;
    }
    public Product findProductByCode(String code) {
    	return productRepository.findByCode(code);
    }
    public void createProduct(ProductRequest request) {
        // Create Product entity
        Product product = new Product();
        product.setProductName(request.getName());
        product.setCode(request.getCode());
        ObjectMapper mapper = new ObjectMapper();
        String str = null;
		try {
			str = mapper.writeValueAsString(request.getAttributes());
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			System.out.println("me doing");
			e.printStackTrace();
		}
        //System.out.println(str);
        product.setAttributes(str);
        
        productRepository.save(product);
        }
    
    public Product getProductById(Long productId) {
        // Retrieve the product by ID
        return productRepository.findById(productId).orElse(null);
    }
    
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }


    public boolean updateProduct(Long productId, ProductRequest request) {
        // Retrieve the product by ID
        Optional<Product> optionalProduct = productRepository.findById(productId);
        if (optionalProduct.isPresent()) {
            Product product = optionalProduct.get();
            // Update the product properties
            product.setId(productId);
            product.setProductName(request.getName());
            product.setCode(request.getCode());
            ObjectMapper mapper = new ObjectMapper();
            String str = null;
    		try {
    			str = mapper.writeValueAsString(request.getAttributes());
    		} catch (JsonProcessingException e) {
    			// TODO Auto-generated catch block
    			e.printStackTrace();
    		}
            //System.out.println(str);
            product.setAttributes(str);
            // Save the updated product
            productRepository.save(product);
            return true;
        } else {
            return false;
        }
    }

    public boolean deleteProduct(Long productId) {
        // Retrieve the product by ID
        Optional<Product> optionalProduct = productRepository.findById(productId);
        if (optionalProduct.isPresent()) {
            Product product = optionalProduct.get();
            // Delete the product
            productRepository.delete(product);
            return true;
        } else {
            return false;
        }
    }
    
    public List<ProductSummary> getProductListWithStockSummary() {
        // Retrieve list of all products
        List<Product> productList = productRepository.findAll();

        // Create a new list for storing products with stock summary
        List<ProductSummary> productListWithStockSummary = new ArrayList<>();

        // Iterate over each product in the list
        for (Product product : productList) {
            // Retrieve all product stocks for the current product
            List<ProductStock> productStockList = productStockRepository.findByProduct(product);

            // Calculate the total available units and weighted average unit price for the product
            Long totalAvailableUnits = 0L;
            BigDecimal totalUnitPrice = BigDecimal.ZERO;
            for (ProductStock productStock : productStockList) {
                totalAvailableUnits += productStock.getAvailableUnits();
                totalUnitPrice = totalUnitPrice.add(productStock.getUnitPrice().multiply(BigDecimal.valueOf(productStock.getAvailableUnits())));
            }
            if (totalAvailableUnits == 0) {
                continue; // Skip this product if total available units is zero
            }
            BigDecimal weightedAvgUnitPrice = totalUnitPrice.divide(BigDecimal.valueOf(totalAvailableUnits), 2, RoundingMode.HALF_UP);

            // Create a new ProductSummary instance with the stock summary information and add it to the list
            ProductSummary productSummary = new ProductSummary();
            productSummary.setId(product.getId());
            productSummary.setProductName(product.getProductName());
            productSummary.setCode(product.getCode());
            productSummary.setAttributes(product.getAttributes());
            productSummary.setTotalAvailableUnits(totalAvailableUnits);
            productSummary.setWeightedAvgUnitPrice(weightedAvgUnitPrice);
            productListWithStockSummary.add(productSummary);
        }

        return productListWithStockSummary;
    }
}
