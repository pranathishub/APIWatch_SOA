package com.apiwatch.service;

import com.apiwatch.entity.*;
import com.apiwatch.exception.ApiException;
import com.apiwatch.repository.*;
import java.time.*;
import java.util.*;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReportingService {

  private final ApiService apis;
  private final LogRepository logs;
  private final AlertRepository alerts;

  public ReportingService(ApiService a, LogRepository l, AlertRepository r) {
    apis = a;
    logs = l;
    alerts = r;
  }

  public Map<String, Object> analytics(Long owner, Long id) {
    if (id != null) apis.get(owner, id);
    Object[] r = logs.aggregate(owner, id).getFirst();
    long total = ((Number) r[0]).longValue(),
      success = ((Number) r[1]).longValue();
    var m = new LinkedHashMap<String, Object>();
    m.put("totalRequests", total);
    m.put("successfulRequests", success);
    m.put("failedRequests", total - success);
    m.put("uptime", total == 0 ? null : (success * 100.0) / total);
    m.put("downtime", total == 0 ? null : ((total - success) * 100.0) / total);
    m.put("averageResponseTime", r[2]);
    m.put("minimumResponseTime", r[3]);
    m.put("maximumResponseTime", r[4]);
    var recent =
      id == null
        ? logs.findTop60ByApi_Owner_IdOrderByCheckedAtDesc(owner)
        : logs.findTop60ByApi_IdAndApi_Owner_IdOrderByCheckedAtDesc(id, owner);
    Collections.reverse(recent);
    m.put("series", recent);
    return m;
  }

  public Page<MonitoringLog> history(
    Long owner,
    Long id,
    LocalDate date,
    String status,
    Integer code,
    int page,
    int size
  ) {
    if (id != null) apis.get(owner, id);
    if (
      status != null && !Set.of("UP", "DOWN").contains(status)
    ) throw new ApiException(400, "Status must be UP or DOWN");
    Specification<MonitoringLog> s = (root, q, cb) ->
      cb.equal(root.get("api").get("owner").get("id"), owner);
    if (id != null) s = s.and((r, q, c) -> c.equal(r.get("api").get("id"), id));
    if (status != null) s = s.and((r, q, c) ->
      c.equal(r.get("status"), status)
    );
    if (code != null) s = s.and((r, q, c) ->
      c.equal(r.get("statusCode"), code)
    );
    if (date != null) {
      Instant start = date.atStartOfDay(ZoneOffset.UTC).toInstant(),
        end = start.plusSeconds(86400);
      s = s.and((r, q, c) ->
        c.and(
          c.greaterThanOrEqualTo(r.get("checkedAt"), start),
          c.lessThan(r.get("checkedAt"), end)
        )
      );
    }
    return logs.findAll(
      s,
      PageRequest.of(
        Math.max(0, page),
        Math.clamp(size, 1, 100),
        Sort.by(Sort.Direction.DESC, "checkedAt", "id")
      )
    );
  }

  public Page<Alert> alerts(Long owner, int page) {
    return alerts.findByApi_Owner_IdOrderByCreatedAtDesc(
      owner,
      PageRequest.of(Math.max(0, page), 20)
    );
  }

  public long unresolvedAlerts(Long owner) {
    return alerts.countByApi_Owner_IdAndResolvedFalse(owner);
  }

  @Transactional
  public Alert resolve(Long owner, Long id) {
    var a = alerts
      .findByIdAndApi_Owner_Id(id, owner)
      .orElseThrow(() -> new ApiException(404, "Alert not found"));
    if (!a.resolved) {
      a.resolved = true;
      a.resolvedAt = Instant.now();
    }
    return alerts.save(a);
  }
}
