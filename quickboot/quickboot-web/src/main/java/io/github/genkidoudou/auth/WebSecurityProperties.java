package io.github.genkidoudou.auth;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * Web 层 Sa-Token 匿名路径等安全相关配置。
 */
@Data
@ConfigurationProperties(prefix = "qc.security.web")
public class WebSecurityProperties {

    /**
     * 除登录/验证码/文档等固定路径外，额外放行（不要求登录）的 Ant 路径。
     * 生产环境应为空；开发可在 {@code application-dev.yml} 中配置 {@code /actuator/**}、H2 控制台等。
     */
    private List<String> anonymousPaths = new ArrayList<>();
}
