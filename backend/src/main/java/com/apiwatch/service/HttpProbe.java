package com.apiwatch.service;

import com.apiwatch.entity.ApiDetails;
import jakarta.annotation.PreDestroy;
import java.net.http.*;
import java.time.Duration;
import java.util.concurrent.*;
import org.springframework.stereotype.Component;

@Component
public class HttpProbe implements AutoCloseable {

  public record Result(
    String status,
    Integer code,
    long elapsed,
    String error
  ) {}

  private final TargetValidator targets;
  // Reuse connections, and never follow redirects to unintended destinations.
  private final HttpClient client = HttpClient.newBuilder()
    .followRedirects(HttpClient.Redirect.NEVER)
    .connectTimeout(Duration.ofSeconds(30))
    .build();

  public HttpProbe(TargetValidator targets) {
    this.targets = targets;
  }

  public Result check(ApiDetails api) {
    long start = System.nanoTime();
    CompletableFuture<HttpResponse<Void>> pending = null;
    try {
      var uri = targets.checkDestination(api.url);
      var request = HttpRequest.newBuilder(uri)
        .timeout(Duration.ofSeconds(api.timeout))
        .header("User-Agent", "APIWatch/1.0")
        .method(api.httpMethod, HttpRequest.BodyPublishers.noBody())
        .build();
      pending = client.sendAsync(
        request,
        HttpResponse.BodyHandlers.discarding()
      );
      // The deadline includes downloading/discarding the body, not only headers.
      var response = pending.get(api.timeout, TimeUnit.SECONDS);
      int code = response.statusCode();
      boolean up = code == api.expectedStatusCode;
      return new Result(
        up ? "UP" : "DOWN",
        code,
        elapsed(start),
        up
          ? null
          : "Expected HTTP " + api.expectedStatusCode + ", received " + code
      );
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      return new Result("DOWN", null, elapsed(start), "Check interrupted");
    } catch (Exception e) {
      Throwable cause = e instanceof ExecutionException ? e.getCause() : e;
      if (
        cause instanceof TimeoutException ||
        cause instanceof HttpTimeoutException
      ) return new Result(
        "DOWN",
        null,
        elapsed(start),
        "Request timed out after " + api.timeout + " seconds"
      );
      String message =
        cause.getClass().getSimpleName() +
        ": " +
        (cause.getMessage() == null ? "Connection failed" : cause.getMessage());
      return new Result(
        "DOWN",
        null,
        elapsed(start),
        message.substring(0, Math.min(1900, message.length()))
      );
    } finally {
      if (pending != null && !pending.isDone()) pending.cancel(true);
    }
  }

  private long elapsed(long start) {
    return (System.nanoTime() - start) / 1_000_000;
  }

  @Override
  @PreDestroy
  public void close() {
    client.shutdownNow();
  }
}
