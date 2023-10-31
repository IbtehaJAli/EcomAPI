package com.ibtehaj.Ecom.Controller;

import java.io.IOException;
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
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
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
import org.springframework.data.domain.Page;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.util.concurrent.RateLimiter;
import com.ibtehaj.Ecom.Annotation.CheckBlacklist;
import com.ibtehaj.Ecom.Annotation.RestrictedToAdmin;
import com.ibtehaj.Ecom.Exception.CustomAccessDeniedException;
import com.ibtehaj.Ecom.Exception.NoAvailableStockException;
import com.ibtehaj.Ecom.GraphQL.DataFetchers;
import com.ibtehaj.Ecom.GraphQL.GraphqlSchemaLoader;
import com.ibtehaj.Ecom.Models.AccessTokens;
import com.ibtehaj.Ecom.Models.Cart;
import com.ibtehaj.Ecom.Models.CartItem;
import com.ibtehaj.Ecom.Models.CustomerProfile;
import com.ibtehaj.Ecom.Models.PasswordResetToken;
import com.ibtehaj.Ecom.Models.Product;
import com.ibtehaj.Ecom.Models.ProductStock;
import com.ibtehaj.Ecom.Models.ProductStockSummary;
import com.ibtehaj.Ecom.Models.ProductSummary;
import com.ibtehaj.Ecom.Models.Review;
import com.ibtehaj.Ecom.Models.Sale;
import com.ibtehaj.Ecom.Models.SaleItem;
import com.ibtehaj.Ecom.Models.SaleStatus;
import com.ibtehaj.Ecom.Models.User;
import com.ibtehaj.Ecom.Models.UserRole;
import com.ibtehaj.Ecom.Repository.AccessTokenRepository;
import com.ibtehaj.Ecom.Repository.ProductRepository;
import com.ibtehaj.Ecom.Repository.UserRepository;
import com.ibtehaj.Ecom.Requests.LoginRequest;
import com.ibtehaj.Ecom.Requests.LogoutRequest;
import com.ibtehaj.Ecom.Requests.ProductRequest;
import com.ibtehaj.Ecom.Requests.ResetPasswordRequest;
import com.ibtehaj.Ecom.Requests.ResetPasswordRequest2;
import com.ibtehaj.Ecom.Requests.ReviewRequest;
import com.ibtehaj.Ecom.Requests.SignUpRequest;
import com.ibtehaj.Ecom.Requests.StockRequest;
import com.ibtehaj.Ecom.Response.ErrorResponse;
import com.ibtehaj.Ecom.Response.SuccessResponse;
import com.ibtehaj.Ecom.Service.CartItemService;
import com.ibtehaj.Ecom.Service.CartService;
import com.ibtehaj.Ecom.Service.CustomerService;
import com.ibtehaj.Ecom.Service.EmailService;
import com.ibtehaj.Ecom.Service.PasswordResetTokenService;
import com.ibtehaj.Ecom.Service.ProductService;
import com.ibtehaj.Ecom.Service.ReviewService;
import com.ibtehaj.Ecom.Service.SaleItemService;
import com.ibtehaj.Ecom.Service.SaleService;
import com.ibtehaj.Ecom.Service.StockService;
import com.ibtehaj.Ecom.Service.StripeService;
import com.ibtehaj.Ecom.Service.TokenBlacklist;
import com.ibtehaj.Ecom.Utils.AccessTokenUtils;
import com.stripe.exception.StripeException;
import com.stripe.model.Charge;

