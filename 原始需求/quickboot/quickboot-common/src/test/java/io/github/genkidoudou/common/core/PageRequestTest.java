package io.github.genkidoudou.common.core;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * PageRequest 分页请求测试类
 *
 * @author genkidoudou
 */
class PageRequestTest {

    @Test
    void testNoArgsConstructor() {
        PageRequest<String> pageRequest = new PageRequest<>();
        assertEquals(1, pageRequest.getCurrent());
        assertEquals(10, pageRequest.getSize());
        assertNull(pageRequest.getParam());
    }

    @Test
    void testConstructorWithCurrentAndSize() {
        PageRequest<String> pageRequest = new PageRequest<>(2, 20);
        assertEquals(2, pageRequest.getCurrent());
        assertEquals(20, pageRequest.getSize());
        assertNull(pageRequest.getParam());
    }

    @Test
    void testAllArgsConstructor() {
        String param = "test param";
        PageRequest<String> pageRequest = new PageRequest<>(3L, 30L, param);
        assertEquals(3, pageRequest.getCurrent());
        assertEquals(30, pageRequest.getSize());
        assertEquals(param, pageRequest.getParam());
    }

    @Test
    void testGetOffset() {
        PageRequest<String> pageRequest1 = new PageRequest<>(1, 10);
        assertEquals(0, pageRequest1.getOffset());

        PageRequest<String> pageRequest2 = new PageRequest<>(2, 10);
        assertEquals(10, pageRequest2.getOffset());

        PageRequest<String> pageRequest3 = new PageRequest<>(3, 20);
        assertEquals(40, pageRequest3.getOffset());

        PageRequest<String> pageRequest4 = new PageRequest<>(5, 15);
        assertEquals(60, pageRequest4.getOffset());
    }

    @Test
    void testSettersAndGetters() {
        PageRequest<String> pageRequest = new PageRequest<>();
        
        pageRequest.setCurrent(5);
        assertEquals(5, pageRequest.getCurrent());

        pageRequest.setSize(25);
        assertEquals(25, pageRequest.getSize());

        pageRequest.setParam("test");
        assertEquals("test", pageRequest.getParam());
    }

    @Test
    void testOffsetWithDifferentValues() {
        PageRequest<String> pageRequest = new PageRequest<>();
        
        pageRequest.setCurrent(1);
        pageRequest.setSize(10);
        assertEquals(0, pageRequest.getOffset());

        pageRequest.setCurrent(10);
        pageRequest.setSize(100);
        assertEquals(900, pageRequest.getOffset());
    }

    @Test
    void testWithComplexParam() {
        class SearchParam {
            String keyword;
            SearchParam(String keyword) {
                this.keyword = keyword;
            }
        }

        SearchParam param = new SearchParam("test");
        PageRequest<SearchParam> pageRequest = new PageRequest<>(1L, 10L, param);
        
        assertEquals(param, pageRequest.getParam());
        assertEquals("test", pageRequest.getParam().keyword);
    }
}
