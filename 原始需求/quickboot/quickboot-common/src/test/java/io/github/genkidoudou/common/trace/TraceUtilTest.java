package io.github.genkidoudou.common.trace;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

import static org.junit.jupiter.api.Assertions.*;

/**
 * TraceUtil 链路追踪工具测试类
 *
 * @author genkidoudou
 */
class TraceUtilTest {

    @AfterEach
    void tearDown() {
        MDC.clear();
    }

    @Test
    void testGetTraceIdWhenExists() {
        String expectedTraceId = "test-trace-id-123";
        MDC.put("traceId", expectedTraceId);
        
        String actualTraceId = TraceUtil.getTraceId();
        
        assertEquals(expectedTraceId, actualTraceId);
    }

    @Test
    void testGetTraceIdWhenNotExists() {
        MDC.clear();
        
        String traceId = TraceUtil.getTraceId();
        
        assertNull(traceId);
    }

    @Test
    void testGetTraceIdAfterSet() {
        assertNull(TraceUtil.getTraceId());
        
        MDC.put("traceId", "new-trace-id");
        
        assertEquals("new-trace-id", TraceUtil.getTraceId());
    }

    @Test
    void testGetTraceIdAfterClear() {
        MDC.put("traceId", "trace-id");
        assertEquals("trace-id", TraceUtil.getTraceId());
        
        MDC.clear();
        
        assertNull(TraceUtil.getTraceId());
    }

    @Test
    void testGetTraceIdWithEmptyString() {
        MDC.put("traceId", "");
        
        String traceId = TraceUtil.getTraceId();
        
        assertEquals("", traceId);
    }
}
