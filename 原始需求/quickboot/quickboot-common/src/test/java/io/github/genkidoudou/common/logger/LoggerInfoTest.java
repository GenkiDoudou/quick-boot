package io.github.genkidoudou.common.logger;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * LoggerInfo 日志信息测试类
 *
 * @author genkidoudou
 */
class LoggerInfoTest {

    @Test
    void testSettersAndGetters() {
        LoggerInfo loggerInfo = new LoggerInfo();
        
        loggerInfo.setMethodName("testMethod");
        loggerInfo.setSourceIp("192.168.1.1");
        loggerInfo.setDescription("测试方法");
        loggerInfo.setUri("/api/test");
        loggerInfo.setMethod("POST");
        loggerInfo.setRequestParams("{\"key\":\"value\"}");
        loggerInfo.setTimeConsuming(100L);
        loggerInfo.setTraceId("trace-123");
        loggerInfo.setResult("{\"code\":200}");
        loggerInfo.setErrorMsg("error message");
        
        assertEquals("testMethod", loggerInfo.getMethodName());
        assertEquals("192.168.1.1", loggerInfo.getSourceIp());
        assertEquals("测试方法", loggerInfo.getDescription());
        assertEquals("/api/test", loggerInfo.getUri());
        assertEquals("POST", loggerInfo.getMethod());
        assertEquals("{\"key\":\"value\"}", loggerInfo.getRequestParams());
        assertEquals(100L, loggerInfo.getTimeConsuming());
        assertEquals("trace-123", loggerInfo.getTraceId());
        assertEquals("{\"code\":200}", loggerInfo.getResult());
        assertEquals("error message", loggerInfo.getErrorMsg());
    }

    @Test
    void testWithNullValues() {
        LoggerInfo loggerInfo = new LoggerInfo();
        
        assertNull(loggerInfo.getMethodName());
        assertNull(loggerInfo.getSourceIp());
        assertNull(loggerInfo.getDescription());
        assertNull(loggerInfo.getUri());
        assertNull(loggerInfo.getMethod());
        assertNull(loggerInfo.getRequestParams());
        assertNull(loggerInfo.getTimeConsuming());
        assertNull(loggerInfo.getTraceId());
        assertNull(loggerInfo.getResult());
        assertNull(loggerInfo.getErrorMsg());
    }

    @Test
    void testCompleteLoggerInfo() {
        LoggerInfo loggerInfo = new LoggerInfo();
        loggerInfo.setMethodName("UserController.login");
        loggerInfo.setSourceIp("127.0.0.1");
        loggerInfo.setDescription("用户登录");
        loggerInfo.setUri("/api/user/login");
        loggerInfo.setMethod("POST");
        loggerInfo.setRequestParams("{\"username\":\"admin\"}");
        loggerInfo.setTimeConsuming(250L);
        loggerInfo.setTraceId("abc-123-def");
        loggerInfo.setResult("{\"code\":200,\"msg\":\"success\"}");
        
        assertNotNull(loggerInfo.getMethodName());
        assertNotNull(loggerInfo.getSourceIp());
        assertNotNull(loggerInfo.getDescription());
        assertNotNull(loggerInfo.getUri());
        assertNotNull(loggerInfo.getMethod());
        assertNotNull(loggerInfo.getRequestParams());
        assertNotNull(loggerInfo.getTimeConsuming());
        assertNotNull(loggerInfo.getTraceId());
        assertNotNull(loggerInfo.getResult());
        assertNull(loggerInfo.getErrorMsg());
    }

    @Test
    void testWithErrorMessage() {
        LoggerInfo loggerInfo = new LoggerInfo();
        loggerInfo.setMethodName("UserController.delete");
        loggerInfo.setErrorMsg("用户不存在");
        
        assertEquals("UserController.delete", loggerInfo.getMethodName());
        assertEquals("用户不存在", loggerInfo.getErrorMsg());
    }
}
