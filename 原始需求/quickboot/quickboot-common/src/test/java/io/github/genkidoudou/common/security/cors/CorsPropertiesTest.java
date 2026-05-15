package io.github.genkidoudou.common.security.cors;

import io.github.genkidoudou.common.firewall.cors.CorsProperties;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * CorsProperties 跨域配置属性测试类
 *
 * @author genkidoudou
 */
class CorsPropertiesTest {

    @Test
    void testDefaultValues() {
        CorsProperties properties = new CorsProperties();
        
        assertFalse(properties.getEnabled());
        assertNotNull(properties.getAllowedOrigins());
        assertTrue(properties.getAllowedOrigins().isEmpty());
        
        assertEquals(5, properties.getAllowedMethods().size());
        assertTrue(properties.getAllowedMethods().contains("GET"));
        assertTrue(properties.getAllowedMethods().contains("POST"));
        assertTrue(properties.getAllowedMethods().contains("PUT"));
        assertTrue(properties.getAllowedMethods().contains("DELETE"));
        assertTrue(properties.getAllowedMethods().contains("OPTIONS"));
        
        assertEquals(1, properties.getAllowedHeaders().size());
        assertEquals("*", properties.getAllowedHeaders().get(0));
        
        assertNotNull(properties.getExposedHeaders());
        assertTrue(properties.getExposedHeaders().isEmpty());
        
        assertTrue(properties.getAllowCredentials());
        assertEquals(3600L, properties.getMaxAge());
        assertEquals("/**", properties.getPathPattern());
    }

    @Test
    void testSetEnabled() {
        CorsProperties properties = new CorsProperties();
        properties.setEnabled(true);
        assertTrue(properties.getEnabled());
    }

    @Test
    void testSetAllowedOrigins() {
        CorsProperties properties = new CorsProperties();
        List<String> origins = Arrays.asList("http://localhost:3000", "https://example.com");
        properties.setAllowedOrigins(origins);
        
        assertEquals(2, properties.getAllowedOrigins().size());
        assertTrue(properties.getAllowedOrigins().contains("http://localhost:3000"));
        assertTrue(properties.getAllowedOrigins().contains("https://example.com"));
    }

    @Test
    void testSetAllowedMethods() {
        CorsProperties properties = new CorsProperties();
        List<String> methods = Arrays.asList("GET", "POST");
        properties.setAllowedMethods(methods);
        
        assertEquals(2, properties.getAllowedMethods().size());
        assertTrue(properties.getAllowedMethods().contains("GET"));
        assertTrue(properties.getAllowedMethods().contains("POST"));
    }

    @Test
    void testSetAllowedHeaders() {
        CorsProperties properties = new CorsProperties();
        List<String> headers = Arrays.asList("Content-Type", "Authorization");
        properties.setAllowedHeaders(headers);
        
        assertEquals(2, properties.getAllowedHeaders().size());
        assertTrue(properties.getAllowedHeaders().contains("Content-Type"));
        assertTrue(properties.getAllowedHeaders().contains("Authorization"));
    }

    @Test
    void testSetExposedHeaders() {
        CorsProperties properties = new CorsProperties();
        List<String> exposedHeaders = Arrays.asList("X-Total-Count", "X-Page-Number");
        properties.setExposedHeaders(exposedHeaders);
        
        assertEquals(2, properties.getExposedHeaders().size());
        assertTrue(properties.getExposedHeaders().contains("X-Total-Count"));
        assertTrue(properties.getExposedHeaders().contains("X-Page-Number"));
    }

    @Test
    void testSetAllowCredentials() {
        CorsProperties properties = new CorsProperties();
        properties.setAllowCredentials(false);
        assertFalse(properties.getAllowCredentials());
    }

    @Test
    void testSetMaxAge() {
        CorsProperties properties = new CorsProperties();
        properties.setMaxAge(7200L);
        assertEquals(7200L, properties.getMaxAge());
    }

    @Test
    void testSetPathPattern() {
        CorsProperties properties = new CorsProperties();
        properties.setPathPattern("/api/**");
        assertEquals("/api/**", properties.getPathPattern());
    }
}
