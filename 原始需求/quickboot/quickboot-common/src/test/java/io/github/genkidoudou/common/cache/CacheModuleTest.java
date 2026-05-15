package io.github.genkidoudou.common.cache;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 缓存模块测试类
 * 测试 Caffeine 和 Redis 缓存管理器的动态过期时间功能
 * 
 * @author QuickBoot
 * @date 2026-03-01
 */
@SpringBootTest(classes = CacheModuleTest.TestConfig.class)
class CacheModuleTest {

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private TestService testService;

    /**
     * 测试缓存管理器是否正确初始化
     */
    @Test
    void testCacheManagerInitialization() {
        assertNotNull(cacheManager, "缓存管理器不应为空");
        assertTrue(cacheManager instanceof CustomCaffeineCacheManager, 
                "缓存管理器应该是 CustomCaffeineCacheManager 实例");
    }

    /**
     * 测试动态过期时间解析
     * 验证 cacheName#ttl 格式是否正确解析
     */
    @Test
    void testDynamicExpireTime() {
        // 测试带过期时间的缓存名称
        Cache cache3600 = cacheManager.getCache("testCache#3600");
        assertNotNull(cache3600, "缓存实例不应为空");
        
        Cache cache600 = cacheManager.getCache("testCache#600");
        assertNotNull(cache600, "缓存实例不应为空");
        
        Cache cacheDefault = cacheManager.getCache("testCache");
        assertNotNull(cacheDefault, "缓存实例不应为空");
    }

    /**
     * 测试缓存的基本操作
     * 验证缓存的存取功能
     */
    @Test
    void testCacheBasicOperations() {
        String cacheName = "userCache#3600";
        Cache cache = cacheManager.getCache(cacheName);
        assertNotNull(cache, "缓存实例不应为空");

        // 测试缓存写入
        String key = "user:1";
        String value = "张三";
        cache.put(key, value);

        // 测试缓存读取
        Cache.ValueWrapper wrapper = cache.get(key);
        assertNotNull(wrapper, "缓存值包装器不应为空");
        assertEquals(value, wrapper.get(), "缓存值应该匹配");

        // 测试缓存清除
        cache.evict(key);
        Cache.ValueWrapper afterEvict = cache.get(key);
        assertNull(afterEvict, "清除后缓存值应为空");
    }

    /**
     * 测试注解方式的缓存
     * 验证 @Cacheable 注解是否正常工作
     */
    @Test
    void testCacheableAnnotation() {
        // 第一次调用，应该执行方法并缓存结果
        String result1 = testService.getUserName(1L);
        assertEquals("用户-1", result1, "第一次调用结果应该正确");

        // 第二次调用，应该从缓存中获取
        String result2 = testService.getUserName(1L);
        assertEquals("用户-1", result2, "第二次调用结果应该从缓存获取");

        // 验证方法只执行了一次（通过计数器验证）
        assertEquals(1, testService.getCallCount(1L), "方法应该只执行一次");
    }

    /**
     * 测试不同过期时间的缓存
     * 验证不同的 TTL 配置是否生效
     */
    @Test
    void testDifferentExpireTimes() {
        // 测试 1 小时过期时间
        String longCache = testService.getLongCache("key1");
        assertEquals("长期缓存-key1", longCache);

        // 测试 10 分钟过期时间
        String shortCache = testService.getShortCache("key2");
        assertEquals("短期缓存-key2", shortCache);

        // 验证缓存已存在
        Cache longCacheInstance = cacheManager.getCache("longCache#3600");
        assertNotNull(longCacheInstance);
        assertNotNull(longCacheInstance.get("key1"));

        Cache shortCacheInstance = cacheManager.getCache("shortCache#600");
        assertNotNull(shortCacheInstance);
        assertNotNull(shortCacheInstance.get("key2"));
    }

    /**
     * 测试配置类
     * 提供测试所需的 Bean 配置
     */
    @Configuration
    @EnableCaching
    static class TestConfig {

        /**
         * 配置 Caffeine 缓存管理器用于测试
         */
        @Bean
        public CacheManager cacheManager() {
            return new CustomCaffeineCacheManager();
        }

        /**
         * 提供测试服务 Bean
         */
        @Bean
        public TestService testService() {
            return new TestService();
        }
    }

    /**
     * 测试服务类
     * 用于测试缓存注解功能
     */
    @Service
    static class TestService {

        /**
         * 方法调用计数器，用于验证缓存是否生效
         */
        private final java.util.Map<Long, Integer> callCountMap = new java.util.concurrent.ConcurrentHashMap<>();

        /**
         * 获取用户名称（缓存 1 小时）
         * 
         * @param userId 用户ID
         * @return 用户名称
         */
        @Cacheable(cacheNames = "userCache#3600", key = "#userId")
        public String getUserName(Long userId) {
            // 增加调用计数
            callCountMap.merge(userId, 1, Integer::sum);
            return "用户-" + userId;
        }

        /**
         * 获取方法调用次数
         * 
         * @param userId 用户ID
         * @return 调用次数
         */
        public int getCallCount(Long userId) {
            return callCountMap.getOrDefault(userId, 0);
        }

        /**
         * 获取长期缓存数据（缓存 1 小时）
         * 
         * @param key 缓存键
         * @return 缓存值
         */
        @Cacheable(cacheNames = "longCache#3600", key = "#key")
        public String getLongCache(String key) {
            return "长期缓存-" + key;
        }

        /**
         * 获取短期缓存数据（缓存 10 分钟）
         * 
         * @param key 缓存键
         * @return 缓存值
         */
        @Cacheable(cacheNames = "shortCache#600", key = "#key")
        public String getShortCache(String key) {
            return "短期缓存-" + key;
        }
    }
}
