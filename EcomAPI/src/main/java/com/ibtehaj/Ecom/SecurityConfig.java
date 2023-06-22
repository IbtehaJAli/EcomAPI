package com.ibtehaj.Ecom;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@ComponentScan(basePackages = "com.ibtehaj.todo")
public class SecurityConfig {
	
	@SuppressWarnings("removal")
	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		// This method configures the security features for the application and returns a SecurityFilterChain object

		return http
				.csrf().disable() // This disables Cross-Site Request Forgery (CSRF) protection
				.authorizeHttpRequests(auth -> {
					auth.requestMatchers("/api/ecom/login").permitAll(); // This allows unauthenticated access to the "/api/ecom/login" endpoint
					auth.requestMatchers("/api/ecom/signup").permitAll(); // This allows unauthenticated access to the "/api/ecom/signup" endpoint
					auth.requestMatchers("/api/ecom/logout").permitAll(); // This allows unauthenticated access to the "/api/ecom/logout" endpoint
					auth.requestMatchers("/api/ecom/reset-password/initiate").permitAll(); // This allows unauthenticated access to the "/initiate" endpoint
					auth.requestMatchers("/api/ecom/reset-password/confirm").permitAll(); // This allows unauthenticated access to the "/confirm" endpoint
					auth.anyRequest().authenticated(); // This requires authentication for all other endpoints
				})
				.sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // This sets the session creation policy to "STATELESS"
				.oauth2ResourceServer((oauth2ResourceServer) ->
				oauth2ResourceServer
				.jwt() // This enables JSON Web Token (JWT) authentication for incoming requests
						)
				.build(); // This builds the SecurityFilterChain object with the configured security features
	}
}