package io.github.genkidoudou.web.auth.clientsign;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Client HMAC 签名校验配置，前缀 {@code qc.security.client-sign}。
 */
@Data
@Component
@ConfigurationProperties(prefix = "qc.security.client-sign")
public class ClientSignProperties {

    /** 是否启用（dev 默认可 true，按需关闭） */
    private boolean enabled = true;

    /** 时间窗（秒），默认 5 分钟 */
    private int windowSeconds = 300;

    /** nonce 缓存名（配合 DynamicTtl*CacheManager 的 name#ttl 约定） */
    private String nonceCacheName = "clientSignNonce#300";

    /** 跳过签名校验的路径（Ant 风格） */
    private List<String> excludePaths = defaultExcludes();

    private static List<String> defaultExcludes() {
        List<String> paths = new ArrayList<>();
        paths.add("/error");
        paths.add("/oauth2/**");
        paths.add("/swagger-ui.html");
        paths.add("/swagger-ui/**");
        paths.add("/v3/api-docs/**");
        return paths;
    }
}
