package io.github.genkidoudou.common.desensitization;

/**
 * 字符串掩码算法：供 Jackson 序列化使用，{@code input} 本身不会被改写（返回新字符串）。
 * <p>
 * 规则与 OpenSpec {@code common-field-desensitization} 对齐：{@code null}、空串、长度不足以脱敏、
 * {@link SensitiveType#CUSTOM} 非法 {@code strategy} → 返回原串。
 */
public final class SensitiveMasking {

    private static final String MASK = "*";
    private static final String PASSWORD_MASK = "******";

    private SensitiveMasking() {
    }

    /**
     * @param input    原始值（可为 {@code null}）
     * @param type     脱敏类型
     * @param strategy 仅在 {@link SensitiveType#CUSTOM} 下读取，形如 {@code "3,4"}
     */
    public static String mask(String input, SensitiveType type, String strategy) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        switch (type) {
            case NAME:
                return maskName(input);
            case ID_CARD:
                return maskIdCard(input);
            case MOBILE:
                return maskMobile(input);
            case BANK_CARD:
                return maskBankCard(input);
            case EMAIL:
                return maskEmail(input);
            case ADDRESS:
                return maskAddress(input);
            case PASSWORD:
                return maskPassword(input);
            case CUSTOM:
                return maskCustom(input, strategy);
            default:
                return input;
        }
    }

    /** 静态入口：从注解读取 type/strategy（仅序列化调用）。 */
    public static String mask(String input, Sensitive ann) {
        if (ann == null) {
            return input;
        }
        return mask(input, ann.type(), ann.strategy());
    }

    /**
     * 单字不脱敏（长度 ≤ 1 原样）；否则首字符 + 其余每位 {@code *}。
     */
    static String maskName(String v) {
        int len = v.length();
        if (len <= 1) {
            return v;
        }
        return v.charAt(0) + MASK.repeat(len - 1);
    }

    /**
     * 至少 10 位（前 6 + 后 4）；否则原样。
     */
    static String maskIdCard(String v) {
        int len = v.length();
        if (len < 10) {
            return v;
        }
        int mid = len - 10;
        return v.substring(0, 6) + MASK.repeat(mid) + v.substring(len - 4);
    }

    /**
     * 至少 7 位（前 3 + 后 4）；否则原样。
     */
    static String maskMobile(String v) {
        int len = v.length();
        if (len < 7) {
            return v;
        }
        int mid = len - 7;
        return v.substring(0, 3) + MASK.repeat(mid) + v.substring(len - 4);
    }

    /**
     * 至少 8 位（前 4 + 后 4）；否则原样。
     */
    static String maskBankCard(String v) {
        int len = v.length();
        if (len < 8) {
            return v;
        }
        int mid = len - 8;
        return v.substring(0, 4) + MASK.repeat(mid) + v.substring(len - 4);
    }

    /**
     * 须含 {@code @}；本地段 ≤2 原样；否则本地前 2 + 本地余下等长 {@code *}；域名全文保留。
     */
    static String maskEmail(String v) {
        int at = v.lastIndexOf('@');
        if (at <= 0 || at >= v.length() - 1) {
            return v;
        }
        String local = v.substring(0, at);
        String domain = v.substring(at + 1);
        if (local.length() <= 2) {
            return v;
        }
        return local.substring(0, 2) + MASK.repeat(local.length() - 2) + "@" + domain;
    }

    /**
     * 长度 ≤ 6 原样；否则前 6 位保留，后缀每位 {@code *}（等长于剩余段）。
     */
    static String maskAddress(String v) {
        int len = v.length();
        if (len <= 6) {
            return v;
        }
        return v.substring(0, 6) + MASK.repeat(len - 6);
    }

    /** 非空非 null 入口处已过滤；此处恒为 {@value #PASSWORD_MASK}。 */
    static String maskPassword(@SuppressWarnings("unused") String v) {
        return PASSWORD_MASK;
    }

    /**
     * {@code prefix,suffix} 非负整数；{@code prefix+suffix >= length} 或解析失败 → 原样。
     */
    static String maskCustom(String v, String strategy) {
        if (strategy == null || strategy.isBlank()) {
            return v;
        }
        String[] parts = strategy.trim().split("\\s*,\\s*");
        if (parts.length != 2) {
            return v;
        }
        int prefix;
        int suffix;
        try {
            prefix = Integer.parseInt(parts[0]);
            suffix = Integer.parseInt(parts[1]);
        } catch (NumberFormatException e) {
            return v;
        }
        if (prefix < 0 || suffix < 0) {
            return v;
        }
        int len = v.length();
        if (prefix + suffix >= len) {
            return v;
        }
        int midLen = len - prefix - suffix;
        return v.substring(0, prefix) + MASK.repeat(midLen) + v.substring(len - suffix);
    }
}
