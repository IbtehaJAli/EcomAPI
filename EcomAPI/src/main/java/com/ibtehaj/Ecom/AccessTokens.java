package com.ibtehaj.Ecom;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "access_tokens")
public class AccessTokens {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@Column(name = "token_value", length = 50000, unique = true)
	private String accesstoken;
	private String username;
	
	public AccessTokens() {};
	
	public AccessTokens(String access_token, String user_name) {
		super();
		this.accesstoken = access_token;
		this.username = user_name;
	}

	public String getAccess_token() {
		return accesstoken;
	}

	public void setAccess_token(String access_token) {
		this.accesstoken = access_token;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String user_name) {
		this.username = user_name;
	}

}
