package io.github.genkidoudou.common.exception;

import cn.dev33.satoken.exception.NotLoginException;
import io.github.genkidoudou.common.core.R;
import io.github.genkidoudou.common.i18n.I18nUtil;
import io.github.genkidoudou.common.firewall.client.exception.ClientAuthException;
import io.github.genkidoudou.common.firewall.idempotent.IdempotentException;
import io.github.genkidoudou.common.firewall.methodandhost.MethodAndHostException;
import io.github.genkidoudou.common.firewall.referer.RefererException;
import io.github.genkidoudou.common.firewall.sensitiveword.SensitiveWordException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器
 *
 * @author genkidoudou
 * @since 2026/03/05
 */
@RestControllerAdvice(basePackages = "io.github.genkidoudou.common")
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);


    /**
     * 处理基础异常
     *
     * @param ex      基础异常
     * @param request 请求对象
     * @return 错误响应
     * @since 2026/03/05
     */
    @ExceptionHandler(BaseException.class)
    public ResponseEntity<R<Void>> handleBaseException(BaseException ex, HttpServletRequest request) {
        log.warn("BaseException: code={}, msg={}", ex.getCode(), ex.getMsg(), ex);

        String message = getI18nMessage(ex.getCode(), ex.getArgs(), ex.getMsg());
        R<Void> result = R.error(ex.getCode(), message);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
    }

    /**
     * 处理错误异常
     *
     * @param ex      错误异常
     * @param request 请求对象
     * @return 错误响应
     * @since 2026/03/05
     */
    @ExceptionHandler(ErrorException.class)
    public ResponseEntity<R<Void>> handleErrorException(ErrorException ex, HttpServletRequest request) {
        log.error("ErrorException: code={}, msg={}", ex.getCode(), ex.getMsg(), ex);

        String message = getI18nMessage(ex.getCode(), ex.getArgs(), ex.getMsg());
        R<Void> result = R.error(ex.getCode(), message);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
    }

    /**
     * 处理警告异常
     *
     * @param ex      警告异常
     * @param request 请求对象
     * @return 错误响应
     * @since 2026/03/05
     */
    @ExceptionHandler(WarningException.class)
    public ResponseEntity<R<Void>> handleWarningException(WarningException ex, HttpServletRequest request) {
        log.warn("WarningException: code={}, msg={}", ex.getCode(), ex.getMsg(), ex);

        String message = getI18nMessage(ex.getCode(), ex.getArgs(), ex.getMsg());
        R<Void> result = R.error(ex.getCode(), message);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
    }

    /**
     * 处理客户端认证异常
     *
     * @param ex      客户端认证异常
     * @param request 请求对象
     * @return 错误响应
     * @since 2026/03/05
     */
    @ExceptionHandler(ClientAuthException.class)
    public ResponseEntity<R<Void>> handleClientAuthException(ClientAuthException ex, HttpServletRequest request) {
        log.warn("ClientAuthException: code={}, msg={}", ex.getCode(), ex.getMsg());

        String message = getI18nMessage(ex.getCode(), ex.getArgs(), ex.getMsg());
        R<Void> result = R.error(ex.getCode(), message);

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(result);
    }

    /**
     * 处理幂等异常
     *
     * @param ex      幂等异常
     * @param request 请求对象
     * @return 错误响应
     * @since 2026/03/05
     */
    @ExceptionHandler(IdempotentException.class)
    public ResponseEntity<R<Void>> handleIdempotentException(IdempotentException ex, HttpServletRequest request) {
        log.warn("IdempotentException: code={}, msg={}", ex.getCode(), ex.getMsg());

        String message = getI18nMessage(ex.getCode(), ex.getArgs(), ex.getMsg());
        R<Void> result = R.error(ex.getCode(), message);

        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(result);
    }

    /**
     * 处理请求来源拦截异常
     *
     * @param ex      请求来源拦截异常
     * @param request 请求对象
     * @return 错误响应
     * @since 2026/03/05
     */
    @ExceptionHandler(RefererException.class)
    public ResponseEntity<R<Void>> handleRefererException(RefererException ex, HttpServletRequest request) {
        log.warn("RefererException: code={}, msg={}", ex.getCode(), ex.getMsg());

        String message = getI18nMessage(ex.getCode(), ex.getArgs(), ex.getMsg());
        R<Void> result = R.error(ex.getCode(), message);

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(result);
    }

    /**
     * 处理请求方式和域名拦截异常
     *
     * @param ex      请求方式和域名拦截异常
     * @param request 请求对象
     * @return 错误响应
     * @since 2026/03/05
     */
    @ExceptionHandler(MethodAndHostException.class)
    public ResponseEntity<R<Void>> handleMethodAndHostException(MethodAndHostException ex, HttpServletRequest request) {
        log.warn("MethodAndHostException: code={}, msg={}", ex.getCode(), ex.getMsg());

        String message = getI18nMessage(ex.getCode(), ex.getArgs(), ex.getMsg());
        R<Void> result = R.error(ex.getCode(), message);

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(result);
    }

    /**
     * 处理敏感词异常
     *
     * @param ex      敏感词异常
     * @param request 请求对象
     * @return 错误响应
     * @since 2026/03/05
     */
    @ExceptionHandler(SensitiveWordException.class)
    public ResponseEntity<R<Void>> handleSensitiveWordException(SensitiveWordException ex, HttpServletRequest request) {
        log.warn("SensitiveWordException: code={}, msg={}, sensitiveWord={}", ex.getCode(), ex.getMsg(), ex.getSensitiveWord());

        String message = getI18nMessage(ex.getCode(), ex.getArgs(), ex.getMsg());
        R<Void> result = R.error(ex.getCode(), message);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
    }

    /**
     * 处理其他未捕获的异常
     *
     * @param ex      异常
     * @param request 请求对象
     * @return 错误响应
     * @since 2026/03/05
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<R<Void>> handleException(Exception ex, HttpServletRequest request) {
        log.error("Unexpected exception occurred", ex);

        R<Void> result = R.error(ErrorCode.INTERNAL_ERROR, "系统内部错误");

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
    }

    /**
     * 获取国际化消息
     *
     * @param code           错误码
     * @param args           参数
     * @param defaultMessage 默认消息
     * @return 国际化消息
     * @since 2026/03/05
     */
    private String getI18nMessage(Integer code, Object[] args, String defaultMessage) {
        if (null == code) {
            return defaultMessage;
        }

        try {
            String i18nMessage = I18nUtil.getMessage(code + "", args, defaultMessage);
            if (i18nMessage != null && !i18nMessage.equals(String.valueOf(code))) {
                return i18nMessage;
            }
        } catch (Exception e) {
            log.debug("Failed to get i18n message for code: {}", code, e);
        }

        return defaultMessage;
    }
}
