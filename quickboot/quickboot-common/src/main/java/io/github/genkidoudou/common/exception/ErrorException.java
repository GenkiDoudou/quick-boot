package io.github.genkidoudou.common.exception;

/**
 * 严重异常：用于系统内部故障、关键依赖失败等需要按 5xx 语义处理的场景。
 */
public class ErrorException extends BaseException {

    /**
     * @param code 业务错误码
     * @param msg  默认文案
     */
    public ErrorException(Integer code, String msg) {
        super(code, msg);
    }

    /**
     * @param code 业务错误码
     * @param msg  默认文案
     * @param args 国际化占位参数
     */
    public ErrorException(Integer code, String msg, Object[] args) {
        super(code, msg, args);
    }

    /**
     * @param code  业务错误码
     * @param msg   默认文案
     * @param cause 原始异常
     */
    public ErrorException(Integer code, String msg, Throwable cause) {
        super(code, msg, cause);
    }

    /**
     * @param code  业务错误码
     * @param msg   默认文案
     * @param args  国际化占位参数
     * @param cause 原始异常
     */
    public ErrorException(Integer code, String msg, Object[] args, Throwable cause) {
        super(code, msg, args, cause);
    }
}
