package io.github.genkidoudou.common.firewall.sensitiveword;

import io.github.genkidoudou.common.exception.ErrorCode;
import io.github.genkidoudou.common.exception.WarningException;

/**
 * 敏感词异常
 * 
 * 当检测到敏感词且策略为 THROW 时抛出此异常
 *
 * @author QuickBoot
 * @since 2026/03/02
 */
public class SensitiveWordException extends WarningException {

    /**
     * 检测到的敏感词
     *
     * @since 2026/03/02
     */
    private final String sensitiveWord;

    /**
     * 构造函数
     *
     * @param sensitiveWord 敏感词
     * @since 2026/03/02
     */
    public SensitiveWordException(String sensitiveWord) {
        super(ErrorCode.SENSITIVE_WORD_FOUND, "检测到敏感词", new Object[]{sensitiveWord});
        this.sensitiveWord = sensitiveWord;
    }

    /**
     * 构造函数
     *
     * @param code 错误码
     * @param msg 异常消息
     * @param sensitiveWord 敏感词
     * @since 2026/03/02
     */
    public SensitiveWordException(Integer code, String msg, String sensitiveWord) {
        super(code, msg, new Object[]{sensitiveWord});
        this.sensitiveWord = sensitiveWord;
    }

    /**
     * 获取敏感词
     *
     * @return 敏感词
     * @since 2026/03/02
     */
    public String getSensitiveWord() {
        return sensitiveWord;
    }

    /**
     * 检测到敏感词
     *
     * @param sensitiveWord 敏感词
     * @return 敏感词异常
     * @since 2026/03/05
     */
    public static SensitiveWordException found(String sensitiveWord) {
        return new SensitiveWordException(sensitiveWord);
    }
}
