package io.qrun.qqq.platform.app;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

@RestControllerAdvice
class GlobalApiExceptionHandler {
  private static final Logger log = LoggerFactory.getLogger(GlobalApiExceptionHandler.class);

  @ExceptionHandler(ApiException.class)
  ResponseEntity<?> api(ApiException ex, HttpServletRequest request) {
    return error(ex.status, ex.code, ex.getMessage(), request);
  }

  @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class, ConstraintViolationException.class,
      HttpMessageNotReadableException.class, IllegalArgumentException.class})
  ResponseEntity<?> validation(Exception ex, HttpServletRequest request) {
    String message = ex.getMessage();
    if (ex instanceof MethodArgumentNotValidException invalid) {
      message = invalid.getBindingResult().getFieldErrors().stream()
          .map(e -> e.getField() + ": " + e.getDefaultMessage()).findFirst().orElse("Invalid request");
    }
    return error(HttpStatus.BAD_REQUEST, "validation_failed", message, request);
  }

  @ExceptionHandler(MaxUploadSizeExceededException.class)
  ResponseEntity<?> tooLarge(Exception ex, HttpServletRequest request) {
    return error(HttpStatus.PAYLOAD_TOO_LARGE, "file_too_large", "Upload exceeds the configured limit", request);
  }

  @ExceptionHandler(DataIntegrityViolationException.class)
  ResponseEntity<?> conflict(Exception ex, HttpServletRequest request) {
    return error(HttpStatus.CONFLICT, "conflict", "The request conflicts with existing data", request);
  }

  @ExceptionHandler(Exception.class)
  ResponseEntity<?> unexpected(Exception ex, HttpServletRequest request) {
    log.error("Unhandled API error for {}", request.getRequestURI(), ex);
    return error(HttpStatus.INTERNAL_SERVER_ERROR, "internal_error", "An internal error occurred", request);
  }

  private ResponseEntity<?> error(HttpStatus status, String code, String message, HttpServletRequest request) {
    Map<String, Object> body = new LinkedHashMap<>();
    body.put("timestamp", Instant.now());
    body.put("status", status.value());
    body.put("code", code);
    body.put("message", message == null ? status.getReasonPhrase() : message);
    body.put("path", request.getRequestURI());
    return ResponseEntity.status(status).body(body);
  }
}
