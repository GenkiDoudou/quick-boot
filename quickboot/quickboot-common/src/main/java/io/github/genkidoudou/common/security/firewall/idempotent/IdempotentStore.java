package io.github.genkidoudou.common.security.firewall.idempotent;

import java.time.Duration;

/**
 * 幂等占位存储：须保证并发下「仅首次占位成功」语义，并带 TTL。
 */
public interface IdempotentStore {

    /**
     * 若 key 不存在则原子占位并设置 TTL；已存在（含未过期）则返回 {@code false}。
     *
     * @return {@code true} 表示本次获得锁（首次）
     */
    boolean setIfAbsent(String key, Duration ttl);

    /**
     * 主动删除占位，用于业务异常后允许同 token 重试，或 {@link Idempotent#deleteAfterExecution()}。
     */
    void delete(String key);
}
