package io.github.genkidoudou.common.file.url;

import org.springframework.util.StringUtils;

/**
 * {@link io.github.genkidoudou.common.file.FileTemplate#view} 与 {@link FileUrl} 共用的 domain 拼接/剥离。
 */
public final class FileUrlSupport {

    private FileUrlSupport() {
    }

    public static String join(String domain, String relativePath) {
        if (!StringUtils.hasText(domain)) {
            return relativePath;
        }
        String d = domain.trim();
        while (d.endsWith("/")) {
            d = d.substring(0, d.length() - 1);
        }
        String p = StringUtils.hasText(relativePath) ? relativePath.trim() : "";
        return d + "/" + p;
    }

    /**
     * 若 value 以 domain 为前缀（忽略 domain 尾斜杠），返回相对路径段；否则返回原值。
     */
    public static String stripDomainIfPresent(String value, String domain) {
        if (!StringUtils.hasText(value) || !StringUtils.hasText(domain)) {
            return value;
        }
        String v = value.trim();
        String d = domain.trim();
        while (d.endsWith("/")) {
            d = d.substring(0, d.length() - 1);
        }
        if (v.startsWith(d + "/")) {
            return v.substring(d.length() + 1);
        }
        if (v.equals(d)) {
            return "";
        }
        return v;
    }
}
