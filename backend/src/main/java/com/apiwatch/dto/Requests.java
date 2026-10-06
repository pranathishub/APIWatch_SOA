package com.apiwatch.dto;

import jakarta.validation.constraints.*;

public class Requests {

  public record Register(
    @NotBlank @Pattern(regexp = "[A-Za-z0-9_]{3,40}") String username,
    @NotBlank @Email @Size(max = 120) String email,
    @NotBlank @Size(min = 8, max = 72) String password
  ) {}

  public record Login(@NotBlank String username, @NotBlank String password) {}

  public record ApiInput(
    @NotBlank @Size(max = 120) String name,
    @NotBlank @Size(max = 2048) String url,
    @NotBlank @Pattern(regexp = "GET|POST|PUT|DELETE|PATCH") String httpMethod,
    @Size(max = 2000) String description,
    @Min(100) @Max(599) int expectedStatusCode,
    @Min(10) @Max(86400) int monitoringInterval,
    @Min(1) @Max(30) int timeout,
    boolean active
  ) {}
}
