package io.github.genkidoudou.common.firewall.client;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * 客户端配置属性
 *
 * @author luyanan
 * @since 2026-03-04
 */
@Data
@ConfigurationProperties(prefix = "qc.security.firewall.client")
public class ClientProperties {

    /**
     * 是否启用客户端认证
     */
    private Boolean enabled = false;

    /**
     * 数据源类型：config(配置文件) 或 database(数据库)
     */
    private String source = "config";

    /**
     * 客户端列表（仅当 source=config 时有效）
     */
    private List<OauthClient> clients = new ArrayList<>();

    /**
     * 排除的URL（不需要客户端认证）
     */
    private List<String> excludeUrls = new ArrayList<>();

    /**
     * 客户端ID的Header名称
     */
    private String clientIdHeader = "X-Client-Id";


}
