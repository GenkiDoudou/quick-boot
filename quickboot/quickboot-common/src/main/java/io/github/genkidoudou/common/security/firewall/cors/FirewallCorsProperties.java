package io.github.genkidoudou.common.security.firewall.cors;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * 安全防火墙：CORS 跨域配置，绑定前缀 {@code qc.security.firewall.cors}。
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "qc.security.firewall.cors")
public class FirewallCorsProperties {

    /**
     * 默认关闭；显式 {@code true} 时注册 CORS Filter。
     */
    private boolean enabled = false;

    /**
     * 允许的 Origin 列表，支持 {@code *}。
     * <p>
     * 说明：当列表为空时，等价于允许 {@code *}（便于默认可用）；当 {@code allowCredentials=true} 且允许 {@code *}
     * 时，将采用“回显 Origin”的方式满足浏览器规范。
     */
    private List<String> allowedOrigins = new ArrayList<>();

    /**
     * 允许的 HTTP Method（默认 GET/POST/PUT/DELETE/OPTIONS）。
     */
    private List<String> allowedMethods = new ArrayList<>(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));

    /**
     * 允许的请求头（默认 {@code *}）。
     */
    private List<String> allowedHeaders = new ArrayList<>(List.of("*"));

    /**
     * 允许暴露给前端读取的响应头列表（默认空）。
     */
    private List<String> exposedHeaders = new ArrayList<>();

    /**
     * 是否允许携带凭证（Cookie/Authorization 等），默认 {@code true}。
     */
    private boolean allowCredentials = true;

    /**
     * 预检缓存时间（秒），默认 3600。
     */
    private long maxAge = 3600L;

    /**
     * 生效路径模式（Ant 风格），默认 {@code /**}。
     */
    private String pathPattern = "/**";
}

