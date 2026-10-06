package com.apiwatch.exception;

import java.time.Instant;
import java.util.Map;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

@RestControllerAdvice
public class ExceptionHandlerAdvice {

  private ResponseEntity<?> error(int status, String message) {
    return ResponseEntity.status(status).body(
      Map.of("timestamp", Instant.now(), "status", status, "message", message)
    );
  }

  @ExceptionHandler(ApiException.class)
  ResponseEntity<?> domain(ApiException e) {
    return error(e.status, e.getMessage());
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  ResponseEntity<?> validation(MethodArgumentNotValidException e) {
    return error(
      400,
      e
        .getBindingResult()
        .getFieldErrors()
        .stream()
        .map(x -> x.getField() + ": " + x.getDefaultMessage())
        .findFirst()
        .orElse("Invalid input")
    );
  }

  @ExceptionHandler({
    IllegalArgumentException.class,
    org.springframework.web.method.annotation.MethodArgumentTypeMismatchException.class,
    org.springframework.http.converter.HttpMessageNotReadableException.class,
  })
  ResponseEntity<?> invalid(Exception e) {
    return error(400, "Invalid request parameters");
  }

  @ExceptionHandler(DataIntegrityViolationException.class)
  ResponseEntity<?> conflict(Exception e) {
    return error(
      409,
      "A record with these details already exists or is in use"
    );
  }

  @ExceptionHandler(Exception.class)
  ResponseEntity<?> unexpected(Exception e) {
    org.slf4j.LoggerFactory.getLogger(getClass()).error("Request failed", e);
    return error(500, "Unable to process request. Please try again.");
  }
}
