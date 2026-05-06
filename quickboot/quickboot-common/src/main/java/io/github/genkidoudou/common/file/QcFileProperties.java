package io.github.genkidoudou.common.file;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * {@code qc.file.*} 配置绑定：本地/MinIO 切换、分类白名单、域名（{@link io.github.genkidoudou.common.file.url.FileUrl}）等。
 */
@ConfigurationProperties(prefix = "qc.file")
public class QcFileProperties {

    /**
     * 是否启用文件存储能力；为 false 时注入 {@link DisabledFileTemplate}。
     */
    private boolean enabled = true;

    private FileStorageType type = FileStorageType.local;

    /**
     * 对外访问域名，用于 {@code view} 与 {@link io.github.genkidoudou.common.file.url.FileUrl}（如 {@code https://cdn.example.com}，无尾斜杠亦可）。
     */
    private String domain = "";

    private String defaultClassify = "default";

    private final List<ClassifyProperties> classifies = new ArrayList<>();

    private final LocalProperties local = new LocalProperties();

    private final MinioProperties minio = new MinioProperties();

    private final ShortUrlProperties shortUrl = new ShortUrlProperties();

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public FileStorageType getType() {
        return type;
    }

    public void setType(FileStorageType type) {
        this.type = type;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getDefaultClassify() {
        return defaultClassify;
    }

    public void setDefaultClassify(String defaultClassify) {
        this.defaultClassify = defaultClassify;
    }

    public List<ClassifyProperties> getClassifies() {
        return classifies;
    }

    public LocalProperties getLocal() {
        return local;
    }

    public MinioProperties getMinio() {
        return minio;
    }

    public ShortUrlProperties getShortUrl() {
        return shortUrl;
    }

    /**
     * 校验 MinIO 必填项；在创建客户端前调用。
     */
    public void validateMinioIfNeeded() {
        if (type != FileStorageType.minio) {
            return;
        }
        MinioProperties m = minio;
        if (!StringUtils.hasText(m.getEndpoint())
                || !StringUtils.hasText(m.getAccessKey())
                || !StringUtils.hasText(m.getSecretKey())
                || !StringUtils.hasText(m.getBucket())) {
            throw new IllegalStateException(
                    "qc.file.type=minio 时需配置 minio.endpoint、accessKey、secretKey、bucket");
        }
    }

    public static class ClassifyProperties {
        private String classify;
        /** 逗号分隔，带点或不带点均可，比对时归一为小写 */
        private String limitExt = "";
        /** 最大字节数，默认 10MB */
        private long limitSize = 10L * 1024 * 1024;

        public String getClassify() {
            return classify;
        }

        public void setClassify(String classify) {
            this.classify = classify;
        }

        public String getLimitExt() {
            return limitExt;
        }

        public void setLimitExt(String limitExt) {
            this.limitExt = limitExt;
        }

        public long getLimitSize() {
            return limitSize;
        }

        public void setLimitSize(long limitSize) {
            this.limitSize = limitSize;
        }
    }

    public static class LocalProperties {
        /** 空则使用 {@code java.io.tmpdir}/quickboot-uploads */
        private String path = "";

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }
    }

    public static class MinioProperties {
        private String endpoint = "";
        private String accessKey = "";
        private String secretKey = "";
        private String bucket = "";
        private boolean pathStyleAccess = true;

        public String getEndpoint() {
            return endpoint;
        }

        public void setEndpoint(String endpoint) {
            this.endpoint = endpoint;
        }

        public String getAccessKey() {
            return accessKey;
        }

        public void setAccessKey(String accessKey) {
            this.accessKey = accessKey;
        }

        public String getSecretKey() {
            return secretKey;
        }

        public void setSecretKey(String secretKey) {
            this.secretKey = secretKey;
        }

        public String getBucket() {
            return bucket;
        }

        public void setBucket(String bucket) {
            this.bucket = bucket;
        }

        public boolean isPathStyleAccess() {
            return pathStyleAccess;
        }

        public void setPathStyleAccess(boolean pathStyleAccess) {
            this.pathStyleAccess = pathStyleAccess;
        }
    }

    public static class ShortUrlProperties {
        private boolean enabled = false;
        private String pathPrefix = "";
        private String storage = "";

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getPathPrefix() {
            return pathPrefix;
        }

        public void setPathPrefix(String pathPrefix) {
            this.pathPrefix = pathPrefix;
        }

        public String getStorage() {
            return storage;
        }

        public void setStorage(String storage) {
            this.storage = storage;
        }
    }
}
