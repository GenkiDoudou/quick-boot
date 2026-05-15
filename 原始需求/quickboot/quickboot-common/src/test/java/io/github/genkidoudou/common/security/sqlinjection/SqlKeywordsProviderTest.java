package io.github.genkidoudou.common.security.sqlinjection;

import io.github.genkidoudou.common.firewall.sqlinjection.SqlInjectionProperties;
import io.github.genkidoudou.common.firewall.sqlinjection.SqlKeywordsProvider;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * SqlKeywordsProvider SQL关键字提供者测试类
 *
 * @author genkidoudou
 */
class SqlKeywordsProviderTest {

    @Test
    void testGetDefaultKeywords() {
        SqlInjectionProperties properties = new SqlInjectionProperties();
        SqlKeywordsProvider provider = new SqlKeywordsProvider(properties);
        
        List<String> keywords = provider.getKeywords();
        
        assertNotNull(keywords);
        assertFalse(keywords.isEmpty());
        assertTrue(keywords.contains("select"));
        assertTrue(keywords.contains("union"));
        assertTrue(keywords.contains("drop"));
        assertTrue(keywords.contains("'"));
        assertTrue(keywords.contains("--"));
    }

    @Test
    void testGetCustomKeywords() {
        SqlInjectionProperties properties = new SqlInjectionProperties();
        List<String> customKeywords = Arrays.asList("custom1", "custom2", "custom3");
        properties.setKeywords(customKeywords);
        
        SqlKeywordsProvider provider = new SqlKeywordsProvider(properties);
        List<String> keywords = provider.getKeywords();
        
        assertEquals(3, keywords.size());
        assertTrue(keywords.contains("custom1"));
        assertTrue(keywords.contains("custom2"));
        assertTrue(keywords.contains("custom3"));
    }

    @Test
    void testDefaultKeywordsContainCommonSqlInjectionPatterns() {
        SqlInjectionProperties properties = new SqlInjectionProperties();
        SqlKeywordsProvider provider = new SqlKeywordsProvider(properties);
        
        List<String> keywords = provider.getKeywords();
        
        // SQL语句关键字
        assertTrue(keywords.contains("select"));
        assertTrue(keywords.contains("insert"));
        assertTrue(keywords.contains("update"));
        assertTrue(keywords.contains("delete"));
        assertTrue(keywords.contains("drop"));
        assertTrue(keywords.contains("create"));
        assertTrue(keywords.contains("alter"));
        
        // 特殊字符
        assertTrue(keywords.contains("'"));
        assertTrue(keywords.contains("\""));
        assertTrue(keywords.contains(";"));
        assertTrue(keywords.contains("--"));
        
        // 函数和存储过程
        assertTrue(keywords.contains("exec"));
        assertTrue(keywords.contains("execute"));
        assertTrue(keywords.contains("xp_cmdshell"));
    }

    @Test
    void testEmptyCustomKeywordsReturnsDefault() {
        SqlInjectionProperties properties = new SqlInjectionProperties();
        properties.setKeywords(Arrays.asList());
        
        SqlKeywordsProvider provider = new SqlKeywordsProvider(properties);
        List<String> keywords = provider.getKeywords();
        
        assertFalse(keywords.isEmpty());
        assertTrue(keywords.contains("select"));
    }

    @Test
    void testNullCustomKeywordsReturnsDefault() {
        SqlInjectionProperties properties = new SqlInjectionProperties();
        properties.setKeywords(null);
        
        SqlKeywordsProvider provider = new SqlKeywordsProvider(properties);
        List<String> keywords = provider.getKeywords();
        
        assertFalse(keywords.isEmpty());
        assertTrue(keywords.contains("select"));
    }
}
