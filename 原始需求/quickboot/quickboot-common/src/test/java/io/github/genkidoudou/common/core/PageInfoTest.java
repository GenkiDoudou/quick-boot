package io.github.genkidoudou.common.core;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * PageInfo 分页返回信息测试类
 *
 * @author genkidoudou
 */
class PageInfoTest {

    @Test
    void testConstructorWithAllParams() {
        List<String> records = Arrays.asList("item1", "item2", "item3");
        PageInfo<String> pageInfo = new PageInfo<>(1, 10, records, 100);

        assertEquals(1, pageInfo.getCurrent());
        assertEquals(10, pageInfo.getSize());
        assertEquals(records, pageInfo.getRecords());
        assertEquals(100, pageInfo.getTotal());
        assertEquals(10, pageInfo.getPages());
    }

    @Test
    void testConstructorWithFourParams() {
        List<String> records = Arrays.asList("item1", "item2");
        PageInfo<String> pageInfo = new PageInfo<>(2, 5, records, 23);

        assertEquals(2, pageInfo.getCurrent());
        assertEquals(5, pageInfo.getSize());
        assertEquals(records, pageInfo.getRecords());
        assertEquals(23, pageInfo.getTotal());
        assertEquals(5, pageInfo.getPages()); // (23 + 5 - 1) / 5 = 5
    }

    @Test
    void testPagesCalculation() {
        List<String> records = Arrays.asList("item1");
        
        // 测试整除情况
        PageInfo<String> pageInfo1 = new PageInfo<>(1, 10, records, 100);
        assertEquals(10, pageInfo1.getPages());

        // 测试有余数情况
        PageInfo<String> pageInfo2 = new PageInfo<>(1, 10, records, 95);
        assertEquals(10, pageInfo2.getPages());

        // 测试总数小于每页条数
        PageInfo<String> pageInfo3 = new PageInfo<>(1, 10, records, 5);
        assertEquals(1, pageInfo3.getPages());

        // 测试总数为0
        PageInfo<String> pageInfo4 = new PageInfo<>(1, 10, records, 0);
        assertEquals(0, pageInfo4.getPages());
    }

    @Test
    void testPagesCalculationWithZeroSize() {
        List<String> records = Arrays.asList("item1");
        PageInfo<String> pageInfo = new PageInfo<>(1, 0, records, 100);
        assertEquals(0, pageInfo.getPages());
    }

    @Test
    void testAddExt() {
        PageInfo<String> pageInfo = new PageInfo<>();
        pageInfo.addExt("key1", "value1");
        pageInfo.addExt("key2", 123);

        assertNotNull(pageInfo.getExt());
        assertEquals("value1", pageInfo.getExt().get("key1"));
        assertEquals(123, pageInfo.getExt().get("key2"));
    }

    @Test
    void testAddExtChaining() {
        PageInfo<String> pageInfo = new PageInfo<>();
        PageInfo<String> result = pageInfo.addExt("key1", "value1")
                                          .addExt("key2", "value2");

        assertSame(pageInfo, result);
        assertEquals(2, pageInfo.getExt().size());
    }

    @Test
    void testAddExtWithNullExt() {
        PageInfo<String> pageInfo = new PageInfo<>();
        pageInfo.setExt(null);
        pageInfo.addExt("key", "value");

        assertNotNull(pageInfo.getExt());
        assertEquals("value", pageInfo.getExt().get("key"));
    }

    @Test
    void testNoArgsConstructor() {
        PageInfo<String> pageInfo = new PageInfo<>();
        assertNotNull(pageInfo);
        assertNotNull(pageInfo.getExt());
    }

    @Test
    void testAllArgsConstructor() {
        List<String> records = Arrays.asList("item1", "item2");
        PageInfo<String> pageInfo = new PageInfo<>(1L, 10L, records, 100L, 10L, null);

        assertEquals(1, pageInfo.getCurrent());
        assertEquals(10, pageInfo.getSize());
        assertEquals(records, pageInfo.getRecords());
        assertEquals(100, pageInfo.getTotal());
        assertEquals(10, pageInfo.getPages());
    }
}
