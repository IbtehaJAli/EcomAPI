package com.ibtehaj.Ecom.Requests;



import com.fasterxml.jackson.databind.JsonNode;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class ProductRequest {
	@NotBlank(message = "Please provide product name")
	private String name;
	@NotBlank(message = "Please provide product code")
    private String code;
	@NotNull(message = "Please provide attributes in json format")
    private JsonNode attributes;

    // Getters and setters
    public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	
	public JsonNode getAttributes() {
		return attributes;
	}
	public void setAttributes(JsonNode attributes) {
		this.attributes = attributes;
	}
    
}
