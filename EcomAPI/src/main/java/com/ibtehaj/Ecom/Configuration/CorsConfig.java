package com.ibtehaj.Ecom.Configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableWebMvc
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/sse/price-update")
                .allowedOrigins("http://127.0.0.1:5500") // Allow http://127.0.0.1:5500
                .allowedMethods("GET")
                .allowedHeaders("*") // Allow all headers, replace with specific allowed headers if needed
                .allowCredentials(true);
    }
}
