package com.CustomerData;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.thymeleaf.TemplateEngine;

@SpringBootApplication
@EnableAsync
public class CustomerDataApplication {

	public static void main(String[] args) {
		SpringApplication.run(CustomerDataApplication.class, args);
	}

}
