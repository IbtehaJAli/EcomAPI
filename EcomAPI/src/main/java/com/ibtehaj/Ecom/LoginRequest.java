package com.ibtehaj.Ecom;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class LoginRequest {
	@NotBlank(message = "Please provide password")
	private String password;

	@NotBlank(message = "Please provide email")
	@Email(message = "Please provide a valid email address")
	private String email;

	public LoginRequest() {
	};

	public LoginRequest(String password, String email) {
		super();
		this.password = password;
		this.email = email;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

}
