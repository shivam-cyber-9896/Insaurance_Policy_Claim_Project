package com.monocept.app.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI insuranceProjectOpenAPI() {

        return new OpenAPI()
                .info(new Info()
                        .title("Insurance Policy Claim Management API")
                        .description("REST APIs for Insurance Policy & Claim Management System")
                        .version("1.0")

                        

                        .license(new License()
                                .name("MIT License")));
    }
}