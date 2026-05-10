package io.github.genkidoudou.web.common.exception;

import cn.dev33.satoken.exception.NotLoginException;
import io.github.genkidoudou.common.api.HttpCodes;
import io.github.genkidoudou.common.api.R;
import io.github.genkidoudou.common.exception.BaseException;
import io.github.genkidoudou.common.exception.ErrorCodes;
import io.github.genkidoudou.common.exception.ErrorException;
import io.github.genkidoudou.common.exception.WarningException;
import io.github.genkidoudou.common.i18n.I18nUtil;
import io.github.genkidoudou.common.security.firewall.idempotent.IdempotentException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import jakarta.validation.ConstraintViolationException;

import java.util.Objects;

/**
 * 全局异常处理：统一将异常映射为 {@link R}，并设置合适 HTTP 状态。
 * <p>
 * 规则：
 * 1. {@link WarningException} 视为可预期异常，按映射表返回 4xx/429。
 * 2. {@link ErrorException} 与兜底 {@link Throwable} 统一返回 HTTP 500。
 * 3. 文案优先走 i18n，未命中回退异常默认文案，再回退统一兜底文案。
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
     * Sa-Token 未登录异常映射到 401。
     */
    @ExceptionHandler(NotLoginException.class)
    public ResponseEntity<R<Void>> handleNotLogin(NotLoginException ex) {
        int code = ErrorCodes.Security.UNAUTHORIZED;
        String message = resolveMessage(code, null, "未登录或登录已过期");
        log.warn("not login, code={}, msg={}", code, message);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(R.error(code, message));
    }

    /**
     * 幂等重复映射到 429。
     */
    @ExceptionHandler(IdempotentException.class)
    public ResponseEntity<R<Void>> handleIdempotent(IdempotentException ex) {
        int code = ex.getCode();
        String message = resolveMessage(code, null, ex.getMessage());
        log.warn("idempotent repeat, code={}, msg={}", code, message);
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(R.error(code, message));
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
        if (Objects.equals(code, ErrorCodes.Security.UNAUTHORIZED)) {
            return HttpStatus.UNAUTHORIZED;
        }
        if (Objects.equals(code, ErrorCodes.Security.FORBIDDEN) || Objects.equals(code, ErrorCodes.Security.HOST_NOT_ALLOWED)) {
            return HttpStatus.FORBIDDEN;
        }
        if (Objects.equals(code, ErrorCodes.Security.RATE_LIMITED) || Objects.equals(code, ErrorCodes.Biz.IDEMPOTENT_REPEAT)) {
            return HttpStatus.TOO_MANY_REQUESTS;
        }
        return HttpStatus.BAD_REQUEST;
    }

    private int normalizeCode(Integer code) {
        if (code == null) {
            return ErrorCodes.System.INTERNAL_ERROR;
        }
        return code;
    }

    private String resolveMessage(int code, Object[] args, String defaultMsg) {
        String fallback = defaultMsg != null && !defaultMsg.isBlank() ? defaultMsg : DEFAULT_FALLBACK_MESSAGE;
        String i18n = I18nUtil.getMessage(String.valueOf(code), args, fallback);
        if (i18n == null || i18n.isBlank()) {
            return fallback;
        }
        return i18n;
    }
}
