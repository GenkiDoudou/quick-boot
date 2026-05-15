package io.github.genkidoudou.common.firewall.sqlinjection;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * SQL注入检测工具类
 *
 * @author genkidoudou
 * @since 2026/03/05
 */
public class SqlInjectionUtils {

    /**
     * 检测字符串中是否包含SQL注入关键字
     *
     * @param value    待检测字符串
     * @param keywords SQL关键字列表
     * @return 检测到的关键字列表，如果没有检测到返回空列表
     * @since 2026/03/05
     */
    public static List<String> detect(String value, List<String> keywords) {
        List<String> hits = new ArrayList<>();
        if (value == null || value.isEmpty() || keywords == null || keywords.isEmpty()) {
            return hits;
        }

        String lowerValue = value.toLowerCase();

        for (String keyword : keywords) {
            String lowerKeyword = keyword.toLowerCase();

            // 特殊字符直接匹配
            if (isSpecialChar(lowerKeyword)) {
                if (lowerValue.contains(lowerKeyword)) {
                    hits.add(keyword);
                }
                continue;
            }

            // SQL关键字需要使用单词边界匹配
            Pattern pattern = Pattern.compile("\\b" + Pattern.quote(lowerKeyword) + "\\b");
            Matcher matcher = pattern.matcher(lowerValue);
            if (matcher.find()) {
                hits.add(keyword);
            }
        }

        return hits;
    }

    /**
     * 判断是否为特殊字符
     *
     * @param keyword 关键字
     * @return 是否为特殊字符
     * @since 2026/03/05
     */
    private static boolean isSpecialChar(String keyword) {
        return keyword.equals("'") || keyword.equals("\"") || keyword.equals(";")
                || keyword.equals("--") || keyword.equals("/*") || keyword.equals("*/")
                || keyword.equals("0x");
    }
}
