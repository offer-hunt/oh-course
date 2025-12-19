package ru.offer.hunt.oh_course.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.savedrequest.NullRequestCache;

import java.util.*;


@Configuration
public class SecurityConfig {

    @Value("${app.auth.issuer}")
    private String issuer;

    @Value("${app.auth.audience}")
    private String audience;

    @Value("${app.auth.jwks-url}")
    private String jwksUrl;

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.cors(Customizer.withDefaults())
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .requestCache(cache -> cache.requestCache(new NullRequestCache()))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                        .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/secure/ping").hasAuthority("SCOPE_course.read")
//                        .anyRequest().authenticated()
                        .anyRequest().permitAll()
                )
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt ->
                        jwt.decoder(jwtDecoder())
                                .jwtAuthenticationConverter(jwtAuthConverter())
                ));

        return http.build();
    }
    @Bean
    JwtDecoder jwtDecoder() {
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withJwkSetUri(jwksUrl).build();

        OAuth2TokenValidator<Jwt> withIssuer = JwtValidators.createDefaultWithIssuer(issuer);
        OAuth2TokenValidator<Jwt> withAudience =
                jwt -> {
                    Object aud = jwt.getClaims().get("aud");
                    boolean ok =
                            (aud instanceof String s && audience.equals(s))
                                    || (aud instanceof List<?> list && list.contains(audience));
                    return ok
                            ? OAuth2TokenValidatorResult.success()
                            : OAuth2TokenValidatorResult.failure(
                            new OAuth2Error("invalid_token", "Invalid aud", null));
                };

        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(withIssuer, withAudience));
        return decoder;
    }

    @Bean
    JwtAuthenticationConverter jwtAuthConverter() {
        var converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(
                jwt -> {
                    Object rolesClaim = jwt.getClaims().getOrDefault("roles", List.of());
                    Object scopesClaim = jwt.getClaims().getOrDefault("scp", List.of());

                    List<String> roles =
                            (rolesClaim instanceof List<?> l)
                                    ? l.stream().map(Object::toString).toList()
                                    : List.of();
                    List<String> scopes =
                            (scopesClaim instanceof List<?> l)
                                    ? l.stream().map(Object::toString).toList()
                                    : List.of();

                    List<GrantedAuthority> auths = new ArrayList<>();
                    for (String r : roles) {
                        auths.add(new SimpleGrantedAuthority("ROLE_" + r));
                    }
                    for (String s : scopes) {
                        auths.add(new SimpleGrantedAuthority("SCOPE_" + s));
                    }
                    return auths;
                });
        return converter;
    }
}