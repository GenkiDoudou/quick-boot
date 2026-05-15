package io.github.genkidoudou.common.info;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@EnableConfigurationProperties(AppInfoProperties.class)
@Configuration
@PropertySource(value = "classpath:git.properties", encoding = "UTF-8")
public class AppInfoConfiguration {

    @Value("${spring.application.name:}")
    private String appName;

    // 注入 Git Tag（默认值 unknown）
    @Value("${git.tag:1.0}")
    private String gitVersion;
    @Autowired
    private AppInfoProperties appInfoProperties;
    @PostConstruct
    public void appInfo() {
        if (null != appInfoProperties) {
            appInfoProperties.setVersion(gitVersion);
            appInfoProperties.setApplicationName(appName);
        }
    }
}
