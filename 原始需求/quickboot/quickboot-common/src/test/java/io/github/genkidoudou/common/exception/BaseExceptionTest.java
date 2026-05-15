package io.github.genkidoudou.common.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * BaseException 基础异常测试类
 *
 * @author genkidoudou
 */
class BaseExceptionTest {

    @Test
    void testNoArgsConstructor() {
        BaseException exception = new BaseException();
        assertNotNull(exception);
        assertNull(exception.getCode());
        assertNull(exception.getMsg());
        assertNull(exception.getArgs());
    }

    @Test
    void testConstructorWithMsg() {
        BaseException exception = new BaseException("测试异常");
        assertEquals("测试异常", exception.getMsg());
        assertEquals("测试异常", exception.getMessage());
        assertNull(exception.getCode());
        assertNull(exception.getArgs());
    }

    @Test
    void testConstructorWithCodeAndMsg() {
        BaseException exception = new BaseException(400, "参数错误");
        assertEquals(400, exception.getCode());
        assertEquals("参数错误", exception.getMsg());
        assertEquals("参数错误", exception.getMessage());
        assertNull(exception.getArgs());
    }

    @Test
    void testConstructorWithCodeMsgAndArgs() {
        Object[] args = {"arg1", "arg2"};
        BaseException exception = new BaseException(400, "参数错误: {0}, {1}", args);
        assertEquals(400, exception.getCode());
        assertEquals("参数错误: {0}, {1}", exception.getMsg());
        assertArrayEquals(args, exception.getArgs());
    }

    @Test
    void testConstructorWithMsgAndCause() {
        Throwable cause = new RuntimeException("原因");
        BaseException exception = new BaseException("测试异常", cause);
        assertEquals("测试异常", exception.getMsg());
        assertEquals(cause, exception.getCause());
        assertNull(exception.getCode());
    }

    @Test
    void testConstructorWithCodeMsgAndCause() {
        Throwable cause = new RuntimeException("原因");
        BaseException exception = new BaseException(500, "服务器错误", cause);
        assertEquals(500, exception.getCode());
        assertEquals("服务器错误", exception.getMsg());
        assertEquals(cause, exception.getCause());
    }

    @Test
    void testConstructorWithAllParams() {
        Throwable cause = new RuntimeException("原因");
        Object[] args = {"arg1", "arg2"};
        BaseException exception = new BaseException(500, "服务器错误: {0}, {1}", args, cause);
        assertEquals(500, exception.getCode());
        assertEquals("服务器错误: {0}, {1}", exception.getMsg());
        assertArrayEquals(args, exception.getArgs());
        assertEquals(cause, exception.getCause());
    }

    @Test
    void testSettersAndGetters() {
        BaseException exception = new BaseException();
        
        exception.setCode(404);
        assertEquals(404, exception.getCode());

        exception.setMsg("未找到");
        assertEquals("未找到", exception.getMsg());

        Object[] args = {"test"};
        exception.setArgs(args);
        assertArrayEquals(args, exception.getArgs());
    }
}
