package com.ibtehaj.Ecom.Requests;

import jakarta.validation.constraints.NotBlank;

public class LogoutRequest {
	@NotBlank(message = "Please provide token") 
	String token;
	public LogoutRequest() {}
	public LogoutRequest(String token) {
		super();
		this.token = token;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	
}
