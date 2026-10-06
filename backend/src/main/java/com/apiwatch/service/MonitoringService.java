package com.apiwatch.service;

import com.apiwatch.entity.*;
import com.apiwatch.repository.*;
import java.time.Instant;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MonitoringService {

  private final ApiRepository apis;
  private final LogRepository logs;
  private final AlertRepository alerts;
  private final HttpProbe probe;

  public MonitoringService(
    ApiRepository a,
    LogRepository l,
    AlertRepository r,
    HttpProbe p
  ) {
    apis = a;
    logs = l;
    alerts = r;
    probe = p;
  }

  // The row lock prevents overlapping checks and protects edits/deletes during a check.
  @Transactional
  public void check(Long id) {
    var found = apis.lockById(id);
    if (found.isEmpty()) return;
    var a = found.get();
    if (!a.active || a.nextCheckAt.isAfter(Instant.now())) return;
    var result = probe.check(a);
    Instant now = Instant.now();
    var log = new MonitoringLog();
    log.api = a;
    log.apiName = a.name;
    log.url = a.url;
    log.httpMethod = a.httpMethod;
    log.status = result.status();
    log.statusCode = result.code();
    log.responseTime = result.elapsed();
    log.errorMessage = result.error();
    log.checkedAt = now;
    logs.save(log);
    if ("DOWN".equals(result.status()) && !"DOWN".equals(a.lastStatus)) {
      var alert = new Alert();
      alert.api = a;
      alert.apiName = a.name;
      alert.message = a.name + " is down. " + result.error();
      alerts.save(alert);
    }
    if ("UP".equals(result.status())) {
      for (var alert : alerts.findByApi_IdAndResolvedFalse(a.id)) {
        alert.resolved = true;
        alert.resolvedAt = now;
        alerts.save(alert);
      }
    }
    a.lastStatus = result.status();
    a.lastStatusCode = result.code();
    a.lastResponseTime = result.elapsed();
    a.lastChecked = now;
    a.nextCheckAt = now.plusSeconds(a.monitoringInterval);
    apis.save(a);
  }
}
