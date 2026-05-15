package io.github.genkidoudou.common.exception;

/**
 * 影响程序的异常
 * 用于表示严重错误,会影响程序正常运行
 *
 * @author genkidoudou
 */
public class ErrorException extends BaseException {

    private static final long serialVersionUID = 1L;

    public ErrorException() {
        super();
    }

    public ErrorException(String msg) {
        super(msg);
    }

    public ErrorException(Integer code) {
        super(code);
    }

    public ErrorException(Integer code, String msg) {
        super(code, msg);
    }

    public ErrorException(Integer code, String msg, Object... args) {
        super(code, msg, args);
    }

    public ErrorException(String msg, Throwable cause) {
        super(msg, cause);
    }

    public ErrorException(Integer code, String msg, Throwable cause) {
        super(code, msg, cause);
    }

}
