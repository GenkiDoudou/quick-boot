package io.github.genkidoudou.common.firewall.xss;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * XSS 脚本注入检测工具类
 *
 * @author genkidoudou
 * @since 2026/03/06
 */
public class XssUtils {

    /**
     * 默认 XSS 检测模式（不区分大小写）
     *
     * @since 2026/03/06
     */
    private static final List<Pattern> DEFAULT_PATTERNS = List.of(
            // script 标签
            Pattern.compile("<script[^>]*>", Pattern.CASE_INSENSITIVE),
            Pattern.compile("</script>", Pattern.CASE_INSENSITIVE),
            // javascript/vbscript 协议
            Pattern.compile("javascript\\s*:", Pattern.CASE_INSENSITIVE),
            Pattern.compile("vbscript\\s*:", Pattern.CASE_INSENSITIVE),
            // 事件处理器
            Pattern.compile("on\\w+\\s*=", Pattern.CASE_INSENSITIVE),
            // 危险标签
            Pattern.compile("<iframe[^>]*", Pattern.CASE_INSENSITIVE),
            Pattern.compile("<object[^>]*", Pattern.CASE_INSENSITIVE),
            Pattern.compile("<embed[^>]*", Pattern.CASE_INSENSITIVE),
            Pattern.compile("<form[^>]*", Pattern.CASE_INSENSITIVE),
            Pattern.compile("<svg[^>]*", Pattern.CASE_INSENSITIVE),
            Pattern.compile("<marquee[^>]*", Pattern.CASE_INSENSITIVE),
            // expression (CSS)
            Pattern.compile("expression\\s*\\(", Pattern.CASE_INSENSITIVE),
            // data URI (text/html)
            Pattern.compile("data\\s*:\\s*text/html", Pattern.CASE_INSENSITIVE),
            // eval
            Pattern.compile("eval\\s*\\(", Pattern.CASE_INSENSITIVE),
            // document/window/alert
            Pattern.compile("document\\.", Pattern.CASE_INSENSITIVE),
            Pattern.compile("window\\.", Pattern.CASE_INSENSITIVE),
            Pattern.compile("alert\\s*\\(", Pattern.CASE_INSENSITIVE)
    );

    /**
     * 检测字符串中是否包含 XSS 脚本
     *
     * @param value        待检测字符串
     * @param customPatterns 自定义正则模式，可为空
     * @return 检测到的模式描述列表，无则返回空列表
     * @since 2026/03/06
     */
    public static List<String> detect(String value, List<String> customPatterns) {
        List<String> hits = new ArrayList<>();
        if (value == null || value.isEmpty()) {
            return hits;
        }

        // 检测默认模式
        for (Pattern pattern : DEFAULT_PATTERNS) {
            Matcher matcher = pattern.matcher(value);
            if (matcher.find()) {
                hits.add(pattern.pattern());
            }
        }

        // 检测自定义模式
        if (customPatterns != null && !customPatterns.isEmpty()) {
            for (String patternStr : customPatterns) {
                try {
                    Pattern pattern = Pattern.compile(patternStr, Pattern.CASE_INSENSITIVE);
                    Matcher matcher = pattern.matcher(value);
                    if (matcher.find()) {
                        hits.add("custom:" + patternStr);
                    }
                } catch (Exception e) {
                    // 忽略无效正则
                }
            }
        }

        return hits;
    }

    /**
     * 使用默认模式检测
     *
     * @param value 待检测字符串
     * @return 检测到的模式列表
     * @since 2026/03/06
     */
    public static List<String> detect(String value) {
        return detect(value, null);
    }
}
