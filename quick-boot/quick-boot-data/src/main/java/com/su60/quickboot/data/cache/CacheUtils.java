package com.su60.quickboot.data.cache;

import cn.hutool.extra.spring.SpringUtil;
import lombok.experimental.UtilityClass;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.cache.CacheManager;

@UtilityClass
public class CacheUtils {


	/**
	 * 添加缓存
	 * @since 2025/11/29
	 * @param cacheName 缓存名称
	 * @param key  缓存的key
	 * @param value  缓存的值
	 * @return
	 */
	public void setCache(String cacheName, String key, Object value) {
		CacheManager cacheManager = SpringUtil.getBean(CacheManager.class);
		if (null == cacheManager) {
			throw new RuntimeException("cacheManager is null");
		}
		cacheManager.getCache(cacheName)
				.put(key, value);
	}


	/**
	 * 从缓存中获取
	 * @since 2025/11/29
	 * @param cacheName 缓存名称
	 * @param key key
	 * @param type  返回的类型
	 * @return
	 */
	public <T> T getCache(String cacheName, String key, Class<T> type) {
		CacheManager cacheManager = SpringUtil.getBean(CacheManager.class);
		if (null == cacheManager) {
			throw new RuntimeException("cacheManager is null");
		}
		return cacheManager.getCache(cacheName).get(key, type);
	}

	/**
	 * 删除缓存
	 * @since 2025/11/29
	 * @param cacheName 缓存名称
	 * @param key  缓存的key
	 * @return
	 */
	public void clearCache(String cacheName, String key) {
		CacheManager cacheManager = SpringUtil.getBean(CacheManager.class);
		if (null == cacheManager) {
			throw new RuntimeException("cacheManager is null");
		}
		cacheManager.getCache(cacheName).evict(key);
	}

	/**
	 * 清空缓存
	 * @since 2025/11/29
	 * @param cacheName 缓存名称
	 * @return
	 */
	public void clearCache(String cacheName) {
		CacheManager cacheManager = SpringUtil.getBean(CacheManager.class);
		if (null == cacheManager) {
			throw new RuntimeException("cacheManager is null");
		}
		cacheManager.getCache(cacheName).clear();
	}
}
