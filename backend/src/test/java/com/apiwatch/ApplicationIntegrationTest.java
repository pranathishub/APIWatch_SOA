package com.apiwatch;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.apiwatch.entity.*;
import com.apiwatch.repository.*;
import com.apiwatch.service.*;
import com.fasterxml.jackson.databind.*;
import com.sun.net.httpserver.HttpServer;
import java.net.*;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class ApplicationIntegrationTest {

  @Autowired
  MockMvc mvc;

  @Autowired
  ObjectMapper json;

  @Autowired
  ApiRepository apis;

  @Autowired
  LogRepository logs;

  @Autowired
  AlertRepository alerts;

  @Autowired
  UserRepository users;

  @Autowired
  MonitoringService monitor;

  static HttpServer server;
  static AtomicInteger code = new AtomicInteger(200);

  @BeforeAll
  static void start() throws Exception {
    server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
    server.createContext("/health", exchange -> {
      exchange.sendResponseHeaders(code.get(), -1);
      exchange.close();
    });
    server.start();
  }

  @AfterAll
  static void stop() {
    server.stop(0);
  }

  @BeforeEach
  void clear() {
    alerts.deleteAll();
    logs.deleteAll();
    apis.deleteAll();
    users.deleteAll();
    code.set(200);
  }

  String register(String username) throws Exception {
    var r = mvc
      .perform(
        post("/api/auth/register")
          .contentType(MediaType.APPLICATION_JSON)
          .content(
            json.writeValueAsString(
              Map.of(
                "username",
                username,
                "email",
                username + "@example.com",
                "password",
                "demo-password-123"
              )
            )
          )
      )
      .andExpect(status().isCreated())
      .andExpect(jsonPath("user.password").doesNotExist())
      .andReturn();
    return json
      .readTree(r.getResponse().getContentAsString())
      .get("token")
      .asText();
  }

  Map<String, Object> input() {
    return new HashMap<>(
      Map.of(
        "name",
        "Health API",
        "url",
        "http://127.0.0.1:" + server.getAddress().getPort() + "/health",
        "httpMethod",
        "GET",
        "description",
        "Integration test",
        "expectedStatusCode",
        200,
        "monitoringInterval",
        10,
        "timeout",
        2,
        "active",
        true
      )
    );
  }

  long create(String token) throws Exception {
    return json
      .readTree(
        mvc
          .perform(
            post("/api/apis")
              .header("Authorization", "Bearer " + token)
              .contentType(MediaType.APPLICATION_JSON)
              .content(json.writeValueAsString(input()))
          )
          .andExpect(status().isCreated())
          .andReturn()
          .getResponse()
          .getContentAsString()
      )
      .get("id")
      .asLong();
  }

  void due(long id) {
    var a = apis.findById(id).orElseThrow();
    a.nextCheckAt = Instant.now().minusSeconds(1);
    apis.save(a);
  }

  @Test
  void authenticationAndValidation() throws Exception {
    mvc.perform(get("/api/apis")).andExpect(status().isUnauthorized());
    String token = register("student");
    assertTrue(
      users.findByUsername("student").orElseThrow().password.startsWith("$2")
    );
    mvc
      .perform(
        post("/api/auth/login")
          .contentType(MediaType.APPLICATION_JSON)
          .content("{\"username\":\"student\",\"password\":\"wrong\"}")
      )
      .andExpect(status().isUnauthorized())
      .andExpect(jsonPath("message").value("Invalid username or password"));
    mvc
      .perform(
        post("/api/auth/login")
          .contentType(MediaType.APPLICATION_JSON)
          .content(
            "{\"username\":\"student\",\"password\":\"demo-password-123\"}"
          )
      )
      .andExpect(status().isOk())
      .andExpect(jsonPath("token").isNotEmpty());
    mvc
      .perform(get("/api/auth/me").header("Authorization", "Bearer " + token))
      .andExpect(jsonPath("username").value("student"));
    mvc
      .perform(
        post("/api/auth/register")
          .contentType(MediaType.APPLICATION_JSON)
          .content(
            "{\"username\":\"student\",\"email\":\"student@example.com\",\"password\":\"demo-password-123\"}"
          )
      )
      .andExpect(status().isConflict());
    mvc
      .perform(
        post("/api/auth/register")
          .contentType(MediaType.APPLICATION_JSON)
          .content("{}")
      )
      .andExpect(status().isBadRequest());
    mvc
      .perform(
        get("/api/apis").header("Authorization", "Bearer " + token + "x")
      )
      .andExpect(status().isUnauthorized());
  }

  @Test
  void crudOwnershipAndExceptions() throws Exception {
    String owner = register("owner"),
      other = register("other");
    long id = create(owner);
    mvc
      .perform(get("/api/apis").header("Authorization", "Bearer " + other))
      .andExpect(jsonPath("$.length()").value(0));
    mvc
      .perform(
        get("/api/apis/" + id).header("Authorization", "Bearer " + other)
      )
      .andExpect(status().isNotFound());
    mvc
      .perform(
        get("/api/analytics/" + id).header("Authorization", "Bearer " + other)
      )
      .andExpect(status().isNotFound());
    mvc
      .perform(
        get("/api/monitoring/" + id).header("Authorization", "Bearer " + other)
      )
      .andExpect(status().isNotFound());
    var body = input();
    body.put("name", "Updated API");
    body.put("active", false);
    mvc
      .perform(
        put("/api/apis/" + id)
          .header("Authorization", "Bearer " + owner)
          .contentType(MediaType.APPLICATION_JSON)
          .content(json.writeValueAsString(body))
      )
      .andExpect(status().isOk())
      .andExpect(jsonPath("name").value("Updated API"))
      .andExpect(jsonPath("active").value(false));
    mvc
      .perform(
        delete("/api/apis/" + id).header("Authorization", "Bearer " + other)
      )
      .andExpect(status().isNotFound());
    body.put("url", "file:///etc/passwd");
    mvc
      .perform(
        post("/api/apis")
          .header("Authorization", "Bearer " + owner)
          .contentType(MediaType.APPLICATION_JSON)
          .content(json.writeValueAsString(body))
      )
      .andExpect(status().isBadRequest());
    body = input();
    body.put("monitoringInterval", 0);
    mvc
      .perform(
        post("/api/apis")
          .header("Authorization", "Bearer " + owner)
          .contentType(MediaType.APPLICATION_JSON)
          .content(json.writeValueAsString(body))
      )
      .andExpect(status().isBadRequest());
    mvc
      .perform(
        delete("/api/apis/" + id).header("Authorization", "Bearer " + owner)
      )
      .andExpect(status().isNoContent());
    mvc
      .perform(
        get("/api/apis/" + id).header("Authorization", "Bearer " + owner)
      )
      .andExpect(status().isNotFound())
      .andExpect(jsonPath("timestamp").exists());
  }

  @Test
  void realMonitoringRecoveryAnalyticsAndHistory() throws Exception {
    String token = register("monitor");
    long id = create(token);
    monitor.check(id);
    assertEquals("UP", apis.findById(id).orElseThrow().lastStatus);
    monitor.check(id);
    assertEquals(1, logs.count(), "Do not run before next due time");
    code.set(503);
    due(id);
    monitor.check(id);
    assertEquals(1, alerts.count());
    assertFalse(alerts.findAll().getFirst().resolved);
    due(id);
    monitor.check(id);
    assertEquals(
      1,
      alerts.count(),
      "Do not duplicate consecutive failure alerts"
    );
    code.set(200);
    due(id);
    monitor.check(id);
    assertTrue(alerts.findAll().getFirst().resolved);
    assertNotNull(alerts.findAll().getFirst().resolvedAt);
    mvc
      .perform(
        get("/api/analytics/" + id).header("Authorization", "Bearer " + token)
      )
      .andExpect(status().isOk())
      .andExpect(jsonPath("totalRequests").value(4))
      .andExpect(jsonPath("successfulRequests").value(2))
      .andExpect(jsonPath("failedRequests").value(2))
      .andExpect(jsonPath("uptime").value(50.0));
    mvc
      .perform(
        get("/api/monitoring")
          .param("api", String.valueOf(id))
          .param("status", "DOWN")
          .param("statusCode", "503")
          .param(
            "date",
            java.time.LocalDate.now(java.time.ZoneOffset.UTC).toString()
          )
          .param("size", "1")
          .header("Authorization", "Bearer " + token)
      )
      .andExpect(status().isOk())
      .andExpect(jsonPath("totalElements").value(2))
      .andExpect(jsonPath("totalPages").value(2))
      .andExpect(jsonPath("content[0].statusCode").value(503));
    mvc
      .perform(
        get("/api/monitoring")
          .param("date", "invalid")
          .header("Authorization", "Bearer " + token)
      )
      .andExpect(status().isBadRequest());
    mvc
      .perform(
        delete("/api/apis/" + id).header("Authorization", "Bearer " + token)
      )
      .andExpect(status().isNoContent());
    assertEquals(0, logs.count());
    assertEquals(0, alerts.count());
  }

  @Test
  void emptyAnalyticsAndInitialFailure() throws Exception {
    String token = register("initial");
    mvc
      .perform(get("/api/analytics").header("Authorization", "Bearer " + token))
      .andExpect(status().isOk())
      .andExpect(jsonPath("totalRequests").value(0))
      .andExpect(jsonPath("uptime").isEmpty());
    code.set(500);
    long id = create(token);
    monitor.check(id);
    assertEquals(1, alerts.count());
    long alertId = alerts.findAll().getFirst().id;
    String other = register("different");
    mvc
      .perform(
        put("/api/alerts/" + alertId + "/resolve").header(
          "Authorization",
          "Bearer " + other
        )
      )
      .andExpect(status().isNotFound());
    mvc
      .perform(
        put("/api/alerts/" + alertId + "/resolve").header(
          "Authorization",
          "Bearer " + token
        )
      )
      .andExpect(status().isOk())
      .andExpect(jsonPath("resolved").value(true));
    var a = apis.findById(id).orElseThrow();
    a.active = false;
    a.nextCheckAt = Instant.now().minusSeconds(1);
    apis.save(a);
    monitor.check(id);
    assertEquals(1, logs.count());
  }

  @Test
  void countsOlderUnresolvedAlertsAcrossAllPages() throws Exception {
    String token = register("alertowner"),
      other = register("observer");
    long id = create(token);
    var api = apis.findById(id).orElseThrow();
    var incident = new Alert();
    incident.api = api;
    incident.apiName = api.name;
    incident.message = "Older failure";
    incident.createdAt = Instant.now().minusSeconds(100);
    alerts.save(incident);
    for (int i = 0; i < 21; i++) {
      var resolved = new Alert();
      resolved.api = api;
      resolved.apiName = api.name;
      resolved.message = "Recovered";
      resolved.resolved = true;
      resolved.resolvedAt = Instant.now();
      alerts.save(resolved);
    }
    mvc
      .perform(
        get("/api/alerts/unresolved-count").header(
          "Authorization",
          "Bearer " + token
        )
      )
      .andExpect(status().isOk())
      .andExpect(jsonPath("count").value(1));
    mvc
      .perform(
        get("/api/alerts/unresolved-count").header(
          "Authorization",
          "Bearer " + other
        )
      )
      .andExpect(jsonPath("count").value(0));
  }

  @Test
  void rejectsPasswordsExceedingBcryptByteLimit() throws Exception {
    mvc
      .perform(
        post("/api/auth/register")
          .contentType(MediaType.APPLICATION_JSON)
          .content(
            json.writeValueAsString(
              Map.of(
                "username",
                "unicode",
                "email",
                "unicode@example.com",
                "password",
                "é".repeat(40)
              )
            )
          )
      )
      .andExpect(status().isBadRequest())
      .andExpect(
        jsonPath("message").value("Password must be at most 72 UTF-8 bytes")
      );
  }
}
