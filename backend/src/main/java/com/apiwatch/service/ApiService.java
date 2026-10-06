package com.apiwatch.service;

import com.apiwatch.dto.Requests.ApiInput;
import com.apiwatch.entity.*;
import com.apiwatch.exception.ApiException;
import com.apiwatch.repository.*;
import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ApiService {

  private final ApiRepository apis;
  private final UserRepository users;
  private final LogRepository logs;
  private final AlertRepository alerts;
  private final TargetValidator targets;

  public ApiService(
    ApiRepository a,
    UserRepository u,
    LogRepository l,
    AlertRepository r,
    TargetValidator t
  ) {
    apis = a;
    users = u;
    logs = l;
    alerts = r;
    targets = t;
  }

  public List<ApiDetails> list(Long owner) {
    return apis.findByOwnerIdOrderByCreatedAtDesc(owner);
  }

  public ApiDetails get(Long owner, Long id) {
    return apis
      .findByIdAndOwnerId(id, owner)
      .orElseThrow(() -> new ApiException(404, "API not found"));
  }

  @Transactional
  public ApiDetails create(Long owner, ApiInput input) {
    var a = new ApiDetails();
    a.owner = users
      .findById(owner)
      .orElseThrow(() -> new ApiException(401, "Account not found"));
    apply(a, input);
    return apis.save(a);
  }

  @Transactional
  public ApiDetails update(Long owner, Long id, ApiInput input) {
    var a = locked(owner, id);
    apply(a, input);
    return apis.save(a);
  }

  @Transactional
  public void delete(Long owner, Long id) {
    var a = locked(owner, id);
    alerts.deleteByApi_Id(id);
    logs.deleteByApi_Id(id);
    apis.delete(a);
  }

  private ApiDetails locked(Long owner, Long id) {
    var a = apis
      .lockById(id)
      .orElseThrow(() -> new ApiException(404, "API not found"));
    if (!a.owner.id.equals(owner)) throw new ApiException(404, "API not found");
    return a;
  }

  private void apply(ApiDetails a, ApiInput i) {
    targets.validate(i.url());
    a.name = i.name().trim();
    a.url = i.url();
    a.httpMethod = i.httpMethod();
    a.description = i.description();
    a.expectedStatusCode = i.expectedStatusCode();
    a.monitoringInterval = i.monitoringInterval();
    a.timeout = i.timeout();
    a.active = i.active();
    a.updatedAt = Instant.now();
    a.nextCheckAt = Instant.now();
  }
}
