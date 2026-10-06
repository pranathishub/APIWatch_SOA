package com.apiwatch.service;

import com.apiwatch.dto.Requests.*;
import com.apiwatch.entity.UserAccount;
import com.apiwatch.exception.ApiException;
import com.apiwatch.repository.UserRepository;
import java.time.Instant;
import java.util.Map;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

  private final UserRepository users;
  private final PasswordEncoder passwords;
  private final JwtEncoder jwt;

  public AuthService(UserRepository u, PasswordEncoder p, JwtEncoder j) {
    users = u;
    passwords = p;
    jwt = j;
  }

  public Map<String, Object> register(Register r) {
    if (
      r.password().getBytes(java.nio.charset.StandardCharsets.UTF_8).length > 72
    ) throw new ApiException(400, "Password must be at most 72 UTF-8 bytes");
    if (
      users.existsByUsername(r.username()) || users.existsByEmail(r.email())
    ) throw new ApiException(409, "Username or email already registered");
    var u = new UserAccount();
    u.username = r.username();
    u.email = r.email();
    u.password = passwords.encode(r.password());
    users.save(u);
    return token(u);
  }

  public Map<String, Object> login(Login r) {
    var u = users
      .findByUsername(r.username())
      .orElseThrow(() -> new ApiException(401, "Invalid username or password"));
    if (!passwords.matches(r.password(), u.password)) throw new ApiException(
      401,
      "Invalid username or password"
    );
    return token(u);
  }

  private Map<String, Object> token(UserAccount u) {
    var now = Instant.now();
    var claims = JwtClaimsSet.builder()
      .issuer("apiwatch")
      .subject(u.id.toString())
      .issuedAt(now)
      .expiresAt(now.plusSeconds(7200))
      .claim("scope", "ADMIN")
      .build();
    String t = jwt
      .encode(
        JwtEncoderParameters.from(
          JwsHeader.with(MacAlgorithm.HS256).build(),
          claims
        )
      )
      .getTokenValue();
    return Map.of("token", t, "user", u, "expiresAt", now.plusSeconds(7200));
  }

  public UserAccount profile(Long id) {
    return users
      .findById(id)
      .orElseThrow(() -> new ApiException(401, "Account not found"));
  }
}
