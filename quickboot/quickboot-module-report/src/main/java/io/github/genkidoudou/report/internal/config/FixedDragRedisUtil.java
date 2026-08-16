package io.github.genkidoudou.report.internal.config;

import org.springframework.data.redis.core.RedisTemplate;

import java.lang.reflect.Field;

/**
 * 修复 JimuBI {@code DragRedisUtil}（2.5.0 混淆类 {@code util.g}）在无 Redis 时本地缓存
 * {@code increment} 的缺陷：对 {@link org.springframework.cache.Cache.ValueWrapper}
 * 误用 {@code toString()} 导致 {@code NumberFormatException: ValueWrapper for [1]}。
 */
final class FixedDragRedisUtil extends org.jeecg.modules.drag.util.g {

    private static final Field REDIS_TEMPLATE_FIELD;

    static {
        try {
            REDIS_TEMPLATE_FIELD = org.jeecg.modules.drag.util.g.class.getDeclaredField("redisTemplate");
            REDIS_TEMPLATE_FIELD.setAccessible(true);
        } catch (NoSuchFieldException e) {
            throw new IllegalStateException("DragRedisUtil.redisTemplate 字段不存在，请检查 jimubi 版本", e);
        }
    }

    static void injectRedisTemplate(FixedDragRedisUtil util, RedisTemplate<String, Object> redisTemplate) {
        try {
            REDIS_TEMPLATE_FIELD.set(util, redisTemplate);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("无法注入 DragRedisUtil.redisTemplate", e);
        }
    }

    private static boolean usesRedis(FixedDragRedisUtil util) {
        try {
            return REDIS_TEMPLATE_FIELD.get(util) != null;
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * 修复本地缓存 increment：无 Redis 时正确解析数值而非 ValueWrapper.toString()。
     */
    @Override
    public long a(String key, long delta) {
        if (delta < 0) {
            throw new RuntimeException("递增因子必须大于0");
        }
        if (usesRedis(this)) {
            return super.a(key, delta);
        }
        long current = 0L;
        Object raw = b(key);
        if (raw != null) {
            if (raw instanceof Number number) {
                current = number.longValue();
            } else {
                current = Long.parseLong(raw.toString());
            }
        }
        long next = current + delta;
        // 走 a(String, Object) 写入，避免命中本方法 a(String, long) 造成递归
        a(key, (Object) Long.valueOf(next));
        return next;
    }
}
