package io.github.genkidoudou.common.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ErrorException 错误异常测试类
 *
 * @author genkidoudou
 */
class ErrorExceptionTest {

    @Test
    void testNoArgsConstructor() {
        ErrorException exception = new ErrorException();
        assertNotNull(exception);
        assertNull(exception.getCode());
        assertNull(exception.getMsg());
    }

    @Test
    void testConstructorWithMsg() {
        ErrorException exception = new ErrorException("系统错误");
        assertEquals("系统错误", exception.getMsg());
        assertEquals("系统错误", exception.getMessage());
    }

    @Test
    void testConstructorWithCodeAndMsg() {
        ErrorException exception = new ErrorException(500, "服务器内部错误");
        assertEquals(500, exception.getCode());
        assertEquals("服务器内部错误", exception.getMsg());
    }

    @Test
    void testConstructorWithCodeMsgAndArgs() {
        Object[] args = {"数据库", "连接失败"};
        ErrorException exception = new ErrorException(500, "{0}{1}", args);
        assertEquals(500, exception.getCode());
        assertEquals("{0}{1}", exception.getMsg());
        assertArrayEquals(args, exception.getArgs());
    }

    @Test
    void testConstructorWithMsgAndCause() {
        Throwable cause = new RuntimeException("数据库连接失败");
        ErrorException exception = new ErrorException("系统错误", cause);
        assertEquals("系统错误", exception.getMsg());
        assertEquals(cause, exception.getCause());
    }

    @Test
    void testConstructorWithCodeMsgAndCause() {
        Throwable cause = new RuntimeException("数据库连接失败");
        ErrorException exception = new ErrorException(500, "系统错误", cause);
        assertEquals(500, exception.getCode());
        assertEquals("系统错误", exception.getMsg());
        assertEquals(cause, exception.getCause());
    }

    @Test
    void testConstructorWithAllParams() {
        Throwable cause = new RuntimeException("数据库连接失败");
        Object[] args = {"MySQL", "localhost"};
        ErrorException exception = new ErrorException(500, "无法连接到{0}数据库: {1}", args, cause);
        assertEquals(500, exception.getCode());
        assertEquals("无法连接到{0}数据库: {1}", exception.getMsg());
        assertArrayEquals(args, exception.getArgs());
        assertEquals(cause, exception.getCause());
    }

    @Test
    void testInheritanceFromBaseException() {
        ErrorException exception = new ErrorException("测试");
        assertTrue(exception instanceof BaseException);
        assertTrue(exception instanceof RuntimeException);
    }
}
