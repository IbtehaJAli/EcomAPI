package com.ibtehaj.Ecom.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.math.BigDecimal;
import java.util.ArrayList;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import com.ibtehaj.Ecom.Controller.Controller;
import com.ibtehaj.Ecom.Exception.CustomAccessDeniedException;
import com.ibtehaj.Ecom.Exception.NoAvailableStockException;
import com.ibtehaj.Ecom.Repository.UserRepository;
import com.ibtehaj.Ecom.Service.CartItemService;
import com.ibtehaj.Ecom.Service.CartService;
import com.ibtehaj.Ecom.Service.ProductService;
import com.ibtehaj.Ecom.Service.StockService;
import com.ibtehaj.Ecom.Utils.AccessTokenUtils;
import com.ibtehaj.Ecom.Models.User;
import com.ibtehaj.Ecom.Models.Cart;
import com.ibtehaj.Ecom.Models.CartItem;
import com.ibtehaj.Ecom.Models.Product;
import com.ibtehaj.Ecom.Models.ProductStockSummary;
import com.ibtehaj.Ecom.Response.*;



@RunWith(MockitoJUnitRunner.class)
public class CartControllerTest {

    @InjectMocks
    private Controller cartController;

    @Mock
    private AccessTokenUtils accessTokenUtils;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CartService cartService;

    @Mock
    private CartItemService cartItemService;
    
    @Mock
    private ProductService productService;
    
    @Mock
    private StockService stockService;
    

