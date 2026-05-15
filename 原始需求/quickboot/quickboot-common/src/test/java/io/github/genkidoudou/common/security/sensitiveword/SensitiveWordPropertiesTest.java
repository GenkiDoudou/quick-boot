package io.github.genkidoudou.common.security.sensitiveword;

import io.github.genkidoudou.common.firewall.sensitiveword.SensitiveWordProperties;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * SensitiveWordProperties 敏感词配置属性测试类
 *
 * @author genkidoudou
 */
class SensitiveWordPropertiesTest {

    @Test
    void testDefaultValues() {
        SensitiveWordProperties properties = new SensitiveWordProperties();
        
        assertFalse(properties.getEnable());
        assertNotNull(properties.getWhiteList());
        assertTrue(properties.getWhiteList().isEmpty());
        assertNotNull(properties.getBlackList());
        assertTrue(properties.getBlackList().isEmpty());
        assertNotNull(properties.getIgnoreUrls());
        assertTrue(properties.getIgnoreUrls().isEmpty());
        assertEquals(SensitiveWordProperties.FilterStrategy.REPLACE, properties.getStrategy());
    }

    @Test
    void testSetEnable() {
        SensitiveWordProperties properties = new SensitiveWordProperties();
        properties.setEnable(true);
        assertTrue(properties.getEnable());
    }

    @Test
    void testSetWhiteList() {
        SensitiveWordProperties properties = new SensitiveWordProperties();
        List<String> whiteList = Arrays.asList("classpath:white1.txt", "classpath:white2.txt");
        properties.setWhiteList(whiteList);
        
        assertEquals(2, properties.getWhiteList().size());
        assertTrue(properties.getWhiteList().contains("classpath:white1.txt"));
        assertTrue(properties.getWhiteList().contains("classpath:white2.txt"));
    }

    @Test
    void testSetBlackList() {
        SensitiveWordProperties properties = new SensitiveWordProperties();
        List<String> blackList = Arrays.asList("classpath:black1.txt", "classpath:black2.txt");
        properties.setBlackList(blackList);
        
        assertEquals(2, properties.getBlackList().size());
        assertTrue(properties.getBlackList().contains("classpath:black1.txt"));
        assertTrue(properties.getBlackList().contains("classpath:black2.txt"));
    }

    @Test
    void testSetIgnoreUrls() {
        SensitiveWordProperties properties = new SensitiveWordProperties();
        List<String> ignoreUrls = Arrays.asList("/login", "/api/public/**");
        properties.setIgnoreUrls(ignoreUrls);
        
        assertEquals(2, properties.getIgnoreUrls().size());
        assertTrue(properties.getIgnoreUrls().contains("/login"));
        assertTrue(properties.getIgnoreUrls().contains("/api/public/**"));
    }

    @Test
    void testSetStrategyReplace() {
        SensitiveWordProperties properties = new SensitiveWordProperties();
        properties.setStrategy(SensitiveWordProperties.FilterStrategy.REPLACE);
        assertEquals(SensitiveWordProperties.FilterStrategy.REPLACE, properties.getStrategy());
    }

    @Test
    void testSetStrategyThrow() {
        SensitiveWordProperties properties = new SensitiveWordProperties();
        properties.setStrategy(SensitiveWordProperties.FilterStrategy.THROW);
        assertEquals(SensitiveWordProperties.FilterStrategy.THROW, properties.getStrategy());
    }

    @Test
    void testFilterStrategyValues() {
        SensitiveWordProperties.FilterStrategy[] strategies = SensitiveWordProperties.FilterStrategy.values();
        assertEquals(2, strategies.length);
        assertEquals(SensitiveWordProperties.FilterStrategy.REPLACE, strategies[0]);
        assertEquals(SensitiveWordProperties.FilterStrategy.THROW, strategies[1]);
    }
}
