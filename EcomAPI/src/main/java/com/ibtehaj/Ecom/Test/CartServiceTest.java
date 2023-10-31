package com.ibtehaj.Ecom.Test;

// Import the necessary libraries for testing
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

// Import the classes that are involved in the service
import com.ibtehaj.Ecom.Models.Cart;
import com.ibtehaj.Ecom.Models.User;
import com.ibtehaj.Ecom.Models.UserRole;
import com.ibtehaj.Ecom.Repository.UserRepository;
import com.ibtehaj.Ecom.Service.CartService;

// Annotate the class as a Spring Boot test
@SpringBootTest
public class CartServiceTest {

	@Autowired
	private UserRepository userRepository;
    // Autowire the service that needs to be tested
    @Autowired
    private CartService cartService;

    // Write test cases for each method in the service
    @Test
    @Transactional
    @Rollback
    public void testSaveCart() {
        // Create a mock user and cart object
    	User user = new User(
    		    "john.doe",         // username
    		    "password123",      // password
    		    "john@example.com", // email
    		    "John",             // firstName
    		    "Doe",              // lastName
    		    "1234567890",       // phone
    		    "123 Main St",      // address
    		    Collections.singleton(UserRole.ROLE_USER)               // set of roles
    		);
    	userRepository.save(user);
        Cart cart = new Cart(user, BigDecimal.valueOf(100));

        // Save the cart using the service method
        cartService.saveCart(cart);

        // Assert that the cart is saved and has an id
        assertNotNull(cart.getId());
        
        
    }

    @Test
    @Transactional
    @Rollback
    public void testCreateCart() {
        // Create a mock user object
    	User user = new User(
    		    "john.doe",         // username
    		    "password123",      // password
    		    "john@example.com", // email
    		    "John",             // firstName
    		    "Doe",              // lastName
    		    "1234567890",       // phone
    		    "123 Main St",      // address
    		    Collections.singleton(UserRole.ROLE_USER)               // set of roles
    		);
    	userRepository.save(user);
        // Create a cart using the service method
        cartService.createCart(user);

        // Get the cart by user using the service method
        Cart cart = cartService.getCartByUser(user);

        // Assert that the cart is created and has an id
        assertNotNull(cart.getId());
    }

    @Test
    @Transactional
    @Rollback
    public void testGetCartById() {
        // Create a mock user and cart object
    	User user = new User(
    		    "john.doe",         // username
    		    "password123",      // password
    		    "john@example.com", // email
    		    "John",             // firstName
    		    "Doe",              // lastName
    		    "1234567890",       // phone
    		    "123 Main St",      // address
    		    Collections.singleton(UserRole.ROLE_USER)               // set of roles
    		);
    	userRepository.save(user);
        Cart cart = new Cart(user, BigDecimal.valueOf(100));

        // Save the cart using the service method
        cartService.saveCart(cart);

        // Get the cart by id using the service method
        Cart foundCart = cartService.getCartById(cart.getId());

        // Assert that the found cart is not null and has the same id as the saved cart
        assertNotNull(foundCart);
        assertEquals(cart.getId(), foundCart.getId());
    }

    @Test
    @Transactional
    @Rollback
    public void testGetCartByUser() {
        // Create a mock user and cart object
    	User user = new User(
    		    "john.doe",         // username
    		    "password123",      // password
    		    "john@example.com", // email
    		    "John",             // firstName
    		    "Doe",              // lastName
    		    "1234567890",       // phone
    		    "123 Main St",      // address
    		    Collections.singleton(UserRole.ROLE_USER)               // set of roles
    		);
    	userRepository.save(user);
        Cart cart = new Cart(user, BigDecimal.valueOf(100));

        // Save the cart using the service method
        cartService.saveCart(cart);

        // Get the cart by user using the service method
        Cart foundCart = cartService.getCartByUser(user);

        // Assert that the found cart is not null and has the same user as the saved cart
        assertNotNull(foundCart);
        assertEquals(cart.getUser().getId(), foundCart.getUser().getId());
    }

    @Test
    @Transactional
    @Rollback
    public void testGetAllCarts() {
        // Create two mock users and carts objects
    	User user1 = new User(
    		    "john.doe",         // username
    		    "password123",      // password
    		    "john@example.com", // email
    		    "John",             // firstName
    		    "Doe",              // lastName
    		    "1234567890",       // phone
    		    "123 Main St",      // address
    		    Collections.singleton(UserRole.ROLE_USER)               // set of roles
    		);
    	userRepository.save(user1);
    	User user2 = new User(
    		    "jane.doe",           // username
    		    "secretPassword",     // password
    		    "jane@example.com",   // email
    		    "Jane",               // firstName
    		    "Doe",                // lastName
    		    "9876543210",         // phone
    		    "456 Elm St",         // address
    		    Collections.singleton(UserRole.ROLE_USER)      // set of roles
    		);
    	userRepository.save(user2);
        Cart cart1 = new Cart(user1, BigDecimal.valueOf(100));
        Cart cart2 = new Cart(user2, BigDecimal.valueOf(200));

        // Save both carts using the service method
        cartService.saveCart(cart1);
        cartService.saveCart(cart2);

        // Get all carts using the service method
        List<Cart> carts = cartService.getAllCarts();

        System.out.println(carts.size());
        // Assert that the list of carts is not null and has two elements
        assertNotNull(carts);
        assertEquals(2, carts.size());
    }

    @Test
    @Transactional
    @Rollback
    public void testCreateOrGetCartByUser() {
        
         // Create a mock user object without a cart 
    	User user1 = new User(
    		    "john.doe",         // username
    		    "password123",      // password
    		    "john@example.com", // email
    		    "John",             // firstName
    		    "Doe",              // lastName
    		    "1234567890",       // phone
    		    "123 Main St",      // address
    		    Collections.singleton(UserRole.ROLE_USER)               // set of roles
    		);
    	userRepository.save(user1);
         // Create or get a cart by user using the service method 
         Cart createdCart = cartService.createOrGetCartByUser(user1); 

         // Assert that the created cart is not null and has an id 
         assertNotNull(createdCart); 
         assertNotNull(createdCart.getId()); 

         // Create another mock user object with a pre-existing cart 
         User user2 = new User(
     		    "jane.doe",           // username
     		    "secretPassword",     // password
     		    "jane@example.com",   // email
     		    "Jane",               // firstName
     		    "Doe",                // lastName
     		    "9876543210",         // phone
     		    "456 Elm St",         // address
     		    Collections.singleton(UserRole.ROLE_USER)      // set of roles
     		); 
         userRepository.save(user2);
         Cart existingCart = new Cart(user2, BigDecimal.valueOf(200)); 

         // Save the existing cart using the service method 
         cartService.saveCart(existingCart); 

         // Create or get a cart by user using the service method 
         Cart retrievedCart = cartService.createOrGetCartByUser(user2); 

         // Assert that the retrieved cart is not null and has the same id as the existing cart 
         assertNotNull(retrievedCart); 
         assertEquals(existingCart.getId(), retrievedCart.getId()); 
    }
}
