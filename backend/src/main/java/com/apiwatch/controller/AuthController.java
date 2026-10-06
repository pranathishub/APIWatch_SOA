package com.apiwatch.controller;

import com.apiwatch.dto.Requests.*;
import com.apiwatch.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

  private final AuthService auth;

  public AuthController(AuthService a) {
    auth = a;
  }

  @PostMapping("/register")
  @ResponseStatus(HttpStatus.CREATED)
  public Object register(@Valid @RequestBody Register r) {
    return auth.register(r);
  }

  @PostMapping("/login")
  public Object login(@Valid @RequestBody Login r) {
    return auth.login(r);
  }

  @GetMapping("/me")
  public Object me(@AuthenticationPrincipal Jwt jwt) {
    return auth.profile(Long.valueOf(jwt.getSubject()));
  }
}
