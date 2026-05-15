package io.github.genkidoudou.common.firewall.methodandhost;

import io.github.genkidoudou.common.exception.ErrorCode;
import io.github.genkidoudou.common.exception.WarningException;

/**
 * 请求方式和域名拦截异常
 *
 * @author QuickBoot
 * @since 2026/03/02
 */
public class MethodAndHostException extends WarningException {

    public MethodAndHostException(Integer code, String msg) {
        super(code, msg);
    }

    public MethodAndHostException(Integer code, String msg, Object[] args) {
        super(code, msg, args);
    }

    public MethodAndHostException(Integer code, String msg, Throwable cause) {
        super(code, msg, cause);
    }

    /**
     * 请求方式不允许
     *
     * @param method 请求方式
     * @return 请求方式和域名拦截异常
     * @since 2026/03/05
     */
    public static MethodAndHostException methodNotAllowed(String method) {
        return new MethodAndHostException(ErrorCode.METHOD_NOT_ALLOWED, "请求方式不允许", new Object[]{method});
    }

    /**
     * 请求域名不允许
     *
     * @param host 请求域名
     * @return 请求方式和域名拦截异常
     * @since 2026/03/05
     */
    public static MethodAndHostException hostNotAllowed(String host) {
        return new MethodAndHostException(ErrorCode.HOST_NOT_ALLOWED, "请求域名不允许", new Object[]{host});
    }
}
