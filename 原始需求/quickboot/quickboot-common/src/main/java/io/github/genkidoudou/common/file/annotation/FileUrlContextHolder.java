package io.github.genkidoudou.common.file.annotation;

/**
 * 文件 URL 上下文持有者
 * 用于 Jackson 序列化/反序列化时获取配置的域名
 *
 * @author genkidoudou
 * @since 2026/03/11
 */
public final class FileUrlContextHolder {

    private static volatile String domain;

    private FileUrlContextHolder() {
    }

    public static String getDomain() {
        return domain;
    }

    public static void setDomain(String domain) {
        FileUrlContextHolder.domain = domain;
    }
}
