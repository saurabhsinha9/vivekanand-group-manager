
package com.vivekanand.manager;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;

@SpringBootApplication
public class VivekanandManagerApplication {
    public static void main(String[] args) {
        SpringApplication.run(VivekanandManagerApplication.class, args);
    }

    @Bean
    public CorsFilter corsFilter(org.springframework.core.env.Environment env) {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        String origins = env.getProperty("cors.allowedOrigins", "http://localhost:3000,http://localhost:5173");
        config.setAllowedOrigins(Arrays.asList(origins.split(",")));
        config.setAllowedHeaders(Arrays.asList("Origin", "Content-Type", "Accept", "Authorization"));
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowCredentials(true);
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}
