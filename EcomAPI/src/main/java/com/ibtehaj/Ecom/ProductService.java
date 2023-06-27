package com.ibtehaj.Ecom;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductStockRepository productStockRepository;

    public ProductService(ProductRepository productRepository, ProductStockRepository productStockRepository) {
        this.productRepository = productRepository;
        this.productStockRepository = productStockRepository;
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
}
