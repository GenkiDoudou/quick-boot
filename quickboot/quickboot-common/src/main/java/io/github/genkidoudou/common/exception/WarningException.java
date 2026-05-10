package io.github.genkidoudou.common.exception;

/**
 * 可预期异常：用于业务校验失败、安全拦截等可被调用方理解和处理的场景。
 */
public class WarningException extends BaseException {

    /**
     * @param code 业务错误码
     * @param msg  默认文案
     */
    public WarningException(Integer code, String msg) {
        super(code, msg);
    }

    /**
     * @param code 业务错误码
     * @param msg  默认文案
     * @param args 国际化占位参数
     */
    public WarningException(Integer code, String msg, Object[] args) {
        super(code, msg, args);
    }

    /**
     * @param code  业务错误码
     * @param msg   默认文案
     * @param cause 原始异常
     */
    public WarningException(Integer code, String msg, Throwable cause) {
        super(code, msg, cause);
    }

    /**
     * @param code  业务错误码
     * @param msg   默认文案
     * @param args  国际化占位参数
     * @param cause 原始异常
     */
    public WarningException(Integer code, String msg, Object[] args, Throwable cause) {
        super(code, msg, args, cause);
    }
}
