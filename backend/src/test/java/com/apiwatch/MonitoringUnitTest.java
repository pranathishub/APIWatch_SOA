package com.apiwatch;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.apiwatch.entity.*;
import com.apiwatch.repository.*;
import com.apiwatch.service.*;
import java.time.Instant;
import java.util.*;
import org.junit.jupiter.api.Test;

class MonitoringUnitTest {

  @Test
  void persistsTimeoutAsDown() {
    var apis = mock(ApiRepository.class);
    var logs = mock(LogRepository.class);
    var alerts = mock(AlertRepository.class);
    var probe = mock(HttpProbe.class);
    var a = new ApiDetails();
    a.id = 1L;
    a.active = true;
    a.name = "Slow API";
    a.url = "https://example.com";
    a.httpMethod = "GET";
    a.nextCheckAt = Instant.EPOCH;
    a.monitoringInterval = 60;
    a.lastStatus = "UP";
    when(apis.lockById(1L)).thenReturn(Optional.of(a));
    when(probe.check(a)).thenReturn(
      new HttpProbe.Result("DOWN", null, 1000, "Request timed out")
    );
    new MonitoringService(apis, logs, alerts, probe).check(1L);
    verify(logs).save(
      argThat(
        l ->
          l.status.equals("DOWN") &&
          l.statusCode == null &&
          l.errorMessage.contains("timed out")
      )
    );
    verify(alerts).save(argThat(alert -> !alert.resolved));
    assertEquals("DOWN", a.lastStatus);
    assertTrue(a.nextCheckAt.isAfter(Instant.now()));
  }

  @Test
  void validatesUrlsAndRejectsPrivateTargets() throws Exception {
    var v = new TargetValidator(false);
    assertEquals("https", v.validate("https://example.com/health").getScheme());
    assertThrows(RuntimeException.class, () -> v.validate("ftp://example.com"));
    assertThrows(RuntimeException.class, () ->
      v.validate("https://user:pass@example.com")
    );
    assertThrows(RuntimeException.class, () ->
      v.checkDestination("http://127.0.0.1")
    );
    assertThrows(RuntimeException.class, () ->
      v.checkDestination("http://[::1]")
    );
    assertThrows(RuntimeException.class, () ->
      v.checkDestination("http://169.254.169.254")
    );
  }

  @Test
  void connectionFailureIsRecorded() {
    var a = new ApiDetails();
    a.url = "http://127.0.0.1:1";
    a.httpMethod = "GET";
    a.timeout = 1;
    a.expectedStatusCode = 200;
    var r = new HttpProbe(new TargetValidator(true)).check(a);
    assertEquals("DOWN", r.status());
    assertNull(r.code());
    assertNotNull(r.error());
  }

  @Test
  void timeoutIsEnforced() throws Exception {
    var server = com.sun.net.httpserver.HttpServer.create(
      new java.net.InetSocketAddress("127.0.0.1", 0),
      0
    );
    server.createContext("/", e -> {
      try {
        Thread.sleep(1600);
        e.sendResponseHeaders(200, -1);
      } catch (Exception ignored) {
      } finally {
        e.close();
      }
    });
    server.start();
    try {
      var a = new ApiDetails();
      a.url = "http://127.0.0.1:" + server.getAddress().getPort();
      a.httpMethod = "GET";
      a.timeout = 1;
      a.expectedStatusCode = 200;
      var r = new HttpProbe(new TargetValidator(true)).check(a);
      assertEquals("DOWN", r.status());
      assertTrue(r.error().contains("timed out"));
    } finally {
      server.stop(0);
    }
  }

  @Test
  void timesOutWhenHeadersArriveButBodyStalls() throws Exception {
    var server = com.sun.net.httpserver.HttpServer.create(
      new java.net.InetSocketAddress("127.0.0.1", 0),
      0
    );
    server.createContext("/", exchange -> {
      try {
        exchange.sendResponseHeaders(200, 100);
        exchange.getResponseBody().write(1);
        exchange.getResponseBody().flush();
        Thread.sleep(2000);
      } catch (Exception ignored) {
      } finally {
        exchange.close();
      }
    });
    server.start();
    try (var probe = new HttpProbe(new TargetValidator(true))) {
      var api = new ApiDetails();
      api.url = "http://127.0.0.1:" + server.getAddress().getPort();
      api.httpMethod = "GET";
      api.timeout = 1;
      api.expectedStatusCode = 200;
      var result = probe.check(api);
      assertEquals("DOWN", result.status());
      assertTrue(result.error().contains("timed out"));
    } finally {
      server.stop(0);
    }
  }
}
