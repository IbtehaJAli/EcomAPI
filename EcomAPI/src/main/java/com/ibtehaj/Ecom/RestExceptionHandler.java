package com.ibtehaj.Ecom;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import jakarta.persistence.PersistenceException;

import org.postgresql.util.PSQLException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class RestExceptionHandler {

	// Define an exception handler for MethodArgumentNotValidException
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ExceptionResponse> handleValidationException(MethodArgumentNotValidException e) {
		// Get the list of validation errors from the MethodArgumentNotValidException
		List<String> errors = e.getBindingResult().getFieldErrors()
				.stream()
				.map(error -> error.getDefaultMessage()) // Map each error object to its default error message
				.collect(Collectors.toList()); // Convert the stream of error messages to a list

		// Create an ExceptionResponse object with the validation errors
		ExceptionResponse errorResponse = new ExceptionResponse(errors);

		// Return a response entity with HTTP status code 400 (Bad Request) and the error response object as the body
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
	}

	// Define an exception handler for TokenRevokedException
	@ExceptionHandler(TokenRevokedException.class)
	public ResponseEntity<ExceptionResponse> handleTokenRevokedException(TokenRevokedException e) {
		// Create an ExceptionResponse object with the error message
		List<String> errors = new ArrayList<>();
		errors.add(e.getMessage());
		ExceptionResponse errorResponse = new ExceptionResponse(errors);

		// Return a response entity with HTTP status code 401 (Unauthorized) and the error response object as the body
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
	}

	// Define an exception handler for TokenRevokedException
	@ExceptionHandler(CustomAccessDeniedException.class)
	public ResponseEntity<ExceptionResponse> handleCustomAccessDeniedException(CustomAccessDeniedException e) {
		// Create an ExceptionResponse object with the error message
		List<String> errors = new ArrayList<>();
		errors.add(e.getMessage());
		ExceptionResponse errorResponse = new ExceptionResponse(errors);

		// Return a response entity with HTTP status code 401 (Unauthorized) and the error response object as the body
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
	}

	// Define an exception handler for PersistenceException and its subclasses
	@ExceptionHandler(PersistenceException.class)
	public ResponseEntity<ExceptionResponse> handlePersistenceException(PersistenceException e) {
		// Check if the cause of the PersistenceException is a DataIntegrityViolationException
		Throwable cause = e.getCause();
		if (cause instanceof DataIntegrityViolationException) {
			// If the cause is a DataIntegrityViolationException, get the error message from it
			DataIntegrityViolationException dive = (DataIntegrityViolationException) cause;
			String errorMessage = dive.getMostSpecificCause().getMessage();

			// Create an ExceptionResponse object with the error message
			List<String> errors = new ArrayList<>();
			errors.add(errorMessage);
			ExceptionResponse errorResponse = new ExceptionResponse(errors);

			// Return a response entity with HTTP status code 400 (Bad Request) and the error response object as the body
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
		} else if (cause instanceof PSQLException) {
			// If the cause is a PSQLException, get the error message from it
			PSQLException psqlException = (PSQLException) cause;
			String errorMessage = psqlException.getMessage();

			if (errorMessage.startsWith("ERROR: duplicate key value violates unique constraint \"blacklisted_tokens_token_value_key\"\n")) {
				// If the error message starts with the specific string, return a response entity with HTTP status code 400 (Bad Request) and the "You are already logged out" message as the body
				List<String> errors = new ArrayList<>();
				errors.add("You are already logged out.");
				ExceptionResponse errorResponse = new ExceptionResponse(errors);
				return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
			} else {
				// If the error message does not start with the specific string, create an ExceptionResponse object with the error message
				// Create an ExceptionResponse object with the error message
				List<String> errors = new ArrayList<>();
				errors.add(errorMessage);
				ExceptionResponse errorResponse = new ExceptionResponse(errors);

				// Return a response entity with HTTP status code 400 (Bad Request) and the error response object as the body
				return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
			}
		}
		else {
			// If the cause is not a DataIntegrityViolationException or PSQLException, add the cause's message to the list of errors
			List<String> errors = new ArrayList<>();
			errors.add(cause.getMessage());
			ExceptionResponse errorResponse = new ExceptionResponse(errors);

			// Return a response entity with HTTP status code 400 (Bad Request) and the error response object as the body
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
		}
	}
}