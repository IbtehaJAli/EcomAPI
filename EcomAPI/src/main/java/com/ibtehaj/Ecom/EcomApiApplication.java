package com.ibtehaj.Ecom;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class EcomApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(EcomApiApplication.class, args);
	}

}
