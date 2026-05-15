package io.github.genkidoudou.common.security.sqlinjection;

import io.github.genkidoudou.common.firewall.sqlinjection.SqlInjectionUtils;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * SqlInjectionUtils SQL注入检测工具测试类
 *
 * @author genkidoudou
 */
class SqlInjectionUtilsTest {

    @Test
    void testDetectWithSqlKeywords() {
        List<String> keywords = Arrays.asList("select", "union", "drop");
        
        List<String> hits1 = SqlInjectionUtils.detect("select * from users", keywords);
        assertEquals(1, hits1.size());
        assertTrue(hits1.contains("select"));
        
        List<String> hits2 = SqlInjectionUtils.detect("1' union select * from users--", keywords);
        assertEquals(2, hits2.size());
        assertTrue(hits2.contains("union"));
        assertTrue(hits2.contains("select"));
    }

    @Test
    void testDetectWithSpecialChars() {
        List<String> keywords = Arrays.asList("'", "\"", ";", "--");
        
        List<String> hits1 = SqlInjectionUtils.detect("admin' or '1'='1", keywords);
        assertTrue(hits1.contains("'"));
        
        List<String> hits2 = SqlInjectionUtils.detect("test; drop table users;", keywords);
        assertTrue(hits2.contains(";"));
    }

    @Test
    void testDetectWithNormalString() {
        List<String> keywords = Arrays.asList("select", "union", "drop");
        
        List<String> hits = SqlInjectionUtils.detect("normal user input", keywords);
        assertTrue(hits.isEmpty());
    }

    @Test
    void testDetectWithNullValue() {
        List<String> keywords = Arrays.asList("select", "union");
        
        List<String> hits = SqlInjectionUtils.detect(null, keywords);
        assertTrue(hits.isEmpty());
    }

    @Test
    void testDetectWithEmptyValue() {
        List<String> keywords = Arrays.asList("select", "union");
        
        List<String> hits = SqlInjectionUtils.detect("", keywords);
        assertTrue(hits.isEmpty());
    }

    @Test
    void testDetectWithNullKeywords() {
        List<String> hits = SqlInjectionUtils.detect("select * from users", null);
        assertTrue(hits.isEmpty());
    }

    @Test
    void testDetectWithEmptyKeywords() {
        List<String> hits = SqlInjectionUtils.detect("select * from users", Collections.emptyList());
        assertTrue(hits.isEmpty());
    }

    @Test
    void testDetectCaseInsensitive() {
        List<String> keywords = Arrays.asList("select", "union");
        
        List<String> hits1 = SqlInjectionUtils.detect("SELECT * FROM users", keywords);
        assertEquals(1, hits1.size());
        assertTrue(hits1.contains("select"));
        
        List<String> hits2 = SqlInjectionUtils.detect("UnIoN SeLeCt", keywords);
        assertEquals(2, hits2.size());
    }

    @Test
    void testDetectWithWordBoundary() {
        List<String> keywords = Arrays.asList("or", "and");
        
        // 应该检测到 "or"
        List<String> hits1 = SqlInjectionUtils.detect("1 or 1=1", keywords);
        assertTrue(hits1.contains("or"));
        
        // 不应该检测到 "or"（因为是单词的一部分）
        List<String> hits2 = SqlInjectionUtils.detect("order by id", keywords);
        assertFalse(hits2.contains("or"));
    }

    @Test
    void testDetectWithMultipleKeywords() {
        List<String> keywords = Arrays.asList("select", "from", "where", "union", "'", ";");
        
        List<String> hits = SqlInjectionUtils.detect("select * from users where id=1; union select * from admin", keywords);
        assertTrue(hits.contains("select"));
        assertTrue(hits.contains("from"));
        assertTrue(hits.contains("where"));
        assertTrue(hits.contains("union"));
        assertTrue(hits.contains(";"));
    }

    @Test
    void testDetectWithComments() {
        List<String> keywords = Arrays.asList("--", "/*", "*/");
        
        List<String> hits1 = SqlInjectionUtils.detect("admin'--", keywords);
        assertTrue(hits1.contains("--"));
        
        List<String> hits2 = SqlInjectionUtils.detect("/* comment */ select", keywords);
        assertTrue(hits2.contains("/*"));
        assertTrue(hits2.contains("*/"));
    }

    @Test
    void testDetectWithHexValue() {
        List<String> keywords = Arrays.asList("0x");
        
        List<String> hits = SqlInjectionUtils.detect("0x61646d696e", keywords);
        assertTrue(hits.contains("0x"));
    }

    @Test
    void testDetectComplexInjection() {
        List<String> keywords = Arrays.asList("select", "union", "from", "'", "--");
        
        String injection = "admin' union select password from users--";
        List<String> hits = SqlInjectionUtils.detect(injection, keywords);
        
        assertTrue(hits.size() >= 4);
        assertTrue(hits.contains("union"));
        assertTrue(hits.contains("select"));
        assertTrue(hits.contains("from"));
        assertTrue(hits.contains("'"));
    }
}
