package com.apiwatch.controller;

import com.apiwatch.service.ReportingService;
import java.time.LocalDate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class ReportingController {

  private final ReportingService reports;

  public ReportingController(ReportingService r) {
    reports = r;
  }

  @GetMapping("/health")
  public Object health() {
    return java.util.Map.of("status", "UP");
  }

  @GetMapping({ "/analytics", "/analytics/{apiId}" })
  public Object analytics(
    @AuthenticationPrincipal Jwt j,
    @PathVariable(required = false) Long apiId
  ) {
    return reports.analytics(Long.valueOf(j.getSubject()), apiId);
  }

  @GetMapping({ "/monitoring", "/monitoring/{apiId}" })
  public Object history(
    @AuthenticationPrincipal Jwt j,
    @PathVariable(required = false) Long apiId,
    @RequestParam(required = false) Long api,
    @RequestParam(required = false) LocalDate date,
    @RequestParam(required = false) String status,
    @RequestParam(required = false) Integer statusCode,
    @RequestParam(defaultValue = "0") int page,
    @RequestParam(defaultValue = "20") int size
  ) {
    return reports.history(
      Long.valueOf(j.getSubject()),
      apiId != null ? apiId : api,
      date,
      status,
      statusCode,
      page,
      size
    );
  }

  @GetMapping("/alerts")
  public Object alerts(
    @AuthenticationPrincipal Jwt j,
    @RequestParam(defaultValue = "0") int page
  ) {
    return reports.alerts(Long.valueOf(j.getSubject()), page);
  }

  @PutMapping("/alerts/{id}/resolve")
  public Object resolve(@AuthenticationPrincipal Jwt j, @PathVariable Long id) {
    return reports.resolve(Long.valueOf(j.getSubject()), id);
  }

  @GetMapping("/alerts/unresolved-count")
  public Object unresolved(@AuthenticationPrincipal Jwt j) {
    return java.util.Map.of(
      "count",
      reports.unresolvedAlerts(Long.valueOf(j.getSubject()))
    );
  }
}
