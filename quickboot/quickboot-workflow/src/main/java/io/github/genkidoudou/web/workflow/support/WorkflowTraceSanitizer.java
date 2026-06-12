package io.github.genkidoudou.web.workflow.support;

import cn.hutool.core.util.StrUtil;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * 工作流 Trace 脱敏器：Authorization、Cookie、手机号等敏感字段掩码后再落库。
 */
@Component
public class WorkflowTraceSanitizer {

    private static final Set<String> SENSITIVE_KEYS = Set.of(
        "authorization", "cookie", "set-cookie", "password", "secret", "token", "apikey", "api-key"
    );

    private static final Pattern PHONE_PATTERN = Pattern.compile("1[3-9]\\d{9}");

    /**
     * 脱敏 Map 结构（浅层 + 一层嵌套）。
     *
     * @param source 原始数据
     * @return 脱敏后的新 Map
     */
    public Map<String, Object> sanitizeMap(Map<String, Object> source) {
        if (source == null || source.isEmpty()) {
            return Map.of();
        }
        Map<String, Object> result = new HashMap<>();
        for (Map.Entry<String, Object> entry : source.entrySet()) {
            result.put(entry.getKey(), sanitizeValue(entry.getKey(), entry.getValue()));
        }
        return result;
    }

    /**
     * 脱敏 JSON 字符串中的敏感片段（简单替换）。
     *
     * @param json JSON 文本
     * @return 脱敏文本
     */
    public String sanitizeJson(String json) {
        if (StrUtil.isBlank(json)) {
            return json;
        }
        String masked = json;
        masked = masked.replaceAll("(?i)(\"authorization\"\\s*:\\s*\")([^\"]+)(\")", "$1***$3");
        masked = masked.replaceAll("(?i)(\"cookie\"\\s*:\\s*\")([^\"]+)(\")", "$1***$3");
        masked = PHONE_PATTERN.matcher(masked).replaceAll("1**********");
        return masked;
    }

    @SuppressWarnings("unchecked")
    private Object sanitizeValue(String key, Object value) {
        if (key != null && SENSITIVE_KEYS.contains(key.toLowerCase())) {
            return "***";
        }
        if (value instanceof String str) {
            return PHONE_PATTERN.matcher(str).replaceAll("1**********");
        }
        if (value instanceof Map<?, ?> map) {
            Map<String, Object> nested = new HashMap<>();
            for (Map.Entry<?, ?> e : map.entrySet()) {
                String nestedKey = String.valueOf(e.getKey());
                nested.put(nestedKey, sanitizeValue(nestedKey, e.getValue()));
            }
            return nested;
        }
        if (value instanceof List<?> list) {
            List<Object> sanitized = new ArrayList<>(list.size());
            for (Object item : list) {
                sanitized.add(item instanceof Map<?, ?> m ? sanitizeMap((Map<String, Object>) m) : item);
            }
            return sanitized;
        }
        return value;
    }
}
