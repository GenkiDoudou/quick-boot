package io.github.genkidoudou.common.firewall.idempotent;

import java.util.concurrent.TimeUnit;

/**
 * 幂等存储接口
 *
 * @author QuickBoot
 * @since 2026/03/02
 */
public interface IdempotentStorage {

    /**
     * 尝试设置键（如果键不存在）
     *
     * @param key        键
     * @param value      值
     * @param expireTime 过期时间
     * @param timeUnit   时间单位
     * @return true: 设置成功（键不存在），false: 设置失败（键已存在）
     */
    boolean setIfAbsent(String key, String value, long expireTime, TimeUnit timeUnit);

    /**
     * 删除键
     *
     * @param key 键
     */
    void delete(String key);

    /**
     * 检查键是否存在
     *
     * @param key 键
     * @return true: 存在，false: 不存在
     */
    boolean exists(String key);
}
