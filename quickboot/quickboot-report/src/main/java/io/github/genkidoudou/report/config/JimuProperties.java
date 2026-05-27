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

    /**
     * 为 true 且存在 {@code RedisConnectionFactory} 时注册 Jimu 专用 {@link org.springframework.data.redis.core.RedisTemplate}
     *（字符串序列化，适配 increment）。本地无 Redis 时保持 false，使用修复后的内存缓存即可解析 SQL。
     */
    private Redis redis = new Redis();

    /**
     * 将积木 {@code jimu_report_data_source} 与 Spring {@code spring.datasource} 对齐（覆盖官方演示库地址）。
     */
    private PrimaryDataSource primaryDataSource = new PrimaryDataSource();

    private Security security = new Security();

    @Data
    public static class Redis {
        private boolean enabled = false;
    }

    @Data
    public static class PrimaryDataSource {
        /** 启动后同步主库 JDBC（dev/prod 自动跟随 application-*.yml） */
        private boolean syncOnStartup = true;
        /** 官方演示数据源 ID，设计器默认选中 */
        private String id = "1011872166805864448";
        private String name = "QuickBoot主库";
        private String code = "qc2";
    }

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
