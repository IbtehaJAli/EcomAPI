package com.ibtehaj.Ecom.Requests;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class ResetPasswordRequest {
	@NotBlank(message = "Please provide email")
    @Email(message = "Please provide a valid email address")
    private String email;

    // getters and setters
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	
}
