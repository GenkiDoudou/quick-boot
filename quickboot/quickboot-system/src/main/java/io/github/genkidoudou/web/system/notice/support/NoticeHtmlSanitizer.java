package io.github.genkidoudou.web.system.notice.support;

import org.owasp.html.PolicyFactory;
import org.owasp.html.Sanitizers;

/**
 * 通知公告正文 HTML 白名单消毒；在持久化前调用。
 */
public final class NoticeHtmlSanitizer {

    private static final PolicyFactory POLICY = Sanitizers.FORMATTING
        .and(Sanitizers.LINKS)
        .and(Sanitizers.BLOCKS)
        .and(Sanitizers.IMAGES);

    private NoticeHtmlSanitizer() {
    }

    /**
     * 对 HTML 片段做白名单消毒。
     *
     * @param html 原始 HTML，可为 null 或空白
     * @return 消毒后字符串；输入为 null 时返回 null；输入为空白时返回 null
     */
    public static String sanitize(String html) {
        if (html == null) {
            return null;
        }
        String trimmed = html.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        return POLICY.sanitize(trimmed);
    }
}
