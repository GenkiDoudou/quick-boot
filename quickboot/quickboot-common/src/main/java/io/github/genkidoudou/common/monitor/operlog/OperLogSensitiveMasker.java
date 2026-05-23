package io.github.genkidoudou.common.monitor.operlog;

import cn.hutool.core.util.StrUtil;

import java.util.regex.Pattern;

/**
 * 操作日志入库前字符串脱敏：对常见敏感键的 JSON 片段做掩码。
 */
public final class OperLogSensitiveMasker {

    private static final Pattern[] JSON_KEY_PATTERNS = new Pattern[] {
        Pattern.compile("(?i)(\"password\"\\s*:\\s*)\"[^\"]*\""),
        Pattern.compile("(?i)(\"oldPassword\"\\s*:\\s*)\"[^\"]*\""),
        Pattern.compile("(?i)(\"newPassword\"\\s*:\\s*)\"[^\"]*\""),
        Pattern.compile("(?i)(\"confirmPassword\"\\s*:\\s*)\"[^\"]*\""),
        Pattern.compile("(?i)(\"access_token\"\\s*:\\s*)\"[^\"]*\""),
        Pattern.compile("(?i)(\"refresh_token\"\\s*:\\s*)\"[^\"]*\""),
        Pattern.compile("(?i)(\"token\"\\s*:\\s*)\"[^\"]*\""),
    };

    private OperLogSensitiveMasker() {
    }

    /**
     * @param text 可能为 JSON 或任意文本；{@code null} 安全
     * @return 掩码后的文本；{@code null} 入参返回 {@code null}
     */
    public static String mask(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        String s = text;
        for (Pattern p : JSON_KEY_PATTERNS) {
            s = p.matcher(s).replaceAll("$1\"******\"");
        }
        return maskNonJsonSecrets(s);
    }

    private static String maskNonJsonSecrets(String s) {
        if (StrUtil.isBlank(s)) {
            return s;
        }
        String r = s.replaceAll("(?i)(password=)([^&]*)", "$1******");
        return r.replaceAll("(?i)(token=)([^&]*)", "$1******");
    }
}
