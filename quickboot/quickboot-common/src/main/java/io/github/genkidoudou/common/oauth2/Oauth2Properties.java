package io.github.genkidoudou.common.oauth2;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * OAuth2 双角色（AS + Client）配置，绑定前缀 {@code qc.oauth2}。
 */
@Data
@ConfigurationProperties(prefix = "qc.oauth2")
public class Oauth2Properties {

    /**
     * Sa-Token 会话 / OAuth2 code·token 持久化方式。
     * <ul>
     *   <li>{@code local}：进程内内存（本地开发、单实例）</li>
     *   <li>{@code redis}：Redis（多实例、生产推荐）</li>
     * </ul>
     */
    private String tokenStore = "local";

    private final Server server = new Server();
    private final Client client = new Client();

    @Data
    public static class Server {
        /** 是否启用授权服务器 */
        private boolean enabled = true;
        /** 可选 issuer（OIDC 预留） */
        private String issuer = "";
        private final Grant grant = new Grant();
    }

    @Data
    public static class Grant {
        /** 是否允许 password grant（生产建议 false） */
        private boolean passwordEnabled = false;
        /** 是否允许 implicit grant（生产建议 false） */
        private boolean implicitEnabled = false;
    }

    @Data
    public static class Client {
        /** 是否启用外部 IdP 联邦登录 */
        private boolean enabled = true;
        /** 联邦登录成功后前端默认跳转路径 */
        private String defaultRedirectAfterLogin = "/";
    }
}