import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.schema.GraphQLSchema;

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
	private final ReviewService reviewService;
	
	@Autowired
    RabbitTemplate rabbitTemplate;

	public Controller(UserRepository userRepository, TokenBlacklist blacklist,
			AccessTokenRepository accessTokenRepository,ProductRepository productRepository, AccessTokenUtils accessTokenUtils,
			ProductService productService, StockService stockService, CartService cartService, CartItemService cartItemService,
			CustomerService customerService, SaleService saleService, SaleItemService saleItemService,
			PasswordResetTokenService passwordResetTokenService, StripeService stripeService, EmailService emailService,
			ReviewService reviewService) {
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
		this.reviewService = reviewService;
	}
	@PostMapping("createProduct")
	@CheckBlacklist
	@RestrictedToAdmin
	public ResponseEntity<?> createProduct(@Valid @RequestBody ProductRequest request) {
			productService.createProduct(request);
			return ResponseEntity.status(HttpStatus.CREATED).body(new SuccessResponse("Product created successfully."));
		
	}

	@GetMapping("getProduct/{productId}")
	@CheckBlacklist
	@RestrictedToAdmin
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
	@RestrictedToAdmin
	public ResponseEntity<List<Product>> getAllProducts() {
		List<Product> products = productService.getAllProducts();
		return ResponseEntity.ok(products);
	}

	@PutMapping("updateProduct/{productId}")
	@CheckBlacklist
	@RestrictedToAdmin
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
	@RestrictedToAdmin
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
	@CheckBlacklist
	public ResponseEntity<?> getSortedProductListWithStockSummary( 
			@RequestParam(name = "sortBy", required = false) String sortBy,
			@RequestParam(name = "page", defaultValue = "1") int pageNumber,
	        @RequestParam(name = "size", defaultValue = "2") int pageSize) {
		if(pageNumber == 0) {
			ErrorResponse error = new ErrorResponse(HttpStatus.BAD_REQUEST.value(),
					"0 is not a valid page number.", System.currentTimeMillis());
			return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
		}
	    List<ProductSummary> productListWithStockSummary = productService.getProductListWithStockSummary();
	    Page<ProductSummary> page = productService.getSortedproductListWithStockSummary(productListWithStockSummary, sortBy, pageNumber,pageSize);
	    return ResponseEntity.ok(page);
	}
	
	@GetMapping("/searchProductListByKeyword")
	@CheckBlacklist
	public ResponseEntity<List<ProductSummary>> searchProductListByKeyword( @RequestParam("keyword") String keyword) {
	    List<ProductSummary> productListWithStockSummary = productService.getProductListWithStockSummary();
	    List<ProductSummary> matchedProductsList = productService.searchProductListByKeyword(productListWithStockSummary,keyword);
	    return ResponseEntity.ok(matchedProductsList);
	}

	@PostMapping("createStock/{productId}")
	@CheckBlacklist
	@RestrictedToAdmin
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
	@RestrictedToAdmin
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
	@RestrictedToAdmin
	public ResponseEntity<List<ProductStock>> getAllStocks() {
		List<ProductStock> productStocks = stockService.getAllStocks();
		return ResponseEntity.ok(productStocks);
	}
	
	@GetMapping("getStocksByProduct/{productId}")
	@CheckBlacklist
	@RestrictedToAdmin
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
	@RestrictedToAdmin
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
	@RestrictedToAdmin
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
	@RestrictedToAdmin
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
			throws NoAvailableStockException, CustomAccessDeniedException {
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
					"User with user name " + username + " not found.", System.currentTimeMillis());
			return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
		}

	}
	@GetMapping("getAllCartItemsByCart/")
	@CheckBlacklist
	public ResponseEntity<?> getAllCartItemsByCart() throws CustomAccessDeniedException {
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
					"User with user name "+ username+" not found.", System.currentTimeMillis());
			return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
		}
			
	}
	
	@PutMapping("updateCartItem/{cartItemId}/{quantity}")
	@CheckBlacklist
	public ResponseEntity<?> updateCartItem(@PathVariable Long cartItemId, @PathVariable int quantity) throws NoAvailableStockException {
		boolean updated = cartItemService.updateCartItem(cartItemId, quantity);
		if(updated) {
			return ResponseEntity.status(HttpStatus.OK)
					.body(new SuccessResponse("CartItem with id: "+cartItemId+" updated successfully."));
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
					.body(new SuccessResponse("CartItem with id: "+cartItemId+" deleted successfully."));
		}else {
			ErrorResponse error = new ErrorResponse(HttpStatus.NOT_FOUND.value(),
					"CartItem with id: "+cartItemId+" was not found.", System.currentTimeMillis());
			return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
			}

	}

	@DeleteMapping("emptyCart/")
	@CheckBlacklist
	public ResponseEntity<?> emptyCart() throws CustomAccessDeniedException {
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
					"User with user name " + username + " not found.", System.currentTimeMillis());
			return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
		}

	}

	@PostMapping("checkout/")
	@CheckBlacklist
	public ResponseEntity<?> checkout(@RequestParam("stripeToken") String stripeToken)
			throws NoAvailableStockException, StripeException, CustomAccessDeniedException {
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
					Sale sale = new Sale(LocalDateTime.now(), BigDecimal.ZERO, customer, BigDecimal.ZERO, null, SaleStatus.PENDING);
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
					//System.out.println("Deletion message sent for cart ");
					//Send confirmation email to the customer
					emailService.sendSaleConfirmationEmail(sale);
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
	public ResponseEntity<?> getAllSaleItemsBySale() throws CustomAccessDeniedException {
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
	
	@PutMapping("/updateSaleStatus/{saleId}")
	@CheckBlacklist
	@RestrictedToAdmin
	public ResponseEntity<?> updateSaleStatus(@PathVariable Long saleId, @RequestBody SaleStatus status) {
	    boolean updated = saleService.updateSaleStatus(saleId, status);
	    if (updated) {
	    	return ResponseEntity.status(HttpStatus.OK)
                    .body(new SuccessResponse("Sale status for sale with id: "+saleId+" updated successfully"));
	    } else {
	    	ErrorResponse error = new ErrorResponse(HttpStatus.NOT_FOUND.value(),
					"Sale with sale id: "+saleId+" not found", System.currentTimeMillis());
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
	
	@GetMapping("/getAllReviews")
	@CheckBlacklist
	@RestrictedToAdmin
	public ResponseEntity<?> getAllReviews() {
		List<Review> reviews = reviewService.getAllReviews();

		if (reviews.isEmpty()) {
			ErrorResponse error = new ErrorResponse(HttpStatus.NOT_FOUND.value(), "No reviews found",
					System.currentTimeMillis());
			return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
		}

		return ResponseEntity.status(HttpStatus.OK).body(reviews);
	}

	@PostMapping("createReview/{productId}")
	@CheckBlacklist
	public ResponseEntity<?> createReview(@PathVariable Long productId, @RequestBody @Valid ReviewRequest reviewRequest)
			throws CustomAccessDeniedException {
		String username = accessTokenUtils.getUsernameFromAccessToken();
		User user = userRepository.findByUsername(username);
		boolean flag = false;
		if (user != null) {
			CustomerProfile customer = customerService.getCustomerProfileByEmail(user.getEmail());
			if (customer != null) {
				Product product = productService.getProductById(productId);
				if (product != null) {
					boolean reviewExists = reviewService.doesReviewExist(productId, customer.getId());
					if (reviewExists) {
						ErrorResponse error = new ErrorResponse(HttpStatus.BAD_REQUEST.value(),
								"A review for this product already exists by the same customer.",
								System.currentTimeMillis());
						return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
					}
					List<Sale> sales = saleService.getSaleByCustomerProfile(customer);
					if (!sales.isEmpty()) {
						for (Sale sale : sales) {
							List<SaleItem> saleItems = saleItemService.getAllSaleItemsBySale(sale);
							for (SaleItem saleItem : saleItems) {
								if (productId == saleItem.getProductStock().getProduct().getId()) {
									flag = true;
									break; // Break out of the inner loop (saleItems loop)
								}
							}
							if (flag) {
								break; // Break out of the outer loop (sales loop)
							}
						}
						if (!flag) {
							ErrorResponse error = new ErrorResponse(HttpStatus.FORBIDDEN.value(),
									username + ", please buy this product first", System.currentTimeMillis());
							return new ResponseEntity<>(error, HttpStatus.FORBIDDEN);
						} else {
							Review review = new Review(customer, reviewRequest.getRating(), reviewRequest.getComment(),
									product);
							reviewService.createReview(review);
							return ResponseEntity.status(HttpStatus.OK)
									.body(new SuccessResponse("Review for product with id :" + productId
											+ " has been created successfully by " + username));
						}
					} else {
						ErrorResponse error = new ErrorResponse(HttpStatus.FORBIDDEN.value(),
								username + ", please make your first purchase by buying this product",
								System.currentTimeMillis());
						return new ResponseEntity<>(error, HttpStatus.FORBIDDEN);

					}
				} else {
					ErrorResponse error = new ErrorResponse(HttpStatus.NOT_FOUND.value(),
							"Cant create a review as product with product id: " + productId + " not found",
							System.currentTimeMillis());
					return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
				}
			} else {
				ErrorResponse error = new ErrorResponse(HttpStatus.FORBIDDEN.value(),
						username + ", please make your first purchase by buying this product",
						System.currentTimeMillis());
				return new ResponseEntity<>(error, HttpStatus.FORBIDDEN);

			}
		} else {
			ErrorResponse error = new ErrorResponse(HttpStatus.NOT_FOUND.value(),
					"User with user name" + username + " not found.", System.currentTimeMillis());
			return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
		}
	}

	@GetMapping("getReviewByProductIdAndCustomer/{productId}")
	@CheckBlacklist
	public ResponseEntity<?> getReviewByProductIdAndCustomer(@PathVariable Long productId)
			throws CustomAccessDeniedException {
		String username = accessTokenUtils.getUsernameFromAccessToken();
		User user = userRepository.findByUsername(username);
		if (user == null) {
			ErrorResponse error = new ErrorResponse(HttpStatus.NOT_FOUND.value(),
					"User with username " + username + " not found.", System.currentTimeMillis());
			return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
		}

		CustomerProfile customer = customerService.getCustomerProfileByEmail(user.getEmail());
		if (customer == null) {
			ErrorResponse error = new ErrorResponse(HttpStatus.FORBIDDEN.value(),
					username + ", please make your first purchase by buying this product", System.currentTimeMillis());
			return new ResponseEntity<>(error, HttpStatus.FORBIDDEN);
		}

		Product product = productService.getProductById(productId);
		if (product == null) {
			ErrorResponse error = new ErrorResponse(HttpStatus.NOT_FOUND.value(),
					"Product with product id: " + productId + " not found", System.currentTimeMillis());
			return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
		}

		Review review = reviewService.getReviewByProductAndCustomer(productId, customer.getId());
		if (review == null) {
			ErrorResponse error = new ErrorResponse(HttpStatus.NOT_FOUND.value(),
					"No review found for product with id: " + productId + " by " + username,
					System.currentTimeMillis());
			return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
		}

		return ResponseEntity.status(HttpStatus.OK).body(review);
	}

	@GetMapping("/getProductReviews/{productId}")
	@CheckBlacklist
	public ResponseEntity<?> getProductReviews(@PathVariable Long productId) {
		Product product = productService.getProductById(productId);

		if (product == null) {
			ErrorResponse error = new ErrorResponse(HttpStatus.NOT_FOUND.value(),
					"Product with product id: " + productId + " not found", System.currentTimeMillis());
			return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
		}

		List<Review> reviews = reviewService.findReviewsByProduct(product);

		if (reviews.isEmpty()) {
			ErrorResponse error = new ErrorResponse(HttpStatus.NOT_FOUND.value(),
					"No reviews found for product with id: " + productId, System.currentTimeMillis());
			return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
		}

		return ResponseEntity.status(HttpStatus.OK).body(reviews);
	}

	@GetMapping("/getCustomerReviews")
	@CheckBlacklist
	public ResponseEntity<?> getCustomerReviews() throws CustomAccessDeniedException {
		String username = accessTokenUtils.getUsernameFromAccessToken();
		User user = userRepository.findByUsername(username);

		if (user == null) {
			ErrorResponse error = new ErrorResponse(HttpStatus.NOT_FOUND.value(),
					"User with username " + username + " not found.", System.currentTimeMillis());
			return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
		}

		CustomerProfile customer = customerService.getCustomerProfileByEmail(user.getEmail());

		if (customer == null) {
			ErrorResponse error = new ErrorResponse(HttpStatus.NOT_FOUND.value(),
					"Customer with username " + username + " not found.", System.currentTimeMillis());
			return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
		}

		List<Review> reviews = reviewService.findReviewsByCustomer(customer);

		if (reviews.isEmpty()) {
			ErrorResponse error = new ErrorResponse(HttpStatus.NOT_FOUND.value(),
					"No reviews found for customer with username: " + username, System.currentTimeMillis());
			return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
		}

		return ResponseEntity.status(HttpStatus.OK).body(reviews);
	}

	@PutMapping("/updateReview/{reviewId}")
	@CheckBlacklist
	public ResponseEntity<?> updateReview(@PathVariable Long reviewId, @RequestBody @Valid ReviewRequest reviewRequest)
			throws CustomAccessDeniedException {
		String username = accessTokenUtils.getUsernameFromAccessToken();
		User user = userRepository.findByUsername(username);

		if (user == null) {
			ErrorResponse error = new ErrorResponse(HttpStatus.NOT_FOUND.value(),
					"User with username " + username + " not found.", System.currentTimeMillis());
			return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
		}

		CustomerProfile customer = customerService.getCustomerProfileByEmail(user.getEmail());

		if (customer == null) {
			ErrorResponse error = new ErrorResponse(HttpStatus.NOT_FOUND.value(),
					"Customer with username " + username + " not found.", System.currentTimeMillis());
			return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
		}

		Optional<Review> optionalReview = reviewService.findReviewById(reviewId);

		if (!optionalReview.isPresent()) {
			ErrorResponse error = new ErrorResponse(HttpStatus.NOT_FOUND.value(),
					"Review with ID: " + reviewId + " not found", System.currentTimeMillis());
			return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
		}

		Review existingReview = optionalReview.get();

		if (existingReview.getCustomer().getId() != customer.getId()) {
			ErrorResponse error = new ErrorResponse(HttpStatus.FORBIDDEN.value(),
					"You are not authorized to update this review", System.currentTimeMillis());
			return new ResponseEntity<>(error, HttpStatus.FORBIDDEN);
		}

		existingReview.setRating(reviewRequest.getRating());
		existingReview.setComment(reviewRequest.getComment());

		reviewService.updateReview(existingReview);

		return ResponseEntity.status(HttpStatus.OK)
				.body(new SuccessResponse("Review with ID: " + reviewId + " has been updated successfully"));
	}

	@DeleteMapping("/deleteReview/{reviewId}")
	@CheckBlacklist
	public ResponseEntity<?> deleteReview(@PathVariable Long reviewId) throws CustomAccessDeniedException {
		String username = accessTokenUtils.getUsernameFromAccessToken();
		User user = userRepository.findByUsername(username);

		if (user == null) {
			ErrorResponse error = new ErrorResponse(HttpStatus.NOT_FOUND.value(),
					"User with username " + username + " not found.", System.currentTimeMillis());
			return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
		}

		CustomerProfile customer = customerService.getCustomerProfileByEmail(user.getEmail());

		if (customer == null) {
			ErrorResponse error = new ErrorResponse(HttpStatus.NOT_FOUND.value(),
					"Customer with username " + username + " not found.", System.currentTimeMillis());
			return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
		}

		Optional<Review> optionalReview = reviewService.findReviewById(reviewId);

		if (!optionalReview.isPresent()) {
			ErrorResponse error = new ErrorResponse(HttpStatus.NOT_FOUND.value(),
					"Review with ID: " + reviewId + " not found", System.currentTimeMillis());
			return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
		}

		Review existingReview = optionalReview.get();

		if (existingReview.getCustomer().getId() != customer.getId()) {
			ErrorResponse error = new ErrorResponse(HttpStatus.FORBIDDEN.value(),
					"You are not authorized to delete this review", System.currentTimeMillis());
			return new ResponseEntity<>(error, HttpStatus.FORBIDDEN);
		}

		reviewService.deleteReviewById(reviewId);

		return ResponseEntity.status(HttpStatus.OK)
				.body(new SuccessResponse("Review with ID: " + reviewId + " has been deleted successfully"));
	}
	
	@DeleteMapping("/deleteReviewForAdmin/{reviewId}")
	@CheckBlacklist
	@RestrictedToAdmin
	public ResponseEntity<?> deleteReviewForAdmin(@PathVariable Long reviewId) {
		Optional<Review> optionalReview = reviewService.findReviewById(reviewId);

		if (!optionalReview.isPresent()) {
			ErrorResponse error = new ErrorResponse(HttpStatus.NOT_FOUND.value(),
					"Review with ID: " + reviewId + " not found", System.currentTimeMillis());
			return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
		}
		reviewService.deleteReviewById(reviewId);

		return ResponseEntity.status(HttpStatus.OK)
				.body(new SuccessResponse("Review with ID: " + reviewId + " has been deleted successfully"));
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
			// encrypt the password
			String hashedPassword = new BCryptPasswordEncoder().encode(userRequest.getPassword());
			// Create user
			User user = new User(userRequest.getUsername(), hashedPassword, userRequest.getEmail(),
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
		if (user == null || !new BCryptPasswordEncoder().matches(userRequest.getPassword(), user.getPassword())) {
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
	
	@PostMapping("/graphql")
	  public String graphqlEndpoint(@RequestBody String query) throws IOException {
		DataFetchers dataFetchers = new DataFetchers(productService,stockService, customerService, saleService, saleItemService);
		GraphQLSchema schema = GraphqlSchemaLoader.loadSchema(dataFetchers);
		GraphQL graphQL = GraphQL.newGraphQL(schema).build();
	    
	    // Construct GraphQL schema and data fetchers
	    
	    ExecutionResult executionResult = graphQL.execute(query);
	    
	    return new ObjectMapper().writeValueAsString(executionResult.getData());
	  }
	
	
}