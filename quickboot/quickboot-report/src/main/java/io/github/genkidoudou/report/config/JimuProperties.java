package io.github.genkidoudou.report.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * 积木报表集成配置。
 */
@Data
@ConfigurationProperties(prefix = "qc.jimu")
public class JimuProperties {

    private boolean enabled = true;

    /**
     * 管理端 iframe / 设计器基址，如 http://localhost:9992
     */
    private String baseUrl = "http://localhost:9992";

    private Share share = new Share();

    private Security security = new Security();

    @Data
    public static class Share {
        private boolean enabled = true;
    }

    @Data
    public static class Security {
        private List<String> excludeSaTokenPaths = defaultExcludePaths();

        private static List<String> defaultExcludePaths() {
            List<String> paths = new ArrayList<>();
            paths.add("/jmreport/**");
            paths.add("/drag/**");
            paths.add("/jimubi/**");
            paths.add("/jimureport/**");
            return paths;
        }
    }
}
