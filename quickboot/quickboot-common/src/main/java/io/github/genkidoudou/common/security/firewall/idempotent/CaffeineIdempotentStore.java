package io.github.genkidoudou.common.security.firewall.idempotent;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 进程内幂等存储（{@link ConcurrentHashMap#compute} 实现 NX 语义 + 截止时间）。
 * <p>
 * <b>局限</b>：仅当前 JVM 生效，多节点部署时无法互斥；生产多实例请使用 {@link RedisIdempotentStore} 或 {@code cache-type=auto} 且提供 Redis。
 */
public class CaffeineIdempotentStore implements IdempotentStore {

    private final ConcurrentHashMap<String, Long> deadlineByKey = new ConcurrentHashMap<>();

    @Override
    public boolean setIfAbsent(String key, Duration ttl) {
        long now = System.currentTimeMillis();
        long cap = now + Math.max(1, ttl.toMillis());
        boolean[] win = {false};
        deadlineByKey.compute(key, (k, oldDeadline) -> {
            if (oldDeadline != null && oldDeadline > now) {
                win[0] = false;
                return oldDeadline;
            }
            win[0] = true;
            return cap;
        });
        return win[0];
    }

    @Override
    public void delete(String key) {
        deadlineByKey.remove(key);
    }
}
