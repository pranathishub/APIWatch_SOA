package com.apiwatch.scheduler;

import com.apiwatch.repository.ApiRepository;
import com.apiwatch.service.MonitoringService;
import java.time.Instant;
import java.util.concurrent.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(
  name = "apiwatch.scheduler-enabled",
  havingValue = "true",
  matchIfMissing = true
)
public class MonitoringScheduler {

  private final ApiRepository apis;
  private final MonitoringService monitor;

  public MonitoringScheduler(ApiRepository a, MonitoringService m) {
    apis = a;
    monitor = m;
  }

  @Scheduled(fixedDelay = 1000)
  public void tick() {
    var due =
      apis.findTop50ByActiveTrueAndNextCheckAtLessThanEqualOrderByNextCheckAtAsc(
        Instant.now()
      );
    try (var executor = Executors.newFixedThreadPool(8)) {
      for (var a : due)
        executor.submit(() -> {
          try {
            monitor.check(a.id);
          } catch (Exception e) {
            org.slf4j.LoggerFactory.getLogger(getClass()).error(
              "Check failed for API {}",
              a.id,
              e
            );
          }
        });
    }
  }
}
