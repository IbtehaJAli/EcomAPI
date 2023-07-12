package com.ibtehaj.Ecom;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.ArrayList;

import org.json.JSONObject;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Value;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.util.concurrent.RateLimiter;
import com.stripe.exception.StripeException;
import com.stripe.model.Charge;

@RestController
@RequestMapping("/api/ecom/")
public class Controller {

	// A RateLimiter with a rate limit of 10 requests per second
	RateLimiter rateLimiter = RateLimiter.create(10.0);

	@Value("${okta.oauth2.client-id}")
	private String clientId;
	@Value("${okta.oauth2.client-secret}")
	private String clientSecret;
	@Value("${stripe.secretKey}")
	private String stripeSecretKey;
	@Value("${stripe.publishableKey}")
	private String stripePublishableKey;
	@Autowired
	private final TokenBlacklist blacklist;

	@Autowired
	private final UserRepository userRepository;

	@Autowired
	private final AccessTokenRepository accessTokenRepository;
	
	@Autowired
	private final ProductRepository productRepository;

	@Autowired
	private final AccessTokenUtils accessTokenUtils;

	@Autowired
	private final ProductService productService;

	@Autowired
	private final StockService stockService;
	
	@Autowired
	private final CartService cartService;
	
	@Autowired
	private final CartItemService cartItemService;
	
	@Autowired
	private final CustomerService customerService;
	
	@Autowired
	private final SaleService saleService;
	
	@Autowired
	private final SaleItemService saleItemService;
	
	@Autowired
	private final PasswordResetTokenService passwordResetTokenService;
	
	@Autowired
	private final StripeService stripeService;
	@Autowired
	private final EmailService emailService;
	
	@Autowired
    RabbitTemplate rabbitTemplate;

	public Controller(UserRepository userRepository, TokenBlacklist blacklist,
			AccessTokenRepository accessTokenRepository,ProductRepository productRepository, AccessTokenUtils accessTokenUtils,
			ProductService productService, StockService stockService, CartService cartService, CartItemService cartItemService,
			CustomerService customerService, SaleService saleService, SaleItemService saleItemService,
			PasswordResetTokenService passwordResetTokenService, StripeService stripeService, EmailService emailService) {
		this.userRepository = userRepository;
		this.blacklist = blacklist;
		this.accessTokenRepository = accessTokenRepository;
		this.productRepository = productRepository;
		this.accessTokenUtils = accessTokenUtils;
		this.productService = productService;
		this.stockService = stockService;
		this.cartService = cartService;
		this.cartItemService = cartItemService;
		this.customerService = customerService;
		this.saleService = saleService;
		this.saleItemService = saleItemService;
		this.passwordResetTokenService = passwordResetTokenService;
		this.stripeService = stripeService;
		this.emailService = emailService;
	}
	@PostMapping("createProduct")
	@CheckBlacklist
	public ResponseEntity<?> createProduct(@Valid @RequestBody ProductRequest request) {
			productService.createProduct(request);
			return ResponseEntity.status(HttpStatus.CREATED).body(new SuccessResponse("Product created successfully."));
		
	}

	@GetMapping("getProduct/{productId}")
	@CheckBlacklist
	public ResponseEntity<Product> getProductById(@PathVariable Long productId) {
		// Retrieve the product by ID
		Product product = productService.getProductById(productId);
		if (product != null) {
			return ResponseEntity.ok(product);
		} else {
			return ResponseEntity.notFound().build();
		}
	}

	@GetMapping("getAllProducts")
	@CheckBlacklist
	public ResponseEntity<List<Product>> getAllProducts() {
		List<Product> products = productService.getAllProducts();
		return ResponseEntity.ok(products);
	}

	@PutMapping("updateProduct/{productId}")
	@CheckBlacklist
	public ResponseEntity<String> updateProduct(@PathVariable Long productId, @RequestBody ProductRequest request) {
		// Update the product
		boolean updated = productService.updateProduct(productId, request);
		if (updated) {
			return ResponseEntity.ok("Product updated successfully");
		} else {
			return ResponseEntity.notFound().build();
		}
	}

	@DeleteMapping("deleteProduct/{productId}")
	@CheckBlacklist
	public ResponseEntity<?> deleteProduct(@PathVariable Long productId) {
		// Delete the product
		boolean deleted = productService.deleteProduct(productId);
		if (deleted) {
			return ResponseEntity.ok(new SuccessResponse("Product deleted successfully"));
		} else {
			ErrorResponse error = new ErrorResponse(HttpStatus.NOT_FOUND.value(),
					"product with id: " + productId + " not found.", System.currentTimeMillis());
			return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
		}
	}
	
