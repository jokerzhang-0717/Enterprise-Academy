package com.joker.emp.common;

import jakarta.validation.ConstraintViolationException;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

@RestControllerAdvice
public class GlobalExceptionHandler {

  private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

  @ExceptionHandler(BizException.class)
  public ResponseEntity<ApiResponse<Void>> handleBiz(BizException ex) {
    HttpStatus status = switch (ex.getCode()) {
      case 401 -> HttpStatus.UNAUTHORIZED;
      case 403 -> HttpStatus.FORBIDDEN;
      case 404 -> HttpStatus.NOT_FOUND;
      case 409 -> HttpStatus.CONFLICT;
      case 429 -> HttpStatus.TOO_MANY_REQUESTS;
      default -> HttpStatus.BAD_REQUEST;
    };
    return ResponseEntity.status(status).body(ApiResponse.fail(ex.getCode(), ex.getMessage()));
  }

  @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
  public ResponseEntity<ApiResponse<Void>> handleValidation(Exception ex) {
    var result = ex instanceof MethodArgumentNotValidException manv
        ? manv.getBindingResult()
        : ((BindException) ex).getBindingResult();
    String message = result.getFieldErrors().stream()
        .map(FieldError::getDefaultMessage)
        .collect(Collectors.joining("；"));
    return ResponseEntity.badRequest().body(ApiResponse.fail(400, message.isBlank() ? "参数校验失败" : message));
  }

  @ExceptionHandler(ConstraintViolationException.class)
  public ResponseEntity<ApiResponse<Void>> handleConstraint(ConstraintViolationException ex) {
    return ResponseEntity.badRequest().body(ApiResponse.fail(400, ex.getMessage()));
  }

  @ExceptionHandler(MissingServletRequestParameterException.class)
  public ResponseEntity<ApiResponse<Void>> handleMissingParam(MissingServletRequestParameterException ex) {
    return ResponseEntity.badRequest().body(ApiResponse.fail(400, "缺少参数：" + ex.getParameterName()));
  }

  @ExceptionHandler(MaxUploadSizeExceededException.class)
  public ResponseEntity<ApiResponse<Void>> handleUploadTooLarge(MaxUploadSizeExceededException ex) {
    return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
        .body(ApiResponse.fail(413, "上传文件超过大小限制"));
  }

  @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
  public ResponseEntity<ApiResponse<Void>> handleMethodNotSupported(
      HttpRequestMethodNotSupportedException ex) {
    return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
        .body(ApiResponse.fail(405, "不支持的请求方法：" + ex.getMethod()));
  }

  @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
  public ResponseEntity<ApiResponse<Void>> handleMediaType(HttpMediaTypeNotSupportedException ex) {
    return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
        .body(ApiResponse.fail(415, "不支持的请求格式：" + ex.getContentType()));
  }

  @ExceptionHandler({MethodArgumentTypeMismatchException.class,
      HttpMessageNotReadableException.class, MissingServletRequestPartException.class})
  public ResponseEntity<ApiResponse<Void>> handleBadRequest(Exception ex) {
    return ResponseEntity.badRequest().body(ApiResponse.fail(400, "请求参数格式不正确"));
  }

  @ExceptionHandler(DataIntegrityViolationException.class)
  public ResponseEntity<ApiResponse<Void>> handleDataIntegrity(DataIntegrityViolationException ex) {
    log.warn("Data integrity violation: {}", ex.getMostSpecificCause().getMessage());
    return ResponseEntity.badRequest()
        .body(ApiResponse.fail(400, "数据约束校验失败，请检查关联数据或重复项"));
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiResponse<Void>> handleAny(Exception ex) {
    log.error("Unhandled exception", ex);
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(ApiResponse.fail(500, "服务器内部错误，请稍后重试"));
  }
}
