package com.productservice.security;

import java.util.Collections;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.SecurityFilterChain;

import java.security.interfaces.RSAPublicKey;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtConfig jwtConfig;
    private final ServicePublicKeyConfig servicePublicKeyConfig;

    public SecurityConfig(
            JwtConfig jwtConfig,
            ServicePublicKeyConfig servicePublicKeyConfig) {

        this.jwtConfig = jwtConfig;
        this.servicePublicKeyConfig = servicePublicKeyConfig;
    }

    /*
     * ==============================
     * INTERNAL SERVICE JWT
     * ==============================
     */

    @Bean
    @Order(1)
    public SecurityFilterChain internalSecurityFilterChain(
            HttpSecurity http) throws Exception {

        http
            .securityMatcher("/internal/inventory/**")

            .csrf(csrf -> csrf.disable())

            .sessionManagement(session ->
                session.sessionCreationPolicy(
                    SessionCreationPolicy.STATELESS
                )
            )

            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    HttpMethod.PUT,
                    "/internal/inventory/{id}/reserve",
                    "/internal/inventory/{id}/release"
                )
                .hasAuthority("SERVICE_ORDER")
                .anyRequest()
                .authenticated()
            )

            .oauth2ResourceServer(oauth2 ->
                oauth2.jwt(jwt ->
                    jwt.decoder(serviceJwtDecoder())
                       .jwtAuthenticationConverter(
                           serviceJwtAuthenticationConverter()
                       )
                )
            );

        return http.build();
    }

    /*
     * ==============================
     * USER JWT
     * ==============================
     */

    @Bean
    @Order(2)
    public SecurityFilterChain userSecurityFilterChain(
            HttpSecurity http) throws Exception {

        http
            .csrf(csrf -> csrf.disable())

            .sessionManagement(session ->
                session.sessionCreationPolicy(
                    SessionCreationPolicy.STATELESS
                )
            )

            .authorizeHttpRequests(auth -> auth

                .requestMatchers(
                    "/actuator/health",
                    "/actuator/info",
                    "/actuator/prometheus"
                )
                .permitAll()

                .requestMatchers(
                    HttpMethod.GET,
                    "/api/v1/products",
                    "/api/v1/products/{id}",
                    "/api/v1/products/search",
                    "/api/v1/products/category/**"
                )
                .permitAll()

                .requestMatchers(
                    HttpMethod.POST,
                    "/api/v1/products"
                )
                .hasRole("ADMIN")

                .requestMatchers(
                    HttpMethod.PUT,
                    "/api/v1/products/{id}"
                )
                .hasRole("ADMIN")

                .requestMatchers(
                    HttpMethod.DELETE,
                    "/api/v1/products/{id}"
                )
                .hasRole("ADMIN")

                .anyRequest()
                .authenticated()
            )

            .oauth2ResourceServer(oauth2 ->
                oauth2.jwt(jwt ->
                    jwt.decoder(userJwtDecoder())
                       .jwtAuthenticationConverter(
                           userJwtAuthenticationConverter()
                       )
                )
            );

        return http.build();
    }

    /*
     * ==============================
     * USER JWT DECODER
     * ==============================
     */

    @Bean
    public JwtDecoder userJwtDecoder() {

        return NimbusJwtDecoder
                .withSecretKey(jwtConfig.jwtSecretKey())
                .build();
    }

    /*
     * ==============================
     * SERVICE JWT DECODER
     * ==============================
     */

    @Bean
    public JwtDecoder serviceJwtDecoder() {

        RSAPublicKey publicKey =
                servicePublicKeyConfig.servicePublicKey();

        return NimbusJwtDecoder
                .withPublicKey(publicKey)
                .build();
    }

    /*
     * ==============================
     * USER JWT AUTHORITIES
     * ==============================
     */

    @Bean
    public JwtAuthenticationConverter userJwtAuthenticationConverter() {

        JwtAuthenticationConverter converter =
                new JwtAuthenticationConverter();

        converter.setJwtGrantedAuthoritiesConverter(jwt -> {

            String role = jwt.getClaimAsString("role");

            if (role == null || role.isBlank()) {
                return Collections.emptyList();
            }

            return List.of(
                new SimpleGrantedAuthority(
                    "ROLE_" + role.toUpperCase()
                )
            );
        });

        return converter;
    }

    /*
     * ==============================
     * SERVICE JWT AUTHORITIES
     * ==============================
     */

    @Bean
    public JwtAuthenticationConverter serviceJwtAuthenticationConverter() {

        JwtAuthenticationConverter converter =
                new JwtAuthenticationConverter();

        converter.setJwtGrantedAuthoritiesConverter(jwt -> {

            String subject = jwt.getSubject();

            if ("orderservice".equals(subject)) {
                return List.of(
                    new SimpleGrantedAuthority("SERVICE_ORDER")
                );
            }

            return Collections.emptyList();
        });

        return converter;
    }
}