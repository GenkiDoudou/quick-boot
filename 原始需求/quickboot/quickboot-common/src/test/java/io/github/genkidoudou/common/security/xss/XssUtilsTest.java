package io.github.genkidoudou.common.security.xss;

import io.github.genkidoudou.common.firewall.xss.XssUtils;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * XssUtils XSS脚本注入检测工具测试类
 *
 * @author genkidoudou
 */
class XssUtilsTest {

    @Test
    void testDetectScriptTag() {
        List<String> hits = XssUtils.detect("<script>alert('xss')</script>");
        assertFalse(hits.isEmpty());
        assertTrue(hits.stream().anyMatch(h -> h.toLowerCase().contains("script")));
    }

    @Test
    void testDetectJavascriptProtocol() {
        List<String> hits = XssUtils.detect("<a href='javascript:alert(1)'>link</a>");
        assertFalse(hits.isEmpty());
        assertTrue(hits.stream().anyMatch(h -> h.toLowerCase().contains("javascript")));
    }

    @Test
    void testDetectEventHandler() {
        List<String> hits = XssUtils.detect("<img src=x onerror=alert(1)>");
        assertFalse(hits.isEmpty());
        assertTrue(hits.stream().anyMatch(h -> h.toLowerCase().contains("on\\\\w+")));
    }

    @Test
    void testDetectIframe() {
        List<String> hits = XssUtils.detect("<iframe src='http://evil.com'></iframe>");
        assertFalse(hits.isEmpty());
        assertTrue(hits.stream().anyMatch(h -> h.toLowerCase().contains("iframe")));
    }

    @Test
    void testDetectObject() {
        List<String> hits = XssUtils.detect("<object data='http://evil.com'></object>");
        assertFalse(hits.isEmpty());
        assertTrue(hits.stream().anyMatch(h -> h.toLowerCase().contains("object")));
    }

    @Test
    void testDetectEmbed() {
        List<String> hits = XssUtils.detect("<embed src='http://evil.com'>");
        assertFalse(hits.isEmpty());
        assertTrue(hits.stream().anyMatch(h -> h.toLowerCase().contains("embed")));
    }

    @Test
    void testDetectSvg() {
        List<String> hits = XssUtils.detect("<svg onload=alert(1)>");
        assertFalse(hits.isEmpty());
    }

    @Test
    void testDetectExpression() {
        List<String> hits = XssUtils.detect("width: expression(alert(1))");
        assertFalse(hits.isEmpty());
        assertTrue(hits.stream().anyMatch(h -> h.toLowerCase().contains("expression")));
    }

    @Test
    void testDetectDataUri() {
        List<String> hits = XssUtils.detect("<a href='data:text/html,<script>alert(1)</script>'>link</a>");
        assertFalse(hits.isEmpty());
        assertTrue(hits.stream().anyMatch(h -> h.toLowerCase().contains("data")));
    }

    @Test
    void testDetectEval() {
        List<String> hits = XssUtils.detect("eval('alert(1)')");
        assertFalse(hits.isEmpty());
        assertTrue(hits.stream().anyMatch(h -> h.toLowerCase().contains("eval")));
    }

    @Test
    void testDetectDocument() {
        List<String> hits = XssUtils.detect("document.cookie");
        assertFalse(hits.isEmpty());
        assertTrue(hits.stream().anyMatch(h -> h.toLowerCase().contains("document")));
    }

    @Test
    void testDetectWindow() {
        List<String> hits = XssUtils.detect("window.location");
        assertFalse(hits.isEmpty());
        assertTrue(hits.stream().anyMatch(h -> h.toLowerCase().contains("window")));
    }

    @Test
    void testDetectAlert() {
        List<String> hits = XssUtils.detect("alert(1)");
        assertFalse(hits.isEmpty());
        assertTrue(hits.stream().anyMatch(h -> h.toLowerCase().contains("alert")));
    }

    @Test
    void testDetectWithNormalString() {
        List<String> hits = XssUtils.detect("This is a normal string");
        assertTrue(hits.isEmpty());
    }

    @Test
    void testDetectWithNullValue() {
        List<String> hits = XssUtils.detect(null);
        assertTrue(hits.isEmpty());
    }

    @Test
    void testDetectWithEmptyValue() {
        List<String> hits = XssUtils.detect("");
        assertTrue(hits.isEmpty());
    }

    @Test
    void testDetectCaseInsensitive() {
        List<String> hits1 = XssUtils.detect("<SCRIPT>alert(1)</SCRIPT>");
        assertFalse(hits1.isEmpty());

        List<String> hits2 = XssUtils.detect("<ScRiPt>alert(1)</ScRiPt>");
        assertFalse(hits2.isEmpty());
    }

    @Test
    void testDetectWithCustomPatterns() {
        List<String> customPatterns = Arrays.asList("badword", "malicious");
        
        List<String> hits = XssUtils.detect("This contains badword", customPatterns);
        assertFalse(hits.isEmpty());
        assertTrue(hits.stream().anyMatch(h -> h.contains("custom:badword")));
    }

    @Test
    void testDetectWithEmptyCustomPatterns() {
        List<String> hits = XssUtils.detect("<script>alert(1)</script>", Collections.emptyList());
        assertFalse(hits.isEmpty());
    }

    @Test
    void testDetectWithInvalidCustomPattern() {
        List<String> customPatterns = Arrays.asList("[invalid(regex");
        
        // 应该忽略无效正则，不抛出异常
        assertDoesNotThrow(() -> {
            XssUtils.detect("test", customPatterns);
        });
    }

    @Test
    void testDetectMultiplePatterns() {
        String malicious = "<script>alert(1)</script><iframe src='evil'></iframe>";
        List<String> hits = XssUtils.detect(malicious);
        
        assertTrue(hits.size() >= 2);
    }

    @Test
    void testDetectVbscript() {
        List<String> hits = XssUtils.detect("<a href='vbscript:msgbox(1)'>link</a>");
        assertFalse(hits.isEmpty());
        assertTrue(hits.stream().anyMatch(h -> h.toLowerCase().contains("vbscript")));
    }

    @Test
    void testDetectForm() {
        List<String> hits = XssUtils.detect("<form action='http://evil.com'>");
        assertFalse(hits.isEmpty());
        assertTrue(hits.stream().anyMatch(h -> h.toLowerCase().contains("form")));
    }

    @Test
    void testDetectMarquee() {
        List<String> hits = XssUtils.detect("<marquee onstart=alert(1)>");
        assertFalse(hits.isEmpty());
    }
}