	@GetMapping("/products-with-stock-summary")
	public ResponseEntity<List<ProductSummary>> getProductListWithStockSummary() {
	    List<ProductSummary> productListWithStockSummary = productService.getProductListWithStockSummary();
	    return ResponseEntity.ok(productListWithStockSummary);
	}

	@PostMapping("createStock/{productId}")
	@CheckBlacklist
	public ResponseEntity<?> createProductStock(@PathVariable Long productId, @Valid @RequestBody StockRequest request) {

		boolean flag = stockService.createProductStock(productId, request);
		if (flag) {
			return ResponseEntity.status(HttpStatus.CREATED).body(new SuccessResponse("Stock created successfully."));
		} else {
			ErrorResponse error = new ErrorResponse(HttpStatus.BAD_REQUEST.value(),
					"product with id: " + productId + " not found.", System.currentTimeMillis());
			return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
		}

	}

	@GetMapping("getStock/{stockId}")
	@CheckBlacklist
	public ResponseEntity<?> getStockById(@PathVariable Long stockId) {
		// Retrieve the stock by ID
		ProductStock productStock = stockService.getStockById(stockId);
		if (productStock != null) {
			return ResponseEntity.ok(productStock);
		} else {
			return ResponseEntity.notFound().build();
		}
	}

	@GetMapping("getAllStocks")
	@CheckBlacklist
	public ResponseEntity<List<ProductStock>> getAllStocks() {
		List<ProductStock> productStocks = stockService.getAllStocks();
		return ResponseEntity.ok(productStocks);
	}
	
	@GetMapping("getStocksByProduct/{productId}")
	@CheckBlacklist
	public ResponseEntity<?>getStocksByProduct(@PathVariable Long productId){
		Optional<Product> optionalProduct = productRepository.findById(productId);
		if(optionalProduct.isPresent()) {
			Product product = optionalProduct.get();
			List<ProductStock> productStocks = stockService.getStocksByProdcut(product);
			return ResponseEntity.ok(productStocks);
		}else {
			ErrorResponse error = new ErrorResponse(HttpStatus.NOT_FOUND.value(),
					"product with id: " + productId + " not found.", System.currentTimeMillis());
			return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
		}
		
	}

	@PutMapping("updateStock/{stockId}")
	@CheckBlacklist
	public ResponseEntity<?> updateStock(@PathVariable Long stockId, @RequestBody StockRequest request) {
		// Update the stock
		boolean updated = stockService.updateStockforProduct(stockId, request);
		if (updated) {
			return ResponseEntity.ok("Stock updated successfully");
		} else {
			return ResponseEntity.notFound().build();
		}
	}

	@DeleteMapping("deleteStock/{stockId}")
	@CheckBlacklist
	public ResponseEntity<?> deleteStock(@PathVariable Long stockId) {
		// Delete the stock
		boolean deleted = stockService.deleteStockforProduct(stockId);
		if (deleted) {
			return ResponseEntity.ok(new SuccessResponse("Stock deleted successfully"));
		} else {
			ErrorResponse error = new ErrorResponse(HttpStatus.NOT_FOUND.value(),
					"stock with id: " + stockId + " not found.", System.currentTimeMillis());
			return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
		}
	}
	
	@DeleteMapping("deleteAllStockByProduct/{productId}")
	@CheckBlacklist
	public ResponseEntity<?> deleteAllStockByProduct(@PathVariable Long productId) {
		// Delete all stocks
		boolean deleted = stockService.deleteAllStockforProduct(productId);
		if (deleted) {
			return ResponseEntity.ok(new SuccessResponse("All Stocks deleted successfully for product with id: "+productId));
		} else {
			ErrorResponse error = new ErrorResponse(HttpStatus.NOT_FOUND.value(),
					"product with id: " + productId + " not found.", System.currentTimeMillis());
			return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
		}
	}
	
