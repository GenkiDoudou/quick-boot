package io.github.genkidoudou.common.firewall.xss;

import io.github.genkidoudou.common.exception.ErrorCode;
import io.github.genkidoudou.common.exception.WarningException;

/**
 * XSS 脚本注入异常
 *
 * @author genkidoudou
 * @since 2026/03/06
 */
public class XssException extends WarningException {

    public XssException(Integer code, String msg) {
        super(code, msg);
    }

    public XssException(Integer code, String msg, Object[] args) {
        super(code, msg, args);
    }

    /**
     * 检测到 XSS 脚本
     *
     * @param param 参数名
     * @return XSS 异常
     * @since 2026/03/06
     */
    public static XssException scriptDetected(String param) {
        return new XssException(ErrorCode.XSS_SCRIPT_DETECTED, "请求参数包含非法脚本", new Object[]{param});
    }
}
