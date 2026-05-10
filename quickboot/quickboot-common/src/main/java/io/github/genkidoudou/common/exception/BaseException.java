package io.github.genkidoudou.common.exception;

import io.github.genkidoudou.common.api.HttpCodes;

/**
 * 统一异常基类，携带业务错误码、默认文案与国际化占位参数。
 * <p>
 * 约定：{@code code} 为空时会兜底为 {@link HttpCodes#INTERNAL_ERROR}，用于保证响应链路始终有可用错误码。
 */
public class BaseException extends RuntimeException {

    private final Integer code;
    private final String msg;
    private final Object[] args;

    /**
     * @param code 业务错误码；允许为空，为空时兜底为 {@link HttpCodes#INTERNAL_ERROR}
     * @param msg  默认文案（i18n 未命中时回退）
     */
    public BaseException(Integer code, String msg) {
        this(code, msg, null, null);
    }

    /**
     * @param code 业务错误码；允许为空，为空时兜底为 {@link HttpCodes#INTERNAL_ERROR}
     * @param msg  默认文案（i18n 未命中时回退）
     * @param args 国际化占位参数
     */
    public BaseException(Integer code, String msg, Object[] args) {
        this(code, msg, args, null);
    }

    /**
     * @param code  业务错误码；允许为空，为空时兜底为 {@link HttpCodes#INTERNAL_ERROR}
     * @param msg   默认文案（i18n 未命中时回退）
     * @param cause 原始异常
     */
    public BaseException(Integer code, String msg, Throwable cause) {
        this(code, msg, null, cause);
    }

    /**
     * @param code  业务错误码；允许为空，为空时兜底为 {@link HttpCodes#INTERNAL_ERROR}
     * @param msg   默认文案（i18n 未命中时回退）
     * @param args  国际化占位参数
     * @param cause 原始异常
     */
    public BaseException(Integer code, String msg, Object[] args, Throwable cause) {
        super(msg, cause);
        this.code = code != null ? code : HttpCodes.INTERNAL_ERROR;
        this.msg = msg;
        this.args = args;
    }

    /** @return 业务错误码（永不为空） */
    public Integer getCode() {
        return code;
    }

    /** @return 默认文案（允许为空） */
    public String getMsg() {
        return msg;
    }

    /** @return 国际化占位参数（允许为空） */
    public Object[] getArgs() {
        return args;
    }
}