	@PostMapping("createCartItem/{productId}/{quantity}")
	@CheckBlacklist
	public ResponseEntity<?> createCartItem(@PathVariable Long productId, @PathVariable int quantity)
			throws NoAvailableStockException {
		String username = accessTokenUtils.getUsernameFromAccessToken();
		User user = userRepository.findByUsername(username);
		if (user != null) {
			Cart cart = cartService.createOrGetCartByUser(user);
			if (cart != null) {
				Product product = productService.getProductById(productId);
				if (product != null) {
					CartItem existingCartItem = cartItemService.findCartItemByProductAndCart(product,cart);
					if(existingCartItem!=null) {
						existingCartItem.setProduct(product);//
						BigDecimal existingSubTotal = existingCartItem.getSubTotal();
						int existingQuantity = existingCartItem.getQuantity();
						quantity= quantity+existingQuantity;
						existingCartItem.setQuantity(quantity);//
						ProductStockSummary productStockSummary = stockService.getProductStockSummary(product);
						BigDecimal unitPrice = productStockSummary.getWeightedAvgUnitPrice();
						BigDecimal subtotal = unitPrice.multiply(BigDecimal.valueOf(quantity));
						existingCartItem.setSubTotal(subtotal);//
						BigDecimal existingTotalAmount = cart.getTotalAmount();
						BigDecimal difference = existingTotalAmount.subtract(existingSubTotal);
						BigDecimal newTotalAmount = difference.add(subtotal);
						cart.setTotalAmount(newTotalAmount);
						cartService.saveCart(cart);
						existingCartItem.setCart(cart);//
						cartItemService.createCartItem(existingCartItem);
						return ResponseEntity.status(HttpStatus.CREATED)
								.body(new SuccessResponse("CartItem updated successfully."));
						
						
					}else {
						ProductStockSummary productStockSummary = stockService.getProductStockSummary(product);
						BigDecimal unitPrice = productStockSummary.getWeightedAvgUnitPrice();
						BigDecimal subtotal = unitPrice.multiply(BigDecimal.valueOf(quantity));
						CartItem cartItem = new CartItem(product, quantity, subtotal, cart);
						cartItemService.createCartItem(cartItem);
						BigDecimal totalAmount = cart.getTotalAmount();
						BigDecimal finaltotalAmount = totalAmount.add(subtotal);
						cart.setTotalAmount(finaltotalAmount);
						cartService.saveCart(cart);
						return ResponseEntity.status(HttpStatus.CREATED)
								.body(new SuccessResponse("CartItem created successfully."));
					}
					
				} else {
					ErrorResponse error = new ErrorResponse(HttpStatus.NOT_FOUND.value(),
							"product with id: " + productId + " not found.", System.currentTimeMillis());
					return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
				}
			} else {
				ErrorResponse error = new ErrorResponse(HttpStatus.NOT_FOUND.value(), "Cart not found.",
						System.currentTimeMillis());
				return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
			}

		} else {
			ErrorResponse error = new ErrorResponse(HttpStatus.NOT_FOUND.value(),
					"User with user name" + username + " not found.", System.currentTimeMillis());
			return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
		}

	}
	@GetMapping("getAllCartItemsByCart/")
	@CheckBlacklist
	public ResponseEntity<?> getAllCartItemsByCart() {
		String username = accessTokenUtils.getUsernameFromAccessToken();
		User user = userRepository.findByUsername(username);
		if(user != null) {
			Cart cart = cartService.getCartByUser(user);
			if(cart != null) {
				List<CartItem> cartItems= cartItemService.getAllCartItemsByCart(cart);
				return ResponseEntity.ok(cartItems);
			}else {
				ErrorResponse error = new ErrorResponse(HttpStatus.NOT_FOUND.value(),
						"Cart not found.", System.currentTimeMillis());
				return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
			}
		}else {
			ErrorResponse error = new ErrorResponse(HttpStatus.NOT_FOUND.value(),
					"User with user name"+ username+" not found.", System.currentTimeMillis());
			return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
		}
			
	}
	
	@PutMapping("updateCartItem/{cartItemId}/{quantity}")
	@CheckBlacklist
	public ResponseEntity<?> updateCartItem(@PathVariable Long cartItemId, @PathVariable int quantity) throws NoAvailableStockException {
		boolean updated = cartItemService.updateCartItem(cartItemId, quantity);
		if(updated) {
			return ResponseEntity.status(HttpStatus.OK)
					.body(new SuccessResponse("CartItem with id: "+cartItemId+" updated  successfully."));
		}
		else {
			ErrorResponse error = new ErrorResponse(HttpStatus.NOT_FOUND.value(),
					"CartItem with id: "+cartItemId+" was not found.", System.currentTimeMillis());
			return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
		}
		
	}
	@DeleteMapping("deleteCartItemById/{cartItemId}")
	@CheckBlacklist
	public ResponseEntity<?> deleteCartItemById(@PathVariable Long cartItemId) throws CustomAccessDeniedException{
		String username = accessTokenUtils.getUsernameFromAccessToken();
		User user = userRepository.findByUsername(username);
		boolean deleted = false;
		if(user!=null) {
		 deleted = cartItemService.deleteCartItemById(cartItemId,user);
		}
		if(deleted) {
			return ResponseEntity.status(HttpStatus.OK)
					.body(new SuccessResponse("CartItem with id: "+cartItemId+" deleted  successfully."));
		}else {
			ErrorResponse error = new ErrorResponse(HttpStatus.NOT_FOUND.value(),
					"CartItem with id: "+cartItemId+" was not found.", System.currentTimeMillis());
			return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
			}

	}

