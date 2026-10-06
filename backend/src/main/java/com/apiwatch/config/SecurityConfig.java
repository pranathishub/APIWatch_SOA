package com.apiwatch.config;

import java.nio.charset.StandardCharsets;
import java.util.List;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.*;

@Configuration
public class SecurityConfig {

  @Bean
  PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  SecretKeySpec key(@Value("${apiwatch.jwt-secret}") String secret) {
    if (
      secret.getBytes(StandardCharsets.UTF_8).length < 32
    ) throw new IllegalArgumentException(
      "JWT_SECRET must contain at least 32 bytes"
    );
    return new SecretKeySpec(
      secret.getBytes(StandardCharsets.UTF_8),
      "HmacSHA256"
    );
  }

  @Bean
  JwtEncoder encoder(SecretKeySpec key) {
    return new NimbusJwtEncoder(
      new com.nimbusds.jose.jwk.source.ImmutableSecret<>(key)
    );
  }

  @Bean
  JwtDecoder decoder(SecretKeySpec key) {
    var d = NimbusJwtDecoder.withSecretKey(key)
      .macAlgorithm(MacAlgorithm.HS256)
      .build();
    d.setJwtValidator(JwtValidators.createDefaultWithIssuer("apiwatch"));
    return d;
  }

  @Bean
  SecurityFilterChain security(HttpSecurity http) throws Exception {
    return http
      .csrf(c -> c.disable())
      .cors(c -> {})
      .sessionManagement(s ->
        s.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
      )
      .authorizeHttpRequests(a ->
        a
          .requestMatchers(
            "/api/auth/register",
            "/api/auth/login",
            "/api/health"
          )
          .permitAll()
          .anyRequest()
          .authenticated()
      )
      .oauth2ResourceServer(o ->
        o
          .jwt(j -> {})
          .authenticationEntryPoint((req, res, e) -> {
            res.setStatus(401);
            res.setContentType("application/json");
            res
              .getWriter()
              .write("{\"status\":401,\"message\":\"Please sign in again\"}");
          })
      )
      .build();
  }

  @Bean
  CorsConfigurationSource cors(
    @Value("${apiwatch.cors-origin}") String origin
  ) {
    var c = new CorsConfiguration();
    c.setAllowedOrigins(List.of(origin));
    c.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
    c.setAllowedHeaders(List.of("Authorization", "Content-Type"));
    var source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", c);
    return source;
  }
}
