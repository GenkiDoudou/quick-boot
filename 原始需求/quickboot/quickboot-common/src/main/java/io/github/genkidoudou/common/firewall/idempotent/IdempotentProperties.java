package io.github.genkidoudou.common.firewall.idempotent;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 幂等配置属性
 *
 * @author QuickBoot
 * @since 2026/03/02
 */
@ConfigurationProperties(prefix = "qc.security.firewall.idempotent")
public class IdempotentProperties {

    /**
     * 是否启用幂等
     */
    private boolean enabled = true;

    /**
     * 自动拦截的请求方式列表
     * 支持：GET, POST, PUT, DELETE, PATCH 等
     * 为空时不自动拦截，只拦截加了 @Idempotent 注解的接口
     * 示例：[POST, PUT, DELETE]
     */
    private List<String> interceptMethods = new ArrayList<>();

    /**
     * 自动拦截时排除的 URL 列表（支持 Ant 风格路径）
     */
    private List<String> excludeUrls = new ArrayList<>();

    /**
     * 全局过期时间
     */
    private long expireTime = 5;

    /**
     * 全局过期时间单位
     */
    private TimeUnit timeUnit = TimeUnit.SECONDS;

    /**
     * 幂等键前缀
     */
    private String keyPrefix = "idempotent";

    /**
     * 默认提示信息
     */
    private String defaultMessage = "请勿重复提交";

    /**
     * Token 请求头名称（当使用 TOKEN 策略时）
     */
    private String tokenHeader = "X-Idempotent-Token";

    /**
     * 缓存类型（redis 或 caffeine）
     */
    private String cacheType = "redis";

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public List<String> getInterceptMethods() {
        return interceptMethods;
    }

    public void setInterceptMethods(List<String> interceptMethods) {
        this.interceptMethods = interceptMethods;
    }

    public List<String> getExcludeUrls() {
        return excludeUrls;
    }

    public void setExcludeUrls(List<String> excludeUrls) {
        this.excludeUrls = excludeUrls;
    }

    public long getExpireTime() {
        return expireTime;
    }

    public void setExpireTime(long expireTime) {
        this.expireTime = expireTime;
    }

    public TimeUnit getTimeUnit() {
        return timeUnit;
    }

    public void setTimeUnit(TimeUnit timeUnit) {
        this.timeUnit = timeUnit;
    }

    public String getKeyPrefix() {
        return keyPrefix;
    }

    public void setKeyPrefix(String keyPrefix) {
        this.keyPrefix = keyPrefix;
    }

    public String getDefaultMessage() {
        return defaultMessage;
    }

    public void setDefaultMessage(String defaultMessage) {
        this.defaultMessage = defaultMessage;
    }

    public String getTokenHeader() {
        return tokenHeader;
    }

    public void setTokenHeader(String tokenHeader) {
        this.tokenHeader = tokenHeader;
    }

    public String getCacheType() {
        return cacheType;
    }

    public void setCacheType(String cacheType) {
        this.cacheType = cacheType;
    }
}
