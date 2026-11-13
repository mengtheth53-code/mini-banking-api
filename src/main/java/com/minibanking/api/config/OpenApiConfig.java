package com.minibanking.api.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    private static final String SECURITY_SCHEME_NAME = "Bearer Authentication";

    @Bean
    public OpenAPI miniBankingOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Mini Banking API")
                        .description("Production-Ready Mini Banking RESTful API built with Java 21, Spring Boot 3, Spring Security, JWT, PostgreSQL, and Docker.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Theth Meng")
                                .email("mengtheth53@gmail.com")
                                .url("https://github.com/mengtheth53-code/mini-banking-api"))
                        .license(new License().name("Apache 2.0").url("https://spring.io")))
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME,
                                new SecurityScheme()
                                        .name(SECURITY_SCHEME_NAME)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Enter your JWT token obtained from /api/v1/auth/login")));
    }
}
