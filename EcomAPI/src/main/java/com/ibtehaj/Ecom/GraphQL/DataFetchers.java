package com.ibtehaj.Ecom.GraphQL;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.ibtehaj.Ecom.Models.CustomerProfile;
import com.ibtehaj.Ecom.Models.Product;
import com.ibtehaj.Ecom.Models.ProductStock;
import com.ibtehaj.Ecom.Models.Sale;
import com.ibtehaj.Ecom.Models.SaleItem;
import com.ibtehaj.Ecom.Service.CustomerService;
import com.ibtehaj.Ecom.Service.ProductService;
import com.ibtehaj.Ecom.Service.SaleItemService;
import com.ibtehaj.Ecom.Service.SaleService;
import com.ibtehaj.Ecom.Service.StockService;

import graphql.schema.DataFetchingEnvironment;

public class DataFetchers {

	@Autowired
	private ProductService productService;
	@Autowired
	private StockService stockService;
	@Autowired
	private CustomerService customerService;
	@Autowired
	private SaleService saleService;
	@Autowired
	private SaleItemService saleItemService;

	public DataFetchers(ProductService productService, StockService stockService, CustomerService customerService,
			SaleService saleService, SaleItemService saleItemService) {
		this.productService = productService;
		this.stockService = stockService;
		this.customerService = customerService;
		this.saleService = saleService;
		this.saleItemService = saleItemService;
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

	// get all product Stocks for a product
	public List<ProductStock> productStocks(DataFetchingEnvironment dfe) {
		// Get the product ID from the arguments
		Long productId = Long.parseLong(dfe.getArgument("productId"));

		// Retrieve the product stocks by product ID using the stockService
		return stockService.getProductStocksByProductId(productId);
	}

	// get all stocks
	public List<ProductStock> allProductStocks(DataFetchingEnvironment dfe) {

		// Retrieve the all product stocks using the stockService
		return stockService.getAllStocks();
	}

	// Get customer profile by id
	public CustomerProfile getCustomerProfileById(DataFetchingEnvironment dfe) {
		// Get the customer ID from the arguments
		Long customerId = Long.parseLong(dfe.getArgument("customerId"));

		// Retrieve the customer by customer ID using the customerService
		return customerService.getCustomerProfileById(customerId);

	}

	// Get customer profile by email
	public CustomerProfile getCustomerProfileByEmail(DataFetchingEnvironment dfe) {
		// Get the email from the arguments
		String email = dfe.getArgument("email");

		// Retrieve the customer by email using the customerService
		return customerService.getCustomerProfileByEmail(email);
	}

	// Get all customer profiles
	public List<CustomerProfile> getAllCustomerProfiles(DataFetchingEnvironment dfe) {
		// Retrieve all customer profiles using the customerService
		return customerService.getAllCustomerProfiles();
	}

	// Get sale by ID
	public Sale getSaleById(DataFetchingEnvironment dfe) {
		// Get the sale ID from the arguments
		Long saleId = Long.parseLong(dfe.getArgument("saleId"));

		// Retrieve the sale by sale ID using the saleService
		return saleService.getSaleById(saleId);
	}

	// Get sales by customer ID
	public List<Sale> getSalesByCustomerId(DataFetchingEnvironment dfe) {
		// Get the customer id from the arguments
		Long customerId = Long.parseLong(dfe.getArgument("customerId"));

		// Retrieve the sales associated with the customer using the saleService
		return saleService.getSaleByCustomerId(customerId);
	}

	// Get all sales
	public List<Sale> getAllSales(DataFetchingEnvironment dfe) {
		// Retrieve all sales using the saleService
		return saleService.getAllSales();
	}

	// Get sale item by ID
	public SaleItem getSaleItemById(DataFetchingEnvironment dfe) {
		// Get the sale item ID from the arguments
		Long saleItemId = Long.parseLong(dfe.getArgument("saleItemId"));

		// Retrieve the sale item by sale item ID using the saleItemService
		return saleItemService.getSaleItemById(saleItemId);
	}

	// Get all sale items by saleID
	public List<SaleItem> getAllSaleItemsBySaleId(DataFetchingEnvironment dfe) {
		// Get the sale Id from the arguments
		Long saleId = Long.parseLong(dfe.getArgument("saleId"));

		// Retrieve all sale items associated with the sale using the saleItemService
		return saleItemService.getAllSaleItemsBySaleId(saleId);
	}

	// Get all sale items
	public List<SaleItem> getAllSaleItems(DataFetchingEnvironment dfe) {
		// Retrieve all sale items using the saleItemService
		return saleItemService.getAllSaleItems();
	}

}