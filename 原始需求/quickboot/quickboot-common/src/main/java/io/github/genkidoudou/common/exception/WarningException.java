package io.github.genkidoudou.common.exception;

/**
 * 警告异常
 * 用于表示警告信息,不会严重影响程序运行
 *
 * @author genkidoudou
 */
public class WarningException extends BaseException {

    private static final long serialVersionUID = 1L;

    public WarningException() {
        super();
    }

    public WarningException(String msg) {
        super(msg);
    }

    public WarningException(Integer code) {
        super(code);
    }

    public WarningException(Integer code, String msg) {
        super(code, msg);
    }

    public WarningException(Integer code, String msg, Object[] args) {
        super(code, msg, args);
    }

    public WarningException(String msg, Throwable cause) {
        super(msg, cause);
    }

    public WarningException(Integer code, String msg, Throwable cause) {
        super(code, msg, cause);
    }

    public WarningException(Integer code, String msg, Object[] args, Throwable cause) {
        super(code, msg, args, cause);
    }
}
