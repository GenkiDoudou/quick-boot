package io.github.genkidoudou.common.file;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * 文件上传模块配置属性
 *
 * @author genkidoudou
 * @since 2026/03/11
 */
@Data
@ConfigurationProperties(prefix = "qc.file")
public class FileProperties {

    /**
     * 是否启用文件上传模块
     */
    private Boolean enabled = true;

    /**
     * 存储类型：local | minio
     */
    private String type = "local";

    /**
     * 访问域名，如 https://cdn.example.com
     */
    private String domain;

    /**
     * 基础路径
     */
    private String basePath = "uploads";

    /**
     * 默认分类（未配置的分类使用此值）
     */
    private String defaultClassify = "default";

    /**
     * 文件分类配置
     */
    private List<FileClassifyProperties> classifies = new ArrayList<>();

    /**
     * 本地存储配置
     */
    private LocalProperties local = new LocalProperties();

    /**
     * MinIO 存储配置
     */
    private MinioProperties minio = new MinioProperties();

    /**
     * 短链配置（用于 getShortUrl）
     */
    private ShortUrlProperties shortUrl = new ShortUrlProperties();

    /**
     * 文件分类配置
     */
    @Data
    public static class FileClassifyProperties {
        /**
         * 分类标识，如 image、document
         */
        private String classify;

        /**
         * 允许后缀，逗号分隔，如 jpg,jpeg,png,gif
         */
        private String limitExt;

        /**
         * 大小限制（字节），默认 10MB
         */
        private Long limitSize = 10485760L;
    }

    /**
     * 本地存储配置
     */
    @Data
    public static class LocalProperties {
        /**
         * 本地根目录
         */
        private String path = "/data/uploads";
    }

    /**
     * MinIO 存储配置
     */
    @Data
    public static class MinioProperties {
        /**
         * MinIO 服务地址
         */
        private String endpoint = "http://127.0.0.1:9000";

        /**
         * 访问密钥
         */
        private String accessKey = "minioadmin";

        /**
         * 密钥
         */
        private String secretKey = "minioadmin";

        /**
         * 桶名称
         */
        private String bucket = "quickboot";

        /**
         * 是否使用路径风格访问
         */
        private Boolean pathStyleAccess = true;
    }

    /**
     * 短链配置
     */
    @Data
    public static class ShortUrlProperties {
        /**
         * 是否启用短链
         */
        private Boolean enabled = false;

        /**
         * 短链路径前缀，如 /s/abc123
         */
        private String pathPrefix = "/s";

        /**
         * 短码存储：redis | memory
         */
        private String storage = "memory";
    }
}
