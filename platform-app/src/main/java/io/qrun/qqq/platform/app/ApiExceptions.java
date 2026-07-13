package io.qrun.qqq.platform.app;

import org.springframework.http.HttpStatus;

class ApiException extends RuntimeException {
  final HttpStatus status;
  final String code;

  ApiException(HttpStatus status, String code, String message) {
    super(message);
    this.status = status;
    this.code = code;
  }

  static ApiException notFound(String resource) {
    return new ApiException(HttpStatus.NOT_FOUND, "not_found", resource + " not found");
  }

  static ApiException forbidden(String message) {
    return new ApiException(HttpStatus.FORBIDDEN, "forbidden", message);
  }

  static ApiException conflict(String message) {
    return new ApiException(HttpStatus.CONFLICT, "conflict", message);
  }
}
