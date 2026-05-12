package com.su60.quickboot.common.sensitive;

/**
 * 敏感词处理策略。
 */
public enum SensitiveWordStrategy {
    /**
     * 直接替换为 *
     */
    REPLACE,
    /**
     * 发现即抛异常
     */
    THROW
}
