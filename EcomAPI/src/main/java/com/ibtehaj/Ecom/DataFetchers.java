package com.ibtehaj.Ecom;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import graphql.schema.DataFetchingEnvironment;

public class DataFetchers {

	  @Autowired
	  private ProductService productService;
	  @Autowired
	  private StockService stockService;
	  
	    
	  public DataFetchers(ProductService productService, StockService stockService) {
		this.productService = productService;
		this.stockService = stockService;
	}

	  // get all products
	public List<Product> products(DataFetchingEnvironment dfe) { 
	    return productService.getAllProducts();   
	  }
	
	// get single product by id
	public Product product(DataFetchingEnvironment dfe) {
	    // Get the product ID from the arguments
	    Long productId = Long.parseLong(dfe.getArgument("id"));

	    // Retrieve the product by ID using the ProductService
	    return productService.getProductById(productId);
	}
	
	// get single product stock by id
	public ProductStock productStock(DataFetchingEnvironment dfe) {
	    // Get the productStock ID from the arguments
	    Long productStockId = Long.parseLong(dfe.getArgument("productStockId"));

	    // Retrieve the product stock by productStock ID using the stockService
	    return stockService.getStockById(productStockId);
	}
	
	//get all product Stocks for a product 
	public List<ProductStock> productStocks(DataFetchingEnvironment dfe) {
	    // Get the product ID from the arguments
	    Long productId = Long.parseLong(dfe.getArgument("productId"));

	    // Retrieve the product stocks by product ID using the stockService
	    return stockService.getProductStocksByProductId(productId);
	}
	
	//get all stocks
	public List<ProductStock> allProductStocks(DataFetchingEnvironment dfe) {

	    // Retrieve the all product stocks using the stockService
	    return stockService.getAllStocks();
	}

	}