package com.monocept.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class InsuranceProjectApplication {

	public static void main(String[] args) {
		SpringApplication.run(InsuranceProjectApplication.class, args);
	}
// adding extra line here
}
