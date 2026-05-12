package com.su60.quickboot.common.sensitive;

/**
 * 敏感词命中异常。
 */
public class SensitiveWordException extends RuntimeException {

    public SensitiveWordException(String message) {
        super(message);
    }
}
