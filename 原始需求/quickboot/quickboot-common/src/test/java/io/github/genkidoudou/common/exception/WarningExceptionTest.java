package io.github.genkidoudou.common.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * WarningException 警告异常测试类
 *
 * @author genkidoudou
 */
class WarningExceptionTest {

    @Test
    void testNoArgsConstructor() {
        WarningException exception = new WarningException();
        assertNotNull(exception);
        assertNull(exception.getCode());
        assertNull(exception.getMsg());
    }

    @Test
    void testConstructorWithMsg() {
        WarningException exception = new WarningException("警告信息");
        assertEquals("警告信息", exception.getMsg());
        assertEquals("警告信息", exception.getMessage());
    }

    @Test
    void testConstructorWithCodeAndMsg() {
        WarningException exception = new WarningException(400, "参数不完整");
        assertEquals(400, exception.getCode());
        assertEquals("参数不完整", exception.getMsg());
    }

    @Test
    void testConstructorWithCodeMsgAndArgs() {
        Object[] args = {"用户名", "必填"};
        WarningException exception = new WarningException(400, "{0}字段{1}", args);
        assertEquals(400, exception.getCode());
        assertEquals("{0}字段{1}", exception.getMsg());
        assertArrayEquals(args, exception.getArgs());
    }

    @Test
    void testConstructorWithMsgAndCause() {
        Throwable cause = new RuntimeException("验证失败");
        WarningException exception = new WarningException("参数验证警告", cause);
        assertEquals("参数验证警告", exception.getMsg());
        assertEquals(cause, exception.getCause());
    }

    @Test
    void testConstructorWithCodeMsgAndCause() {
        Throwable cause = new RuntimeException("验证失败");
        WarningException exception = new WarningException(400, "参数验证警告", cause);
        assertEquals(400, exception.getCode());
        assertEquals("参数验证警告", exception.getMsg());
        assertEquals(cause, exception.getCause());
    }

    @Test
    void testConstructorWithAllParams() {
        Throwable cause = new RuntimeException("验证失败");
        Object[] args = {"email", "格式不正确"};
        WarningException exception = new WarningException(400, "{0}字段{1}", args, cause);
        assertEquals(400, exception.getCode());
        assertEquals("{0}字段{1}", exception.getMsg());
        assertArrayEquals(args, exception.getArgs());
        assertEquals(cause, exception.getCause());
    }

    @Test
    void testInheritanceFromBaseException() {
        WarningException exception = new WarningException("测试");
        assertTrue(exception instanceof BaseException);
        assertTrue(exception instanceof RuntimeException);
    }
}
