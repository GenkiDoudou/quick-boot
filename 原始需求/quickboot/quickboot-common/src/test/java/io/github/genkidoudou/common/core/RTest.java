package io.github.genkidoudou.common.core;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * R 通用返回结果测试类
 *
 * @author genkidoudou
 */
class RTest {

    @Test
    void testOk() {
        R<String> result = R.ok();
        assertEquals(GlobalMsgCode.SUCCESS, result.getCode());
        assertEquals("success", result.getMsg());
        assertNull(result.getData());
        assertTrue(result.isSuccess());
        assertFalse(result.isError());
        assertNotNull(result.getTraceId());
        assertNotNull(result.getTimestamp());
    }

    @Test
    void testOkWithMsg() {
        R<String> result = R.ok("操作成功");
        assertEquals(GlobalMsgCode.SUCCESS, result.getCode());
        assertEquals("操作成功", result.getMsg());
        assertNull(result.getData());
    }

    @Test
    void testOkWithData() {
        R<String> result = R.ok("test data");
        assertEquals(GlobalMsgCode.SUCCESS, result.getCode());
        assertEquals("success", result.getMsg());
        assertEquals("test data", result.getData());
    }

    @Test
    void testOkWithMsgAndData() {
        R<String> result = R.ok("操作成功", "test data");
        assertEquals(GlobalMsgCode.SUCCESS, result.getCode());
        assertEquals("操作成功", result.getMsg());
        assertEquals("test data", result.getData());
    }

    @Test
    void testError() {
        R<String> result = R.error();
        assertEquals(GlobalMsgCode.INTERNAL_SERVER_ERROR, result.getCode());
        assertEquals("error", result.getMsg());
        assertNull(result.getData());
        assertFalse(result.isSuccess());
        assertTrue(result.isError());
    }

    @Test
    void testErrorWithMsg() {
        R<String> result = R.error("操作失败");
        assertEquals(GlobalMsgCode.INTERNAL_SERVER_ERROR, result.getCode());
        assertEquals("操作失败", result.getMsg());
        assertNull(result.getData());
    }

    @Test
    void testErrorWithCodeAndMsg() {
        R<String> result = R.error(400, "参数错误");
        assertEquals(400, result.getCode());
        assertEquals("参数错误", result.getMsg());
        assertNull(result.getData());
    }

    @Test
    void testErrorWithCodeMsgAndData() {
        R<String> result = R.error(400, "参数错误", "error data");
        assertEquals(400, result.getCode());
        assertEquals("参数错误", result.getMsg());
        assertEquals("error data", result.getData());
    }

    @Test
    void testStatusTrue() {
        R<String> result = R.status(true);
        assertTrue(result.isSuccess());
    }

    @Test
    void testStatusFalse() {
        R<String> result = R.status(false);
        assertTrue(result.isError());
    }

    @Test
    void testStatusWithMsg() {
        R<String> successResult = R.status(true, "成功");
        assertEquals("成功", successResult.getMsg());
        assertTrue(successResult.isSuccess());

        R<String> errorResult = R.status(false, "失败");
        assertEquals("失败", errorResult.getMsg());
        assertTrue(errorResult.isError());
    }

    @Test
    void testStatusWithData() {
        R<String> successResult = R.status(true, "data");
        assertEquals("data", successResult.getData());
        assertTrue(successResult.isSuccess());

        R<String> errorResult = R.status(false, "data");
        assertNull(errorResult.getData());
        assertTrue(errorResult.isError());
    }

    @Test
    void testIsSuccess() {
        R<String> result = new R<>(GlobalMsgCode.SUCCESS, "success", null);
        assertTrue(result.isSuccess());
    }

    @Test
    void testIsError() {
        R<String> result = new R<>(500, "error", null);
        assertTrue(result.isError());
    }
}