    @Test
    public void testEmptyCartSuccess() throws CustomAccessDeniedException {
        // given
        String username = "testUser";
        User user = new User();
        user.setUsername(username);
        Cart cart = new Cart();
        cart.setId(1L);
        cart.setUser(user);

        // when
        Mockito.when(accessTokenUtils.getUsernameFromAccessToken()).thenReturn(username);
        Mockito.when(userRepository.findByUsername(username)).thenReturn(user);
        Mockito.when(cartService.getCartByUser(user)).thenReturn(cart);
        Mockito.when(cartItemService.deleteAllCartItemsforCart(cart.getId())).thenReturn(true);

        // then
        ResponseEntity<?> response = cartController.emptyCart();
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody() instanceof SuccessResponse);
        SuccessResponse successResponse = (SuccessResponse) response.getBody();
        assertEquals("Cart with id: 1 is empty now.", successResponse.message());
    }

    @Test
    public void testEmptyCartFailCartNotFound() throws CustomAccessDeniedException {
        // given
        String username = "testUser";
        User user = new User();
        user.setUsername(username);

        // when
        Mockito.when(accessTokenUtils.getUsernameFromAccessToken()).thenReturn(username);
        Mockito.when(userRepository.findByUsername(username)).thenReturn(user);
        Mockito.when(cartService.getCartByUser(user)).thenReturn(null);

        // then
        ResponseEntity<?> response = cartController.emptyCart();
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertTrue(response.getBody() instanceof ErrorResponse);
        ErrorResponse errorResponse = (ErrorResponse) response.getBody();
        assertEquals(HttpStatus.NOT_FOUND.value(), errorResponse.getStatus());
        assertEquals("Cart not found.", errorResponse.getMessage());
    }
    
    @Test
    public void testEmptyCartFailCartWithIdNotFound() throws CustomAccessDeniedException {
        // given
        String username = "testUser";
        User user = new User();
        user.setUsername(username);
        Cart cart = new Cart();
        cart.setId(1L);
        cart.setUser(user);

        // when
        Mockito.when(accessTokenUtils.getUsernameFromAccessToken()).thenReturn(username);
        Mockito.when(userRepository.findByUsername(username)).thenReturn(user);
        Mockito.when(cartService.getCartByUser(user)).thenReturn(cart);
        Mockito.when(cartItemService.deleteAllCartItemsforCart(cart.getId())).thenReturn(false);

        // then
        ResponseEntity<?> response = cartController.emptyCart();
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertTrue(response.getBody() instanceof ErrorResponse);
        ErrorResponse errorResponse = (ErrorResponse) response.getBody();
        assertEquals(HttpStatus.NOT_FOUND.value(), errorResponse.getStatus());
        assertEquals("Cart with id: 1 was not found.", errorResponse.getMessage());
    }


    @Test
    public void testEmptyCartFailUserNotFound() throws CustomAccessDeniedException {
        // given
        String username = "testUser";

        // when
        Mockito.when(accessTokenUtils.getUsernameFromAccessToken()).thenReturn(username);
        Mockito.when(userRepository.findByUsername(username)).thenReturn(null);

        // then
        ResponseEntity<?> response = cartController.emptyCart();
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertTrue(response.getBody() instanceof ErrorResponse);
        ErrorResponse errorResponse = (ErrorResponse) response.getBody();
        assertEquals(HttpStatus.NOT_FOUND.value(), errorResponse.getStatus());
        assertEquals("User with user name testUser not found.", errorResponse.getMessage());
    }
    
    @Test
    public void testGetAllCartItemsByCartSuccess() throws CustomAccessDeniedException {
        // given
        String username = "testUser";
        User user = new User();
        user.setUsername(username);
        Cart cart = new Cart();
        cart.setId(1L);
        cart.setUser(user);
        List<CartItem> cartItems = new ArrayList<>();
        cartItems.add(new CartItem(new Product("testProduct", "sp01", "{}"), 2,BigDecimal.valueOf(10),cart));

        // when
        Mockito.when(accessTokenUtils.getUsernameFromAccessToken()).thenReturn(username);
        Mockito.when(userRepository.findByUsername(username)).thenReturn(user);
        Mockito.when(cartService.getCartByUser(user)).thenReturn(cart);
        Mockito.when(cartItemService.getAllCartItemsByCart(cart)).thenReturn(cartItems);

        // then
        ResponseEntity<?> response = cartController.getAllCartItemsByCart();
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody() instanceof List<?>);
        List<?> responseBody = (List<?>) response.getBody();
        assertEquals(1, responseBody.size());
        assertTrue(responseBody.get(0) instanceof CartItem);
        CartItem cartItem = (CartItem) responseBody.get(0);
        assertEquals(BigDecimal.valueOf(10), cartItem.getSubTotal());
        assertEquals(cart, cartItem.getCart());
        assertEquals("testProduct", cartItem.getProduct().getProductName());
        assertEquals(2, cartItem.getQuantity());
    }
    
    @Test
    public void testGetAllCartItemsByCartFailCartNotFound() throws CustomAccessDeniedException {
        // given
        String username = "testUser";
        User user = new User();
        user.setUsername(username);

        // when
        Mockito.when(accessTokenUtils.getUsernameFromAccessToken()).thenReturn(username);
        Mockito.when(userRepository.findByUsername(username)).thenReturn(user);
        Mockito.when(cartService.getCartByUser(user)).thenReturn(null);

        // then
        ResponseEntity<?> response = cartController.getAllCartItemsByCart();
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertTrue(response.getBody() instanceof ErrorResponse);
        ErrorResponse errorResponse = (ErrorResponse) response.getBody();
        assertEquals(HttpStatus.NOT_FOUND.value(), errorResponse.getStatus());
        assertEquals("Cart not found.", errorResponse.getMessage());
    }

    @Test
    public void testGetAllCartItemsByCartFailUserNotFound() throws CustomAccessDeniedException {
        // given
        String username = "testUser";

        // when
        Mockito.when(accessTokenUtils.getUsernameFromAccessToken()).thenReturn(username);
        Mockito.when(userRepository.findByUsername(username)).thenReturn(null);

        // then
        ResponseEntity<?> response = cartController.getAllCartItemsByCart();
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertTrue(response.getBody() instanceof ErrorResponse);
        ErrorResponse errorResponse = (ErrorResponse) response.getBody();
        assertEquals(HttpStatus.NOT_FOUND.value(), errorResponse.getStatus());
        assertEquals("User with user name testUser not found.", errorResponse.getMessage());
    }
    
    @Test
    public void testUpdateCartItem() throws NoAvailableStockException {
        // given
        Long cartItemId = 1L;
        int quantity = 5;

        // when cart item is updated successfully
        Mockito.when(cartItemService.updateCartItem(cartItemId, quantity)).thenReturn(true);

        // then
        ResponseEntity<?> response = cartController.updateCartItem(cartItemId, quantity);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody() instanceof SuccessResponse);
        SuccessResponse successResponse = (SuccessResponse) response.getBody();
        assertEquals("CartItem with id: 1 updated successfully.", successResponse.message());

        // when cart item is not found
        Mockito.when(cartItemService.updateCartItem(cartItemId, quantity)).thenReturn(false);

        // then
        response = cartController.updateCartItem(cartItemId, quantity);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertTrue(response.getBody() instanceof ErrorResponse);
        ErrorResponse errorResponse = (ErrorResponse) response.getBody();
        assertEquals(HttpStatus.NOT_FOUND.value(), errorResponse.getStatus());
        assertEquals("CartItem with id: 1 was not found.", errorResponse.getMessage());
    }
    
    @Test
    public void testDeleteCartItemById() throws CustomAccessDeniedException {
        // given
        String username = "testUser";
        User user = new User();
        user.setUsername(username);
        Long cartItemId = 1L;

        // when user exists and cart item is deleted successfully
        Mockito.when(accessTokenUtils.getUsernameFromAccessToken()).thenReturn(username);
        Mockito.when(userRepository.findByUsername(username)).thenReturn(user);
        Mockito.when(cartItemService.deleteCartItemById(cartItemId, user)).thenReturn(true);

        // then
        ResponseEntity<?> response = cartController.deleteCartItemById(cartItemId);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody() instanceof SuccessResponse);
        SuccessResponse successResponse = (SuccessResponse) response.getBody();
        assertEquals("CartItem with id: 1 deleted successfully.", successResponse.message());

        // when user exists but cart item is not found
        Mockito.when(cartItemService.deleteCartItemById(cartItemId, user)).thenReturn(false);

        // then
        response = cartController.deleteCartItemById(cartItemId);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertTrue(response.getBody() instanceof ErrorResponse);
        ErrorResponse errorResponse = (ErrorResponse) response.getBody();
        assertEquals(HttpStatus.NOT_FOUND.value(), errorResponse.getStatus());
        assertEquals("CartItem with id: 1 was not found.", errorResponse.getMessage());

        // when user is null
        Mockito.when(userRepository.findByUsername(username)).thenReturn(null);

        // then
        response = cartController.deleteCartItemById(cartItemId);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertTrue(response.getBody() instanceof ErrorResponse);
        errorResponse = (ErrorResponse) response.getBody();
        assertEquals(HttpStatus.NOT_FOUND.value(), errorResponse.getStatus());
        assertEquals("CartItem with id: 1 was not found.", errorResponse.getMessage());
    }
    
	@Test
	public void testCreateCartItem() throws NoAvailableStockException, CustomAccessDeniedException {
		// given
		Long productId = 1L;
		int quantity = 5;
		String username = "testUser";
		User user = new User();
		user.setUsername(username);
		Cart cart = new Cart();
		Product product = new Product();
		product.setId(productId);

		// when user exists, cart exists, product exists, and cart item already exists
		CartItem existingCartItem = new CartItem();
		existingCartItem.setProduct(product);
		existingCartItem.setQuantity(3);
		existingCartItem.setSubTotal(BigDecimal.valueOf(15));
		BigDecimal existingTotalAmount = BigDecimal.valueOf(100);
		cart.setTotalAmount(existingTotalAmount);
		Mockito.when(accessTokenUtils.getUsernameFromAccessToken()).thenReturn(username);
		Mockito.when(userRepository.findByUsername(username)).thenReturn(user);
		Mockito.when(cartService.createOrGetCartByUser(user)).thenReturn(cart);
		Mockito.when(productService.getProductById(productId)).thenReturn(product);
		Mockito.when(cartItemService.findCartItemByProductAndCart(product, cart)).thenReturn(existingCartItem);
		ProductStockSummary productStockSummary = new ProductStockSummary();
		productStockSummary.setWeightedAvgUnitPrice(BigDecimal.valueOf(5));
		Mockito.when(stockService.getProductStockSummary(product)).thenReturn(productStockSummary);
		Mockito.doNothing().when(cartItemService).createCartItem(existingCartItem);

		// then
		ResponseEntity<?> response = cartController.createCartItem(productId, quantity);
		assertEquals(HttpStatus.CREATED, response.getStatusCode());
		assertTrue(response.getBody() instanceof SuccessResponse);
		SuccessResponse successResponse = (SuccessResponse) response.getBody();
		assertEquals("CartItem updated successfully.", successResponse.message());

		// when user exists, cart exists, product exists, and cart item does not exist
		Mockito.when(cartItemService.findCartItemByProductAndCart(product, cart)).thenReturn(null);

		// then
		response = cartController.createCartItem(productId, quantity);
		assertEquals(HttpStatus.CREATED, response.getStatusCode());
		assertTrue(response.getBody() instanceof SuccessResponse);
		successResponse = (SuccessResponse) response.getBody();
		assertEquals("CartItem created successfully.", successResponse.message());

		// when user exists, cart exists, product does not exist
		Mockito.when(productService.getProductById(productId)).thenReturn(null);

		// then
		response = cartController.createCartItem(productId, quantity);
		assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
		assertTrue(response.getBody() instanceof ErrorResponse);
		ErrorResponse errorResponse = (ErrorResponse) response.getBody();
		assertEquals(HttpStatus.NOT_FOUND.value(), errorResponse.getStatus());
		assertEquals("product with id: 1 not found.", errorResponse.getMessage());

		// when user exists, cart does not exist
		Mockito.when(cartService.createOrGetCartByUser(user)).thenReturn(null);

		// then
		response = cartController.createCartItem(productId, quantity);
		assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
		assertTrue(response.getBody() instanceof ErrorResponse);
		errorResponse = (ErrorResponse) response.getBody();
		assertEquals(HttpStatus.NOT_FOUND.value(), errorResponse.getStatus());
		assertEquals("Cart not found.", errorResponse.getMessage());

		// when user does not exist
		Mockito.when(userRepository.findByUsername(username)).thenReturn(null);

		// then
		response = cartController.createCartItem(productId, quantity);
		assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
		assertTrue(response.getBody() instanceof ErrorResponse);
		errorResponse = (ErrorResponse) response.getBody();
		assertEquals(HttpStatus.NOT_FOUND.value(), errorResponse.getStatus());
		assertEquals("User with user name testUser not found.", errorResponse.getMessage());
	}
}
