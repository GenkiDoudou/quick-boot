package io.github.genkidoudou.core.exception;

import cn.dev33.satoken.exception.NotLoginException;
import io.github.genkidoudou.common.api.R;
import io.github.genkidoudou.common.exception.ErrorCodes;
import io.github.genkidoudou.common.exception.ErrorException;
import io.github.genkidoudou.common.exception.WarningException;
import io.github.genkidoudou.common.file.FileStorageException;
import io.github.genkidoudou.common.i18n.I18nUtil;
import io.github.genkidoudou.common.security.firewall.idempotent.IdempotentException;
import io.github.genkidoudou.common.security.firewall.sensitiveword.SensitiveWordException;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Objects;

/**
 * 全局异常处理：统一将异常映射为 {@link R}，并设置合适 HTTP 状态。

 */
@Slf4j
@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class GlobalExceptionHandler {

  private static final String DEFAULT_FALLBACK_MESSAGE = "系统繁忙，请稍后再试";

  /**
   * 处理可预期异常。
   *
   * @param ex 可预期异常
   * @return 统一响应
   */
  @ExceptionHandler(WarningException.class)
  public ResponseEntity<R<Void>> handleWarningException(WarningException ex) {
    int code = ex.getCode();
    String message = resolveMessage(code, ex.getArgs(), ex.getMsg());
    HttpStatus status = resolveWarningStatus(ex);
    log.warn("warning exception, status={}, code={}, msg={}", status.value(), code, message, ex);
    return ResponseEntity.status(status).body(R.error(code, message));
  }

  /**
   * 处理严重异常。
   *
   * @param ex 严重异常
   * @return 统一响应
   */
  @ExceptionHandler(ErrorException.class)
  public ResponseEntity<R<Void>> handleErrorException(ErrorException ex) {
    int code = normalizeCode(ex.getCode());
    String message = resolveMessage(code, ex.getArgs(), ex.getMsg());
    log.error("error exception, code={}, msg={}", code, message, ex);
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(R.error(code, message));
  }


  /**
   * 数据库唯一约束冲突（含 MyBatis 包装的 {@link DuplicateKeyException}），映射为 400 与可读文案。
   */
  @ExceptionHandler(DataIntegrityViolationException.class)
  public ResponseEntity<R<Void>> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
    String text = collectThrowableMessages(ex).toLowerCase();
    String message = "数据已存在，请勿重复提交";
    if (text.contains("uk_sys_role_key")) {
      message = "权限字符已存在";
    }
    int code = ErrorCodes.Common.INVALID_PARAM;
    log.warn("data integrity violation, code={}, msg={}", code, message, ex);
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(R.error(code, message));
  }


  /**
   * 兜底未捕获异常。
   */
  @ExceptionHandler(Throwable.class)
  public ResponseEntity<R<Void>> handleThrowable(Throwable ex) {
    int code = ErrorCodes.System.INTERNAL_ERROR;
    String message = resolveMessage(code, null, DEFAULT_FALLBACK_MESSAGE);
    log.error("unhandled exception, code={}, msg={}", code, message, ex);
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(R.error(code, message));
  }

  /**
   * 处理 {@code @RequestBody @Valid} 校验失败。
   */
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<R<Void>> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
    String message = ex.getBindingResult().getFieldErrors().stream()
      .findFirst()
      .map(err -> err.getDefaultMessage())
      .orElse("请求参数不合法");
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
      .body(R.error(ErrorCodes.Common.INVALID_PARAM, message));
  }

  /**
   * 处理 query/path 绑定与校验失败。
   */
  @ExceptionHandler({ConstraintViolationException.class, BindException.class})
  public ResponseEntity<R<Void>> handleConstraintViolation(Exception ex) {
    String message = ex.getMessage() != null ? ex.getMessage() : "请求参数不合法";
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
      .body(R.error(ErrorCodes.Common.INVALID_PARAM, message));
  }

  /**
   * 拼接异常链上的 message，便于识别底层库返回的唯一约束名。
   */
  private static String collectThrowableMessages(Throwable ex) {
    StringBuilder sb = new StringBuilder();
    for (Throwable t = ex; t != null; t = t.getCause()) {
      if (t.getMessage() != null) {
        sb.append(' ').append(t.getMessage());
      }
    }
    return sb.toString();
  }
}
