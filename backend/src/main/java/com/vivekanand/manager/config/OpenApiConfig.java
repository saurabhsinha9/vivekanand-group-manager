package com.vivekanand.manager.config;

import io.swagger.v3.oas.models.*;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        // Define the Bearer JWT security scheme
        SecurityScheme bearerScheme = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .description("Paste: Bearer {token}");

        // Register scheme and require it by default
        Components components = new Components()
                .addSecuritySchemes("bearerAuth", bearerScheme);

        SecurityRequirement requirement = new SecurityRequirement()
                .addList("bearerAuth");

        return new OpenAPI()
                .components(components)
                .addSecurityItem(requirement)
                .info(new Info()
                        .title("Vivekanand Group Manager API")
                        .description("Society management APIs with JWT security")
                        .version("1.0.0"));
    }
}
