package com.sj.product_service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(authz -> authz
                .requestMatchers("/api/v1/public/**").permitAll()
                .requestMatchers("/actuator/health/**").permitAll()
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                .requestMatchers("/api/v1/products/available").permitAll()
                .requestMatchers("/api/v1/products/featured").permitAll()
                .requestMatchers("/api/v1/products/search").permitAll()
                .requestMatchers("/api/v1/products/check/**").permitAll()
                .requestMatchers("/api/v1/categories/**").permitAll()
                .requestMatchers("POST", "/api/v1/products").hasAnyRole("SELLER", "ADMIN")
                .requestMatchers("PUT", "/api/v1/products/**").hasAnyRole("SELLER", "ADMIN")
                .requestMatchers("DELETE", "/api/v1/products/**").hasAnyRole("SELLER", "ADMIN")
                .requestMatchers("PATCH", "/api/v1/products/**").hasAnyRole("SELLER", "ADMIN")
                .anyRequest().authenticated()
            );
            
        return http.build();
    }
}
