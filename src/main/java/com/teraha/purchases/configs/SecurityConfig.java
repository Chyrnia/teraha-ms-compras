package com.teraha.purchases.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http) throws Exception {

        return http
            .csrf(AbstractHttpConfigurer::disable)
            .formLogin(AbstractHttpConfigurer::disable)
            .httpBasic(AbstractHttpConfigurer::disable)
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // Public API documentation
                .requestMatchers(
                    "/v3/api-docs/**",
                    "/v3/api-docs.yaml",
                    "/swagger-ui.html",
                    "/swagger-ui/**"
                ).permitAll()

                // Purchase invoices
                .requestMatchers(
                    HttpMethod.GET, "/purchases", "/purchases/**")
                    .hasAnyRole("SELLER", "MANAGER", "SYSADMIN")
                .requestMatchers(HttpMethod.POST, "/purchases")
                    .hasAnyRole("SELLER", "MANAGER", "SYSADMIN")

                // Supplier management
                .requestMatchers(
                    HttpMethod.GET, "/suppliers", "/suppliers/**")
                    .hasAnyRole("SELLER", "MANAGER", "SYSADMIN")
                .requestMatchers(HttpMethod.POST, "/suppliers")
                    .hasAnyRole("MANAGER", "SYSADMIN")
                .requestMatchers(HttpMethod.DELETE, "/suppliers/**")
                    .hasAnyRole("MANAGER", "SYSADMIN")

                .anyRequest().authenticated())
            .oauth2ResourceServer(oauth2 ->
                oauth2.jwt(Customizer.withDefaults()))
            .build();
    }
}
