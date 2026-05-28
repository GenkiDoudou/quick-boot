package io.github.genkidoudou.auth;

import cn.hutool.core.util.StrUtil;
import io.github.genkidoudou.common.exception.ErrorCodes;
import io.github.genkidoudou.common.exception.WarningException;
import io.github.genkidoudou.core.service.LoginLockService;
import io.github.genkidoudou.web.system.config.service.SysConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

/**
 * 基于 Spring {@link org.springframework.cache.CacheManager} 的登录失败锁定实现。
 * <p>
 * 配置解析策略：{@code qc.login.fail-lock-enabled} 非 true/1/yes/on 时视为关闭；
 * {@code qc.login.max-retry}、{@code qc.login.lock-minutes} 解析非正数时分别回退为 5、30，并打 warn 日志。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LoginLockServiceImpl implements LoginLockService {

    /** 与 {@link DynamicTtlRedisCacheManager} / {@link io.github.genkidoudou.common.cache.DynamicTtlCaffeineCacheManager} 命名约定一致。 */
    public static final String CACHE_NAME = "qc-login-fail#604800";

    private static final String KEY_ENABLED = "qc.login.fail-lock-enabled";
    private static final String KEY_MAX_RETRY = "qc.login.max-retry";
    private static final String KEY_LOCK_MINUTES = "qc.login.lock-minutes";

    private final CacheManager cacheManager;
    private final SysConfigService sysConfigService;

    @Override
    public String normalizeUserName(String userName) {
        return userName == null ? "" : userName.trim();
    }

    @Override
    public void assertNotLocked(String userName) {
        if (!isFailLockEnabled()) {
            return;
        }
        String key = normalizeUserName(userName);
        if (key.isEmpty()) {
            return;
        }
        if (isLockedInternal(key)) {
            throw new WarningException(ErrorCodes.Security.FORBIDDEN, "登录尝试过于频繁，账号已暂时锁定，请稍后再试或联系管理员解锁");
        }
    }

    @Override
    public void recordFailure(String userName) {
        if (!isFailLockEnabled()) {
            return;
        }
        String key = normalizeUserName(userName);
        if (key.isEmpty()) {
            return;
        }
        Cache cache = requireCache();
        long now = System.currentTimeMillis();
        LoginFailState state = cache.get(key, LoginFailState.class);
        if (state == null) {
            state = new LoginFailState(0, 0);
        }
        if (state.getLockedUntilMs() > 0 && state.getLockedUntilMs() <= now) {
            state.setFailCount(0);
            state.setLockedUntilMs(0);
        }
        state.setFailCount(state.getFailCount() + 1);
        if (state.getFailCount() >= maxRetry()) {
            state.setLockedUntilMs(now + lockMinutes() * 60_000L);
        }
        cache.put(key, state);
    }

    @Override
    public void onLoginSuccess(String userName) {
        clearForUserName(userName);
    }

    @Override
    public void clearForUserName(String userName) {
        String key = normalizeUserName(userName);
        if (key.isEmpty()) {
            return;
        }
        Cache cache = cacheManager.getCache(CACHE_NAME);
        if (cache != null) {
            cache.evict(key);
        }
    }

    private boolean isLockedInternal(String key) {
        Cache cache = cacheManager.getCache(CACHE_NAME);
        if (cache == null) {
            return false;
        }
        LoginFailState state = cache.get(key, LoginFailState.class);
        if (state == null) {
            return false;
        }
        long now = System.currentTimeMillis();
        if (state.getLockedUntilMs() > 0 && state.getLockedUntilMs() <= now) {
            cache.evict(key);
            return false;
        }
        return state.getLockedUntilMs() > now;
    }

    private Cache requireCache() {
        Cache cache = cacheManager.getCache(CACHE_NAME);
        if (cache == null) {
            throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "登录锁定缓存未初始化");
        }
        return cache;
    }

    private boolean isFailLockEnabled() {
        String raw = sysConfigService.getConfigValueByKey(KEY_ENABLED);
        if (StrUtil.isBlank(raw)) {
            return true;
        }
        String v = raw.trim().toLowerCase();
        return "true".equals(v) || "1".equals(v) || "yes".equals(v) || "on".equals(v);
    }

    private int maxRetry() {
        String raw = sysConfigService.getConfigValueByKey(KEY_MAX_RETRY);
        int n = parsePositiveInt(raw, 5);
        if (n <= 0) {
            log.warn("配置 {} 解析失败或非正数，使用默认值 5", KEY_MAX_RETRY);
            return 5;
        }
        return n;
    }

    private int lockMinutes() {
        String raw = sysConfigService.getConfigValueByKey(KEY_LOCK_MINUTES);
        int n = parsePositiveInt(raw, 30);
        if (n <= 0) {
            log.warn("配置 {} 解析失败或非正数，使用默认值 30", KEY_LOCK_MINUTES);
            return 30;
        }
        return n;
    }

    private static int parsePositiveInt(String raw, int defaultVal) {
        if (StrUtil.isBlank(raw)) {
            return defaultVal;
        }
        try {
            return Integer.parseInt(raw.trim());
        } catch (NumberFormatException e) {
            return defaultVal;
        }
    }
}
