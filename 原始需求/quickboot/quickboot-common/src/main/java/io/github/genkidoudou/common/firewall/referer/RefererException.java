package io.github.genkidoudou.common.firewall.referer;

import io.github.genkidoudou.common.exception.ErrorCode;
import io.github.genkidoudou.common.exception.WarningException;

/**
 * 请求来源拦截异常
 *
 * @author QuickBoot
 * @since 2026/03/03
 */
public class RefererException extends WarningException {

    public RefererException(Integer code, String msg) {
        super(code, msg);
    }

    public RefererException(Integer code, String msg, Object[] args) {
        super(code, msg, args);
    }

    public RefererException(Integer code, String msg, Throwable cause) {
        super(code, msg, cause);
    }

    /**
     * 请求来源不允许
     *
     * @param referer 请求来源
     * @return 请求来源拦截异常
     * @since 2026/03/05
     */
    public static RefererException notAllowed(String referer) {
        return new RefererException(ErrorCode.REFERER_NOT_ALLOWED, "请求来源不允许", new Object[]{referer});
    }
}
