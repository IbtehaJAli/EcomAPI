package com.ibtehaj.Ecom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

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

@RestController
@RequestMapping("/api/ecom/")
public class Controller {

	// A RateLimiter with a rate limit of 10 requests per second
	RateLimiter rateLimiter = RateLimiter.create(10.0);

	@Value("${okta.oauth2.client-id}")
	private String clientId;
	@Value("${okta.oauth2.client-secret}")
	private String clientSecret;
	@Autowired
	private final TokenBlacklist blacklist;

	@Autowired
	private final UserRepository userRepository;

	@Autowired
	private final AccessTokenRepository accessTokenRepository;

	@Autowired
	private final AccessTokenUtils accessTokenUtils;

	@Autowired
	public Controller(UserRepository userRepository, TokenBlacklist blacklist,
			AccessTokenRepository accessTokenRepository, AccessTokenUtils accessTokenUtils) {
		this.userRepository = userRepository;
		this.blacklist = blacklist;
		this.accessTokenRepository = accessTokenRepository;
		this.accessTokenUtils = accessTokenUtils;
	}

	
	@Autowired
	private PasswordResetTokenService passwordResetTokenService;

	@Autowired
	private EmailService emailService;

	

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
					userRequest.getFirstName(), userRequest.getLastName(), Collections.singleton(UserRole.ROLE_USER));
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