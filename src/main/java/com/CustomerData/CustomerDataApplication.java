package com.CustomerData;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class CustomerDataApplication {

	public static void main(String[] args) {
		SpringApplication.run(CustomerDataApplication.class, args);
	}

}
