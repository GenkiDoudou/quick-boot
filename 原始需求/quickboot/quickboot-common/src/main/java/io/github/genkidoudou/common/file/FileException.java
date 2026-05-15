package io.github.genkidoudou.common.file;

import io.github.genkidoudou.common.exception.BaseException;
import io.github.genkidoudou.common.exception.ErrorCode;

/**
 * 文件操作异常
 *
 * @author genkidoudou
 * @since 2026/03/11
 */
public class FileException extends BaseException {

    private static final long serialVersionUID = 1L;

    public FileException(String msg) {
        super(ErrorCode.FILE_ERROR, msg);
    }

    public FileException(Integer code, String msg) {
        super(code, msg);
    }

    public FileException(String msg, Throwable cause) {
        super(ErrorCode.FILE_ERROR, msg, cause);
    }

    public FileException(Integer code, String msg, Throwable cause) {
        super(code, msg, cause);
    }
}
