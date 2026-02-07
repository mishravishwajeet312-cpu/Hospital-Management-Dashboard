package com.hospital.common;

import com.hospital.common.exception.InvalidCredentialsException;
import com.hospital.common.exception.ResourceNotFoundException;
import com.hospital.common.exception.UserAlreadyExistsException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(UserAlreadyExistsException.class)
  public ResponseEntity<ApiError> handleUserAlreadyExists(UserAlreadyExistsException ex, HttpServletRequest request) {
    return build(ex.getMessage(), HttpStatus.BAD_REQUEST, request, null);
  }

  @ExceptionHandler(InvalidCredentialsException.class)
  public ResponseEntity<ApiError> handleInvalidCredentials(InvalidCredentialsException ex, HttpServletRequest request) {
    return build(ex.getMessage(), HttpStatus.UNAUTHORIZED, request, null);
  }

  @ExceptionHandler(ResourceNotFoundException.class)
  public ResponseEntity<ApiError> handleNotFound(ResourceNotFoundException ex, HttpServletRequest request) {
    return build(ex.getMessage(), HttpStatus.NOT_FOUND, request, null);
  }

  @ExceptionHandler(EntityNotFoundException.class)
  public ResponseEntity<ApiError> handleEntityNotFound(EntityNotFoundException ex, HttpServletRequest request) {
    return build("Resource not found", HttpStatus.NOT_FOUND, request, null);
  }

  @ExceptionHandler(AccessDeniedException.class)
  public ResponseEntity<ApiError> handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
    return build("Access denied", HttpStatus.FORBIDDEN, request, null);
  }

  @ExceptionHandler(BadCredentialsException.class)
  public ResponseEntity<ApiError> handleBadCredentials(BadCredentialsException ex, HttpServletRequest request) {
    return build("Bad credentials", HttpStatus.UNAUTHORIZED, request, null);
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
    Map<String, String> errors = new LinkedHashMap<>();
    ex.getBindingResult().getFieldErrors()
        .forEach(err -> errors.put(err.getField(), err.getDefaultMessage()));

    return build("Validation failed", HttpStatus.BAD_REQUEST, request, errors);
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<ApiError> handleInvalidJson(HttpMessageNotReadableException ex, HttpServletRequest request) {
    return build("Malformed JSON request", HttpStatus.BAD_REQUEST, request, null);
  }

  @ExceptionHandler(MaxUploadSizeExceededException.class)
  public ResponseEntity<ApiError> handleMaxSize(MaxUploadSizeExceededException ex, HttpServletRequest request) {
    return build("File size exceeds maximum limit", HttpStatus.BAD_REQUEST, request, null);
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<ApiError> handleIllegalArgument(IllegalArgumentException ex, HttpServletRequest request) {
    return build(ex.getMessage(), HttpStatus.BAD_REQUEST, request, null);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiError> handleGeneric(Exception ex, HttpServletRequest request) {
    return build("Internal server error", HttpStatus.INTERNAL_SERVER_ERROR, request, null);
  }

  private ResponseEntity<ApiError> build(
      String message,
      HttpStatus status,
      HttpServletRequest request,
      Map<String, String> fieldErrors) {

    ApiError error = new ApiError(
        Instant.now(),
        status.value(),
        status.getReasonPhrase(),
        message,
        request.getRequestURI(),
        fieldErrors
    );

    return ResponseEntity.status(status).body(error);
  }
}
