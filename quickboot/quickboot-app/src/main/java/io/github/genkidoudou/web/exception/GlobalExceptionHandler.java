package io.github.genkidoudou.web.exception;

import io.github.genkidoudou.common.api.HttpCodes;
import io.github.genkidoudou.common.api.R;
import io.github.genkidoudou.common.exception.ErrorCodes;
import io.github.genkidoudou.common.exception.ErrorException;
import io.github.genkidoudou.common.exception.WarningException;
import io.github.genkidoudou.common.i18n.I18nUtil;
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
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotPermissionException;
import cn.dev33.satoken.exception.NotRoleException;

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
    int code = normalizeCode(ex.getCode());
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
   * 数据库唯一约束冲突，映射为 400 与可读文案。
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
   * 无匹配 Handler / 静态资源不存在：应返回 HTTP 404，而不是落入兜底 500。
   * <p>
   * Spring Boot 3+ 在请求落到 {@code ResourceHttpRequestHandler} 且资源缺失时抛
   * {@link NoResourceFoundException}；未开启默认 Servlet 时也可能出现 {@link NoHandlerFoundException}。
   */
  @ExceptionHandler({NoResourceFoundException.class, NoHandlerFoundException.class})
  public ResponseEntity<R<Void>> handleNotFound(Exception ex) {
    int code = HttpCodes.NOT_FOUND;
    String message = resolveMessage(code, null, "访问资源不存在");
    if (ex instanceof NoResourceFoundException nrf) {
      log.warn("resource not found, path={}, code={}", nrf.getResourcePath(), code);
    } else if (ex instanceof NoHandlerFoundException nhf) {
      log.warn("handler not found, {} {}, code={}", nhf.getHttpMethod(), nhf.getRequestURL(), code);
    } else {
      log.warn("not found, code={}, msg={}", code, ex.getMessage());
    }
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(R.error(code, message));
  }

  /**
   * sa-token 未登录 / token 失效。
   */
  @ExceptionHandler(NotLoginException.class)
  public ResponseEntity<R<Void>> handleNotLogin(NotLoginException ex) {
    log.warn("not login: {}", ex.getMessage());
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
      .body(R.error(HttpCodes.UNAUTHORIZED, "登录状态已过期，请重新登录"));
  }

  /**
   * sa-token 无权限 / 无角色。
   */
  @ExceptionHandler({NotPermissionException.class, NotRoleException.class})
  public ResponseEntity<R<Void>> handleNotPermission(RuntimeException ex) {
    log.warn("not permission: {}", ex.getMessage());
    return ResponseEntity.status(HttpStatus.FORBIDDEN)
      .body(R.error(HttpCodes.FORBIDDEN, "无权限访问"));
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

  private HttpStatus resolveWarningStatus(WarningException ex) {
    Integer code = ex.getCode();
    if (Objects.equals(code, HttpCodes.UNAUTHORIZED)) {
      return HttpStatus.UNAUTHORIZED;
    }
    if (Objects.equals(code, HttpCodes.FORBIDDEN)) {
      return HttpStatus.FORBIDDEN;
    }
    // ErrorCodes.Auth（3xxxx）：登录/验证码失败统一 401
    if (code != null && code >= 30000 && code < 40000) {
      return HttpStatus.UNAUTHORIZED;
    }
    // 系统级 Warning 按 500
    if (Objects.equals(code, ErrorCodes.System.INTERNAL_ERROR)
      || Objects.equals(code, ErrorCodes.System.DEPENDENCY_UNAVAILABLE)) {
      return HttpStatus.INTERNAL_SERVER_ERROR;
    }
    return HttpStatus.BAD_REQUEST;
  }

  private int normalizeCode(Integer code) {
    if (code == null) {
      return ErrorCodes.System.INTERNAL_ERROR;
    }
    return code;
  }

  /**
   * 优先用异常自带文案；否则走 i18n；再不行用统一兜底。
   */
  private String resolveMessage(int code, Object[] args, String defaultMsg) {
    if (defaultMsg != null && !defaultMsg.isBlank()) {
      return defaultMsg;
    }
    String fromI18n = I18nUtil.getMessage(code, args, null);
    if (fromI18n != null && !fromI18n.isBlank()) {
      return fromI18n;
    }
    return DEFAULT_FALLBACK_MESSAGE;
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
