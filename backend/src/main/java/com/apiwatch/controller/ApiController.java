package com.apiwatch.controller;

import com.apiwatch.dto.Requests.ApiInput;
import com.apiwatch.service.ApiService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/apis")
public class ApiController {

  private final ApiService apis;

  public ApiController(ApiService a) {
    apis = a;
  }

  @GetMapping
  public Object list(@AuthenticationPrincipal Jwt j) {
    return apis.list(Long.valueOf(j.getSubject()));
  }

  @GetMapping("/{id}")
  public Object get(@AuthenticationPrincipal Jwt j, @PathVariable Long id) {
    return apis.get(Long.valueOf(j.getSubject()), id);
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public Object create(
    @AuthenticationPrincipal Jwt j,
    @Valid @RequestBody ApiInput i
  ) {
    return apis.create(Long.valueOf(j.getSubject()), i);
  }

  @PutMapping("/{id}")
  public Object update(
    @AuthenticationPrincipal Jwt j,
    @PathVariable Long id,
    @Valid @RequestBody ApiInput i
  ) {
    return apis.update(Long.valueOf(j.getSubject()), id, i);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@AuthenticationPrincipal Jwt j, @PathVariable Long id) {
    apis.delete(Long.valueOf(j.getSubject()), id);
  }
}
