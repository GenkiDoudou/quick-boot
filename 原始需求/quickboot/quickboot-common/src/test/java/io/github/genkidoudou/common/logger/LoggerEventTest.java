package io.github.genkidoudou.common.logger;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * LoggerEvent 日志事件测试类
 *
 * @author genkidoudou
 */
class LoggerEventTest {

    @Test
    void testConstructor() {
        LoggerEventDto dto = new LoggerEventDto();
        LoggerEvent event = new LoggerEvent(dto);
        
        assertNotNull(event);
        assertEquals(dto, event.getSource());
    }

    @Test
    void testGetSource() {
        LoggerEventDto dto = new LoggerEventDto()
                .setTraceId("test-trace-id")
                .setStartTime(System.currentTimeMillis());
        
        LoggerEvent event = new LoggerEvent(dto);
        
        assertTrue(event.getSource() instanceof LoggerEventDto);
        LoggerEventDto source = (LoggerEventDto) event.getSource();
        assertEquals("test-trace-id", source.getTraceId());
    }

    @Test
    void testWithNullSource() {
        LoggerEvent event = new LoggerEvent(null);
        assertNotNull(event);
        assertNull(event.getSource());
    }

    @Test
    void testWithStringSource() {
        String source = "test source";
        LoggerEvent event = new LoggerEvent(source);
        
        assertNotNull(event);
        assertEquals(source, event.getSource());
    }
}
