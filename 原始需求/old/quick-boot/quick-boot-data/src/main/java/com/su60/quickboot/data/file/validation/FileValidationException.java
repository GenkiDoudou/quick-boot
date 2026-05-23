package com.su60.quickboot.data.file.validation;

/**
 * 文件校验异常
 *
 * @author luyanan
 * @since 2025/01/07
 */
public class FileValidationException extends RuntimeException {

    public FileValidationException(String message) {
        super(message);
    }

    public FileValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
