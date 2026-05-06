package io.github.genkidoudou.common.security.firewall.sensitiveword;

import io.github.genkidoudou.common.api.HttpCodes;

/**
 * 敏感词校验失败时抛出，供 Filter 捕获并写出业务码 {@link HttpCodes#SENSITIVE_WORD}。
 */
public class SensitiveWordException extends RuntimeException {

    private final String hitWord;

    /**
     * @param hitWord 命中的敏感词（houbb 返回的首词，可能为空串）
     */
    public SensitiveWordException(String hitWord) {
        super(hitWord == null ? "" : hitWord);
        this.hitWord = hitWord == null ? "" : hitWord;
    }

    public String getHitWord() {
        return hitWord;
    }

    public int getBizCode() {
        return HttpCodes.SENSITIVE_WORD;
    }
}
