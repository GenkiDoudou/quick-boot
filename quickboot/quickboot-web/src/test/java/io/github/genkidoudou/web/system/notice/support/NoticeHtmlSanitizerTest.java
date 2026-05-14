package io.github.genkidoudou.web.system.notice.support;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link NoticeHtmlSanitizer} 行为用例：剔除危险标签、保留安全结构。
 */
class NoticeHtmlSanitizerTest {

    @Test
    void stripsScriptTag() {
        String raw = "<p>Hi</p><script>alert(1)</script><b>x</b>";
        String safe = NoticeHtmlSanitizer.sanitize(raw);
        assertNotNull(safe);
        assertFalse(safe.toLowerCase().contains("<script"));
        assertTrue(safe.contains("x"));
    }

    @Test
    void nullAndBlankReturnNull() {
        assertNull(NoticeHtmlSanitizer.sanitize(null));
        assertNull(NoticeHtmlSanitizer.sanitize("   "));
    }
}
