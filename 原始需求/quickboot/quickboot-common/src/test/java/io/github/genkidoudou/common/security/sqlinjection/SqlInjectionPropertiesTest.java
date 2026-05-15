package io.github.genkidoudou.common.security.sqlinjection;

import io.github.genkidoudou.common.firewall.sqlinjection.SqlInjectionProperties;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * SqlInjectionProperties SQL注入拦截配置属性测试类
 *
 * @author genkidoudou
 */
class SqlInjectionPropertiesTest {

    @Test
    void testDefaultValues() {
        SqlInjectionProperties properties = new SqlInjectionProperties();
        
        assertFalse(properties.getEnabled());
        assertNotNull(properties.getIgnoreUrls());
        assertTrue(properties.getIgnoreUrls().isEmpty());
        assertNotNull(properties.getKeywords());
        assertTrue(properties.getKeywords().isEmpty());
    }

    @Test
    void testSetEnabled() {
        SqlInjectionProperties properties = new SqlInjectionProperties();
        properties.setEnabled(true);
        assertTrue(properties.getEnabled());
    }

    @Test
    void testSetIgnoreUrls() {
        SqlInjectionProperties properties = new SqlInjectionProperties();
        List<String> ignoreUrls = Arrays.asList("/api/public/**", "/health");
        properties.setIgnoreUrls(ignoreUrls);
        
        assertEquals(2, properties.getIgnoreUrls().size());
        assertTrue(properties.getIgnoreUrls().contains("/api/public/**"));
        assertTrue(properties.getIgnoreUrls().contains("/health"));
    }

    @Test
    void testSetKeywords() {
        SqlInjectionProperties properties = new SqlInjectionProperties();
        List<String> keywords = Arrays.asList("select", "union", "drop");
        properties.setKeywords(keywords);
        
        assertEquals(3, properties.getKeywords().size());
        assertTrue(properties.getKeywords().contains("select"));
        assertTrue(properties.getKeywords().contains("union"));
        assertTrue(properties.getKeywords().contains("drop"));
    }

    @Test
    void testSetMultipleIgnoreUrls() {
        SqlInjectionProperties properties = new SqlInjectionProperties();
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
    void testSetCustomKeywords() {
        SqlInjectionProperties properties = new SqlInjectionProperties();
        List<String> customKeywords = Arrays.asList("custom1", "custom2");
        properties.setKeywords(customKeywords);
        
        assertEquals(2, properties.getKeywords().size());
        assertTrue(properties.getKeywords().contains("custom1"));
        assertTrue(properties.getKeywords().contains("custom2"));
    }
}
