package io.github.genkidoudou.common.logger;

import org.aspectj.lang.Signature;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * LoggerEventDto 日志事件DTO测试类
 *
 * @author genkidoudou
 */
class LoggerEventDtoTest {

    @Test
    void testSettersAndGetters() {
        LoggerEventDto dto = new LoggerEventDto();
        
        long startTime = System.currentTimeMillis();
        long endTime = startTime + 1000;
        Object[] args = {"arg1", "arg2"};
        Object result = "test result";
        Throwable throwable = new RuntimeException("test error");
        String traceId = "trace-123";
        
        dto.setStartTime(startTime);
        dto.setEndTime(endTime);
        dto.setArgs(args);
        dto.setResult(result);
        dto.setThrowable(throwable);
        dto.setTraceId(traceId);
        
        assertEquals(startTime, dto.getStartTime());
        assertEquals(endTime, dto.getEndTime());
        assertArrayEquals(args, dto.getArgs());
        assertEquals(result, dto.getResult());
        assertEquals(throwable, dto.getThrowable());
        assertEquals(traceId, dto.getTraceId());
    }

    @Test
    void testChainedSetters() {
        long startTime = System.currentTimeMillis();
        long endTime = startTime + 1000;
        Object[] args = {"arg1"};
        
        LoggerEventDto dto = new LoggerEventDto()
                .setStartTime(startTime)
                .setEndTime(endTime)
                .setArgs(args)
                .setResult("result")
                .setTraceId("trace-id");
        
        assertEquals(startTime, dto.getStartTime());
        assertEquals(endTime, dto.getEndTime());
        assertArrayEquals(args, dto.getArgs());
        assertEquals("result", dto.getResult());
        assertEquals("trace-id", dto.getTraceId());
    }

    @Test
    void testWithNullValues() {
        LoggerEventDto dto = new LoggerEventDto()
                .setArgs(null)
                .setResult(null)
                .setThrowable(null)
                .setSignature(null)
                .setTraceId(null);
        
        assertNull(dto.getArgs());
        assertNull(dto.getResult());
        assertNull(dto.getThrowable());
        assertNull(dto.getSignature());
        assertNull(dto.getTraceId());
    }

    @Test
    void testWithException() {
        RuntimeException exception = new RuntimeException("Test exception");
        LoggerEventDto dto = new LoggerEventDto()
                .setThrowable(exception);
        
        assertEquals(exception, dto.getThrowable());
        assertEquals("Test exception", dto.getThrowable().getMessage());
    }
}
