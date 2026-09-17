package com.productservice.security;

import java.util.Collections;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoders;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtConfig jwtConfig;

    public SecurityConfig(JwtConfig jwtConfig) {
        this.jwtConfig = jwtConfig;
    }

    /*
     * =========================================================
     * 1. INTERNAL INVENTORY APIs
     * =========================================================
     *
     * These APIs are called by other services.
     *
     * Authentication:
     * Keycloak
     *
     * Authorization:
     * internal:stock:write
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
                .hasAuthority("SCOPE_internal:stock:write")

                .anyRequest()
                .authenticated()
            )

            .oauth2ResourceServer(oauth2 ->
                oauth2.jwt(jwt -> {

                    jwt.decoder(keycloakJwtDecoder());

                    jwt.jwtAuthenticationConverter(
                        keycloakJwtAuthenticationConverter()
                    );
                })
            );

        return http.build();
    }

    /*
     * =========================================================
     * 2. EXISTING PRODUCT APIs
     * =========================================================
     *
     * Existing User Service JWT authentication remains here.
     */

    @Bean
    @Order(2)
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http) throws Exception {

        http
            .csrf(csrf -> csrf.disable())

            .sessionManagement(session ->
                session.sessionCreationPolicy(
                    SessionCreationPolicy.STATELESS
                )
            )

            .authorizeHttpRequests(auth -> auth

                /*
                 * ACTUATOR
                 */

                .requestMatchers(
                    "/actuator/health",
                    "/actuator/info",
                    "/actuator/prometheus"
                )
                .permitAll()

                /*
                 * PUBLIC PRODUCT APIs
                 */

                .requestMatchers(
                    HttpMethod.GET,
                    "/api/v1/products",
                    "/api/v1/products/{id}",
                    "/api/v1/products/search",
                    "/api/v1/products/category/**"
                )
                .permitAll()

                /*
                 * ADMIN PRODUCT APIs
                 */

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
                    jwt.jwtAuthenticationConverter(
                        jwtAuthenticationConverter()
                    )
                )
            );

        return http.build();
    }

    /*
     * =========================================================
     * OLD USER JWT DECODER
     * =========================================================
     */

    @Bean
    @Primary
    public JwtDecoder jwtDecoder() {

        return NimbusJwtDecoder
                .withSecretKey(jwtConfig.jwtSecretKey())
                .build();
    }

    /*
     * =========================================================
     * KEYCLOAK JWT DECODER
     * =========================================================
     */

    @Bean
    public JwtDecoder keycloakJwtDecoder() {

        return JwtDecoders.fromIssuerLocation(
            "https://auth.shevchaha.online/realms/ecommerce"
        );
    }

    /*
     * =========================================================
     * KEYCLOAK AUTHORITIES
     * =========================================================
     *
     * Keycloak puts OAuth2 scopes into the "scope" claim.
     *
     * Spring converts:
     *
     * internal:stock:write
     *
     * into:
     *
     * SCOPE_internal:stock:write
     */

    @Bean
    public JwtAuthenticationConverter keycloakJwtAuthenticationConverter() {

        JwtGrantedAuthoritiesConverter scopes =
                new JwtGrantedAuthoritiesConverter();

        scopes.setAuthorityPrefix("SCOPE_");
        scopes.setAuthoritiesClaimName("scope");

        JwtAuthenticationConverter converter =
                new JwtAuthenticationConverter();

        converter.setJwtGrantedAuthoritiesConverter(scopes);

        return converter;
    }

    /*
     * =========================================================
     * EXISTING USER JWT AUTHORITIES
     * =========================================================
     */

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {

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
}