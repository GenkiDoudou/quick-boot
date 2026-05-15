package io.github.genkidoudou.common.security.xss;

import io.github.genkidoudou.common.firewall.xss.XssProperties;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * XssProperties XSS配置属性测试类
 *
 * @author genkidoudou
 */
class XssPropertiesTest {

    @Test
    void testDefaultValues() {
        XssProperties properties = new XssProperties();
        
        assertFalse(properties.getEnabled());
        assertNotNull(properties.getIgnoreUrls());
        assertTrue(properties.getIgnoreUrls().isEmpty());
        assertNotNull(properties.getCustomPatterns());
        assertTrue(properties.getCustomPatterns().isEmpty());
    }

    @Test
    void testSetEnabled() {
        XssProperties properties = new XssProperties();
        properties.setEnabled(true);
        assertTrue(properties.getEnabled());
    }

    @Test
    void testSetIgnoreUrls() {
        XssProperties properties = new XssProperties();
        List<String> ignoreUrls = Arrays.asList("/api/public/**", "/health");
        properties.setIgnoreUrls(ignoreUrls);
        
        assertEquals(2, properties.getIgnoreUrls().size());
        assertTrue(properties.getIgnoreUrls().contains("/api/public/**"));
        assertTrue(properties.getIgnoreUrls().contains("/health"));
    }

    @Test
    void testSetCustomPatterns() {
        XssProperties properties = new XssProperties();
        List<String> patterns = Arrays.asList("pattern1", "pattern2");
        properties.setCustomPatterns(patterns);
        
        assertEquals(2, properties.getCustomPatterns().size());
        assertTrue(properties.getCustomPatterns().contains("pattern1"));
        assertTrue(properties.getCustomPatterns().contains("pattern2"));
    }

    @Test
    void testSetMultipleIgnoreUrls() {
        XssProperties properties = new XssProperties();
        List<String> ignoreUrls = Arrays.asList(
            "/api/public/**",
            "/health",
            "/actuator/**",
            "/swagger-ui/**"
        );
        properties.setIgnoreUrls(ignoreUrls);
        
        assertEquals(4, properties.getIgnoreUrls().size());
    }

    @Test
    void testSetMultipleCustomPatterns() {
        XssProperties properties = new XssProperties();
        List<String> patterns = Arrays.asList(
            "<custom>",
            "badword",
            "malicious.*pattern"
        );
        properties.setCustomPatterns(patterns);
        
        assertEquals(3, properties.getCustomPatterns().size());
    }
}
