package com.su60.quickboot.common.security.sql;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * SQL 注入检测工具。
 */
public class SqlInjectUtils {

    private SqlInjectUtils() {
    }

    public static List<String> detect(String value, List<String> keywords) {
        if (StrUtil.isBlank(value)) {
            return new ArrayList<>();
        }
        List<String> list = CollUtil.isEmpty(keywords) ? defaultKeywords() : keywords;
        String lower = value.toLowerCase(Locale.ROOT);
        return list.stream()
                .filter(k -> StrUtil.isNotBlank(k) && lower.contains(k.toLowerCase(Locale.ROOT)))
                .collect(Collectors.toList());
    }

    private static List<String> defaultKeywords() {
        return List.of("select", "insert", "update", "delete", "drop", "create", "alter",
                "exec", "execute", "union", "script", "javascript", "vbscript", "onload",
                "onerror", "onclick", "'", "\"", ";", "--", "/*", "*/", "xp_", "sp_", "declare",
                "cast", "convert");
    }
}