	@DeleteMapping("emptyCart/")
	@CheckBlacklist
	public ResponseEntity<?> emptyCart() {
		String username = accessTokenUtils.getUsernameFromAccessToken();
		User user = userRepository.findByUsername(username);
		if (user != null) {
			Cart cart = cartService.getCartByUser(user);
			if (cart != null) {
				boolean deleted = cartItemService.deleteAllCartItemsforCart(cart.getId());
				if (deleted) {
					return ResponseEntity.status(HttpStatus.OK)
							.body(new SuccessResponse("Cart with id: " + cart.getId() + " is empty now."));
				} else {
					ErrorResponse error = new ErrorResponse(HttpStatus.NOT_FOUND.value(),
							"Cart with id: " + cart.getId() + " was not found.", System.currentTimeMillis());
					return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
				}
			} else {
				ErrorResponse error = new ErrorResponse(HttpStatus.NOT_FOUND.value(), "Cart not found.",
						System.currentTimeMillis());
				return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
			}
		} else {
			ErrorResponse error = new ErrorResponse(HttpStatus.NOT_FOUND.value(),
					"User with user name" + username + " not found.", System.currentTimeMillis());
			return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
		}

	}

	@PostMapping("checkout/")
	@CheckBlacklist
	public ResponseEntity<?> checkout(@RequestParam("stripeToken") String stripeToken)
			throws NoAvailableStockException, StripeException {
		String username = accessTokenUtils.getUsernameFromAccessToken();
		User user = userRepository.findByUsername(username);
		if (user != null) {
			Cart cart = cartService.getCartByUser(user);
			if (cart != null) {
				List<CartItem> cartItems = cartItemService.getAllCartItemsByCart(cart);
				if (!cartItems.isEmpty()) {
					CustomerProfile customer = customerService.getOrCreateCustomerProfile(
							user.getFirstName() + " " + user.getLastName(), user.getEmail(), user.getPhone(),
							user.getAddress());
					Sale sale = new Sale(LocalDateTime.now(), BigDecimal.ZERO, customer, BigDecimal.ZERO, null);
					// Create a list of product codes to send in the message
					List<String> productCodes = new ArrayList<>();
					BigDecimal finalTotalAmount = BigDecimal.ZERO;
					for (CartItem cartItem : cartItems) {
						Product product = cartItem.getProduct();
						int quantity = cartItem.getQuantity();
						ProductStockSummary productStockSummary = stockService.getProductStockSummary(product);
						if (quantity > productStockSummary.getTotalAvailableUnits()) {
							throw new NoAvailableStockException("Stock not available for " + product.getCode());
						}
						BigDecimal unitPrice = productStockSummary.getWeightedAvgUnitPrice();
						BigDecimal subtotal = unitPrice.multiply(BigDecimal.valueOf(quantity));
						BigDecimal totalAmount = sale.getTotalAmount();
						finalTotalAmount = totalAmount.add(subtotal);
						saleService.saveSale(sale);
					}

					try {
						Charge charge = stripeService.ProcessPayment(stripeSecretKey, finalTotalAmount, sale, customer,
								stripeToken);
						// Check if the payment source is a card
						JSONObject sourceJson = new JSONObject(charge.getSource());
						if (sourceJson.getString("object").equals("card")) {
							// Extract information about the card payment source and set the sale payment
							// mode
							sale.setPaymentMode(sourceJson.getString("brand") + " " + sourceJson.getString("object"));
						} else {
							sale.setPaymentMode("unknown");
						}

					} catch (StripeException e) {
						saleService.deleteSale(sale);
						ErrorResponse error = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), e.getMessage(),
								System.currentTimeMillis());
						return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
					}

					for (CartItem cartItem : cartItems) {
						Product product = cartItem.getProduct();
						ProductStock productStock = stockService.getOldestAvailableStockByProduct(product);
						int quantity = cartItem.getQuantity();
						ProductStockSummary productStockSummary = stockService.getProductStockSummary(product);
						if (quantity > productStockSummary.getTotalAvailableUnits()) {
							throw new NoAvailableStockException("Stock not available for " + product.getCode());
						}
						BigDecimal unitPrice = productStockSummary.getWeightedAvgUnitPrice();
						BigDecimal subtotal = unitPrice.multiply(BigDecimal.valueOf(quantity));
						BigDecimal totalAmount = sale.getTotalAmount();
						BigDecimal finalTotalAmount2 = totalAmount.add(subtotal);
						sale.setTotalAmount(finalTotalAmount2);
						// sale.setPaymentMode(paymentType);
						saleService.saveSale(sale);
						while (quantity > 0) {
							if (quantity > productStock.getAvailableUnits()) {
								// Create a sale item with the available units
								SaleItem saleItem = new SaleItem(sale, productStock,
										productStock.getAvailableUnits().intValue(),
										BigDecimal.valueOf(productStock.getAvailableUnits()).multiply(unitPrice),
										unitPrice, BigDecimal.ZERO);
								saleItemService.createSaleItem(saleItem);
								// Reduce the stock
								Long availableUnits = productStock.getAvailableUnits();
								Long newAvailableUnits = 0L;
								productStock.setAvailableUnits(newAvailableUnits);
								stockService.saveStock(productStock);
								// Add the product code to the list
								productCodes.add(product.getCode());
								// Update the quantity and get the next available stock
								quantity = quantity - availableUnits.intValue();
								productStock = stockService.getOldestAvailableStockByProduct(product);
							} else {
								// Create a sale item with the requested quantity
								SaleItem saleItem = new SaleItem(sale, productStock, quantity,
										BigDecimal.valueOf(quantity).multiply(unitPrice), unitPrice, BigDecimal.ZERO);
								saleItemService.createSaleItem(saleItem);
								// Reduce the stock
								Long availableUnits = productStock.getAvailableUnits();
								Long newAvailableUnits = availableUnits - quantity;
								productStock.setAvailableUnits(newAvailableUnits);
								stockService.saveStock(productStock);
								// Add the product code to the list
								productCodes.add(product.getCode());
								// Set the quantity to 0 to exit the loop
								quantity = 0;
							}
						}
					}

					// Send a single message to update the product stock for all cart items
					for (String productCode : productCodes) {
						rabbitTemplate.convertAndSend("product.stock.update", productCode);
						System.out.println("Product stock update messages sent for cart ");
					}
					// Wait for 2.5 second to ensure that the deletion message is processed after
					// sending the product stock update messages
					try {
						Thread.sleep(2500);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					// Delete all cart items for the cart in a new thread
					String id = Long.toString(cart.getId());
					rabbitTemplate.convertAndSend("checkout", id);
					System.out.println("Deletion message sent for cart ");

					return ResponseEntity.status(HttpStatus.CREATED).body(new SuccessResponse(
							username + " your order with order id:" + sale.getId() + " is created successfully."));
				} else {
					ErrorResponse error = new ErrorResponse(HttpStatus.NO_CONTENT.value(),
							username + "'s cart is empty.", System.currentTimeMillis());
					return new ResponseEntity<>(error, HttpStatus.NO_CONTENT);
				}
			} else {
				ErrorResponse error = new ErrorResponse(HttpStatus.NOT_FOUND.value(), "Cart not found.",
						System.currentTimeMillis());
				return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
			}
		} else {
			ErrorResponse error = new ErrorResponse(HttpStatus.NOT_FOUND.value(),
					"User with user name" + username + " not found.", System.currentTimeMillis());
			return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
		}
	}
	
	@GetMapping("getAllSaleItemsBySale/")
	@CheckBlacklist
	public ResponseEntity<?> getAllSaleItemsBySale() {
		String username = accessTokenUtils.getUsernameFromAccessToken();
		User user = userRepository.findByUsername(username);
		if(user != null) {
			System.out.println(user.getEmail());
			CustomerProfile customer = customerService.getCustomerProfileByEmail(user.getEmail());
			//System.out.println(customer.getEmail());
			if(customer!= null) {
				List<Sale> sales = saleService.getSaleByCustomerProfile(customer);
				if(!sales.isEmpty()) {
					List<SaleItem> saleItems = new ArrayList<>();
					for(Sale sale : sales) {
						saleItems.addAll(saleItemService.getAllSaleItemsBySale(sale));
					}
					
					return ResponseEntity.ok(saleItems);
				}else {
					ErrorResponse error = new ErrorResponse(HttpStatus.NOT_FOUND.value(),
							"No sale found.", System.currentTimeMillis());
					return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
				}
			
			}else {
				ErrorResponse error = new ErrorResponse(HttpStatus.NOT_FOUND.value(),
						"Customer not found.", System.currentTimeMillis());
				return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
			}
			
		}else {
			ErrorResponse error = new ErrorResponse(HttpStatus.NOT_FOUND.value(),
					"User with user name" + username + " not found.", System.currentTimeMillis());
			return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
		}
			
	}
	
	@GetMapping("getAllSaleItemsBySaleForAdmin/{customerId}")
	@CheckBlacklist
	@RestrictedToAdmin
	public ResponseEntity<?> getAllSaleItemsBySaleForAdmin(@PathVariable Long customerId) {
			CustomerProfile customer = customerService.getCustomerProfileById(customerId);
			//System.out.println(customer.getEmail());
			if(customer!= null) {
				List<Sale> sales = saleService.getSaleByCustomerProfile(customer);
				if(!sales.isEmpty()) {
					List<SaleItem> saleItems = new ArrayList<>();
					for(Sale sale : sales) {
						saleItems.addAll(saleItemService.getAllSaleItemsBySale(sale));
					}
					
					return ResponseEntity.ok(saleItems);
				}else {
					ErrorResponse error = new ErrorResponse(HttpStatus.NOT_FOUND.value(),
							"No sale found.", System.currentTimeMillis());
					return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
				}
			
			}else {
				ErrorResponse error = new ErrorResponse(HttpStatus.NOT_FOUND.value(),
						"Customer not found.", System.currentTimeMillis());
				return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
			}	
	}

	@DeleteMapping("deleteAllSaleItemsforSale/{saleId}")
	@CheckBlacklist
	public ResponseEntity<?> deleteAllSaleItemsforSale(@PathVariable Long saleId) throws CustomAccessDeniedException{
		 String username = accessTokenUtils.getUsernameFromAccessToken();
		    User user = userRepository.findByUsername(username);
		    if (user != null) {
		    	CustomerProfile customer = customerService.getCustomerProfileByEmail(user.getEmail());
		    	if(customer != null) {
		    		boolean deleted = saleItemService.deleteAllSaleItemsforSale(saleId, customer);
		    		if(deleted) {
		    			 return ResponseEntity.status(HttpStatus.OK)
			                        .body(new SuccessResponse(username+" your order with order id:"+ saleId+" is deleted successfully."));
		    		}else {
		    			 ErrorResponse error = new ErrorResponse(HttpStatus.NOT_FOUND.value(),
		    					"Sale with" + saleId + " not found.", System.currentTimeMillis());
		    			return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
		    		}
		    	}else {
					ErrorResponse error = new ErrorResponse(HttpStatus.NOT_FOUND.value(),
							"Customer not found.", System.currentTimeMillis());
					return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
				}
		    }else {
   			 ErrorResponse error = new ErrorResponse(HttpStatus.NOT_FOUND.value(),
 					"User with user name" + username + " not found.", System.currentTimeMillis());
 			return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
 		}
	}
	
	@DeleteMapping("deleteAllSaleItemsforSaleForAdmin/{saleId}/{customerId}")
	@CheckBlacklist
	@RestrictedToAdmin
	public ResponseEntity<?> deleteAllSaleItemsforSaleForAdmin(@PathVariable Long saleId, @PathVariable Long customerId) throws CustomAccessDeniedException{
		    	CustomerProfile customer = customerService.getCustomerProfileById(customerId);
		    	if(customer != null) {
		    		boolean deleted = saleItemService.deleteAllSaleItemsforSale(saleId, customer);
		    		if(deleted) {
		    			 return ResponseEntity.status(HttpStatus.OK)
			                        .body(new SuccessResponse("order for "+customer.getCustomerName()+" with order id:"+ saleId+" is deleted successfully."));
		    		}else {
		    			 ErrorResponse error = new ErrorResponse(HttpStatus.NOT_FOUND.value(),
		    					"Sale with" + saleId + " not found.", System.currentTimeMillis());
		    			return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
		    		}
		    	}else {
					ErrorResponse error = new ErrorResponse(HttpStatus.NOT_FOUND.value(),
							"Customer not found.", System.currentTimeMillis());
					return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
				}
	}
	
	@GetMapping("getAllSales")
	@CheckBlacklist
	@RestrictedToAdmin
	public ResponseEntity<?> getAllSales(){
		List<Sale> sales = saleService.getAllSales();
		return ResponseEntity.status(HttpStatus.OK).body(sales);
	}
	
	
	@GetMapping("getAllCustomerProfiles")
	@CheckBlacklist
	@RestrictedToAdmin
	public ResponseEntity<?> getAllCustomerProfiles(){
		List<CustomerProfile> customerProfiles = customerService.getAllCustomerProfiles();
		return ResponseEntity.status(HttpStatus.OK).body(customerProfiles);
	}
	
	@GetMapping("getAllUsers")
	@CheckBlacklist
	@RestrictedToAdmin
	public ResponseEntity<?> getAllUsers(){
		List<User> users = userRepository.findAll();
		return ResponseEntity.status(HttpStatus.OK).body(users);
	}
	
	@PostMapping("/signup")
	public ResponseEntity<?> signUp(@Valid @RequestBody SignUpRequest userRequest) {

		// Acquire a permit from the RateLimiter
		if (!rateLimiter.tryAcquire()) {
			// If a permit is not available, return a 'Too Many Requests' error response
			ErrorResponse errorResponse = new ErrorResponse(HttpStatus.TOO_MANY_REQUESTS.value(), "Too many requests. ",
					System.currentTimeMillis());
			return new ResponseEntity<>(errorResponse, HttpStatus.TOO_MANY_REQUESTS);
		}

		// create user account and return success response
		// Check if username already exists
		boolean flag = true;
		if (userRepository.findByUsername(userRequest.getUsername()) != null) {
			flag = false;
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse(HttpStatus.BAD_REQUEST.value(),
					"Username is already taken", System.currentTimeMillis()));
		}

		// Check if email is already registered
		if (userRepository.findByEmail(userRequest.getEmail()) != null) {
			flag = false;
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse(HttpStatus.BAD_REQUEST.value(),
					"Email is already registered", System.currentTimeMillis()));
		}

		if (flag) {
			// Create user
			User user = new User(userRequest.getUsername(), userRequest.getPassword(), userRequest.getEmail(),
			userRequest.getFirstName(), userRequest.getLastName(), userRequest.getPhone(), userRequest.getAddress(), Collections.singleton(UserRole.ROLE_USER));
			userRepository.save(user);
			return ResponseEntity.ok(new SuccessResponse("User registered successfully"));
		}

		return ResponseEntity.status(HttpStatus.BAD_REQUEST)
				.body(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "Couldnt signup", System.currentTimeMillis()));
	}

	@PostMapping("/login")
	public ResponseEntity<?> login(@Valid @RequestBody LoginRequest userRequest)
			throws JsonMappingException, JsonProcessingException {

		// Acquire a permit from the RateLimiter
		if (!rateLimiter.tryAcquire()) {
			// If a permit is not available, return a 'Too Many Requests' error response
			ErrorResponse errorResponse = new ErrorResponse(HttpStatus.TOO_MANY_REQUESTS.value(), "Too many requests. ",
					System.currentTimeMillis());
			return new ResponseEntity<>(errorResponse, HttpStatus.TOO_MANY_REQUESTS);
		}

		// authenticate user and return success response
		User user = userRepository.findByEmail(userRequest.getEmail());

		// Check if user does not exist or password does not match
		if (user == null || !user.getPassword().equals(userRequest.getPassword())) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse(HttpStatus.BAD_REQUEST.value(),
					"Incorrect username/email or password", System.currentTimeMillis()));
		} else {
			AccessTokens access_token = accessTokenRepository.findByUsername(user.getUsername());
			if (access_token != null) {
				System.out.println(access_token.getUsername());
				System.out.println(access_token.getAccess_token());
				accessTokenRepository.delete(access_token);
			}

			// Set the basic authentication credentials

			String credentials = clientId + ":" + clientSecret;
			String encodedCredentials = Base64.getEncoder().encodeToString(credentials.getBytes());

			// Set the request headers
			HttpHeaders headers = new HttpHeaders();
			headers.set("Authorization", "Basic" + encodedCredentials);
			headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

			// Set the request body
			MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
			body.add("grant_type", "client_credentials");
			body.add("scope", "gettoken");

			// Set the request entity
			HttpEntity<org.springframework.util.MultiValueMap<String, String>> requestEntity = new HttpEntity<>(body,
					headers);

			// Set the endpoint URL
			String url = "https://dev-85342491.okta.com/oauth2/default/v1/token";
			try {
				// Send the request and retrieve the response
				RestTemplate restTemplate = new RestTemplate();
				ResponseEntity<String> responseEntity = restTemplate.exchange(url, HttpMethod.POST, requestEntity,
						String.class);
				String response = responseEntity.getBody();
				// Create an ObjectMapper instance:
				ObjectMapper objectMapper = new ObjectMapper();

				// Convert the string to a JsonNode object:
				JsonNode jsonNode = objectMapper.readTree(response);

				String AccessToken = jsonNode.get("access_token").asText();
				AccessTokens at = new AccessTokens(AccessToken, user.getUsername());

				accessTokenRepository.save(at);

				// Return the access token as a JSON response
				return ResponseEntity.ok(jsonNode);
			} catch (HttpClientErrorException e) {
				// Handle 404 error
				System.err.println("Endpoint not found: " + url);
			}

		}

		// Login successful
		return ResponseEntity.ok(new SuccessResponse("Login successful"));
	}

	@PostMapping("/logout")
	public ResponseEntity<?> logout(@Valid @RequestBody LogoutRequest userRequest) {

		// Acquire a permit from the RateLimiter
		if (!rateLimiter.tryAcquire()) {
			// If a permit is not available, return a 'Too Many Requests' error response
			ErrorResponse errorResponse = new ErrorResponse(HttpStatus.TOO_MANY_REQUESTS.value(), "Too many requests. ",
					System.currentTimeMillis());
			return new ResponseEntity<>(errorResponse, HttpStatus.TOO_MANY_REQUESTS);
		}

		String token = userRequest.getToken();
		blacklist.add(token);
		System.out.println(token);
		// Build the request URL
		String url = "https://dev-85342491.okta.com/oauth2/default/v1/revoke";

		// Set the request headers
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
		headers.setBasicAuth(clientId, clientSecret);

		// Build the request body
		MultiValueMap<String, String> requestBody = new LinkedMultiValueMap<>();
		requestBody.add("token", token);
		requestBody.add("token_type_hint", "access_token");

		// Build the request entity
		HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(requestBody, headers);

		// Send the request
		RestTemplate restTemplate = new RestTemplate();
		ResponseEntity<String> responseEntity = restTemplate.postForEntity(url, requestEntity, String.class);
		// String response = responseEntity.getBody();
		HttpStatus responseStatus = HttpStatus.valueOf(responseEntity.getStatusCode().value());

		// Handle the response
		if (responseStatus.is2xxSuccessful()) {
			return ResponseEntity.ok(new SuccessResponse("Token revocation successful"));
		} else {
			return ResponseEntity.status(responseStatus)
					.body("Token revocation failed with status code " + responseStatus.value());
		}
	}

	@PostMapping("/reset-password/initiate")
	public ResponseEntity<?> initiatePasswordReset(@Valid @RequestBody ResetPasswordRequest resetPasswordRequest) {

		// Acquire a permit from the RateLimiter
		if (!rateLimiter.tryAcquire()) {
			// If a permit is not available, return a 'Too Many Requests' error response
			ErrorResponse errorResponse = new ErrorResponse(HttpStatus.TOO_MANY_REQUESTS.value(), "Too many requests. ",
					System.currentTimeMillis());
			return new ResponseEntity<>(errorResponse, HttpStatus.TOO_MANY_REQUESTS);
		}

		User user = userRepository.findByEmail(resetPasswordRequest.getEmail());

		if (user == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
					new ErrorResponse(HttpStatus.NOT_FOUND.value(), "User not found", System.currentTimeMillis()));
		}

		PasswordResetToken token = new PasswordResetToken();
		token.setUser(user);
		token.setExpiryDate(LocalDateTime.now().plusHours(1));
		token.setToken(UUID.randomUUID().toString());

		passwordResetTokenService.save(token);

		String resetUrl = "http://localhost:8080/api/todos/reset-password/confirm?token=" + token.getToken();

		emailService.sendResetPasswordConfirmationEmail(user.getEmail(), resetUrl);

		return ResponseEntity.status(HttpStatus.OK)
				.body(new SuccessResponse("reset link send to your email, please check your inbox"));
	}

	@PostMapping("/reset-password/confirm")
	public ResponseEntity<?> confirmPasswordReset(@RequestParam String token,
			@Valid @RequestBody ResetPasswordRequest2 rpr2) {

		// Acquire a permit from the RateLimiter
		if (!rateLimiter.tryAcquire()) {
			// If a permit is not available, return a 'Too Many Requests' error response
			ErrorResponse errorResponse = new ErrorResponse(HttpStatus.TOO_MANY_REQUESTS.value(), "Too many requests. ",
					System.currentTimeMillis());
			return new ResponseEntity<>(errorResponse, HttpStatus.TOO_MANY_REQUESTS);
		}

		PasswordResetToken passwordResetToken = passwordResetTokenService.findByToken(token);

		if (passwordResetToken == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
					new ErrorResponse(HttpStatus.NOT_FOUND.value(), "token not found", System.currentTimeMillis()));
		}

		if (passwordResetToken.getExpiryDate().isBefore(LocalDateTime.now())) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse(HttpStatus.BAD_REQUEST.value(),
					"this token is expired", System.currentTimeMillis()));
		}

		User user = passwordResetToken.getUser();
		user.setPassword(rpr2.getPassword());

		userRepository.save(user);
		passwordResetTokenService.delete(passwordResetToken);

		return ResponseEntity.status(HttpStatus.OK)
				.body(new SuccessResponse("password for " + user.getEmail() + " has been reset"));
	}

}