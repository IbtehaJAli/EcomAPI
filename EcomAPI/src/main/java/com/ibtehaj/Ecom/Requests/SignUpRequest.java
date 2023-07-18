package com.ibtehaj.Ecom.Requests;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class SignUpRequest {
    @NotBlank(message = "Please provide user name")
    @Size(min = 1, max = 50, message = "User name must be between 1 and 50 characters")
    private String username;

    @NotBlank(message = "Please provide password")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,}$", message = "Password must be at least 8 characters long and contain at least one lowercase letter, one uppercase letter, and one digit")
    private String password;

    @NotBlank(message = "Please provide email")
    @Email(message = "Please provide a valid email address")
    private String email;

    @NotBlank(message = "Please provide first name")
    @Size(max = 50, message = "First name must be at most 50 characters")
    private String firstName;

    @NotBlank(message = "Please provide last name")
    @Size(max = 50, message = "Last name must be at most 50 characters")
    private String lastName;
    
    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^0[3]\\d{9}$", message = "Phone number must be in the format 03XXXXXXXXX")
    private String phone;

    @NotBlank(message = "Address is required")
    private String address;

    public SignUpRequest() {
    }

    public SignUpRequest(String username, String password, String email, String firstName, String lastName, String phone, String address) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.phone = phone;
		this.address = address;
    }

    // Getters and setters
    
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
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

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}
}