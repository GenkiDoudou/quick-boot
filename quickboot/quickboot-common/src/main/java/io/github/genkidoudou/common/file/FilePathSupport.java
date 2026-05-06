package io.github.genkidoudou.common.file;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.util.StringUtils;

/**
 * 相对路径生成、扩展名小写归一、分类白名单与安全校验。
 */
public final class FilePathSupport {

    private static final DateTimeFormatter YEAR_MONTH = DateTimeFormatter.ofPattern("yyyy/MM");

    /** 未配置分类时的通用后缀白名单（小写，无点） */
    public static final Set<String> DEFAULT_ALLOWED_EXT = Set.of(
            "jpg", "jpeg", "png", "gif", "webp",
            "pdf",
            "doc", "docx", "xls", "xlsx", "ppt", "pptx",
            "txt", "csv");

    private FilePathSupport() {
    }

    /**
     * 规范化存储用分类段：非空、无路径分隔、无 {@code ..}。
     */
    public static String normalizeClassifyKey(String classify, String defaultClassify) {
        String c = StringUtils.hasText(classify) ? classify.trim() : defaultClassify;
        if (!StringUtils.hasText(c)) {
            throw new FileStorageException("分类 classify 不能为空");
        }
        if (c.contains("/") || c.contains("\\") || c.contains("..")) {
            throw new FileStorageException("非法分类名: " + classify);
        }
        return c;
    }

    /**
     * 从原始文件名解析扩展名（不含点），若无则返回空串。
     */
    public static String normalizeExtension(String originalFilename) {
        if (!StringUtils.hasText(originalFilename)) {
            return "";
        }
        String name = originalFilename.trim();
        int dot = name.lastIndexOf('.');
        if (dot < 0 || dot == name.length() - 1) {
            return "";
        }
        return name.substring(dot + 1).toLowerCase(Locale.ROOT);
    }

    public static String buildRelativePath(String classifyKey, String extLower) {
        String ym = LocalDate.now().format(YEAR_MONTH);
        String uuid = UUID.randomUUID().toString();
        String suffix = StringUtils.hasText(extLower) ? "." + extLower : "";
        return classifyKey + "/" + ym + "/" + uuid + suffix;
    }

    /**
     * 解析配置的 limitExt 字符串为不带点的小写集合。
     */
    public static Set<String> parseLimitExt(String limitExtCsv) {
        if (!StringUtils.hasText(limitExtCsv)) {
            return Set.of();
        }
        return Arrays.stream(limitExtCsv.split(","))
                .map(String::trim)
                .filter(StringUtils::hasText)
                .map(s -> s.startsWith(".") ? s.substring(1) : s)
                .map(s -> s.toLowerCase(Locale.ROOT))
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    /**
     * 根据分类配置校验后缀与大小；{@code rule == null} 时使用内置默认白名单与 10MB。
     */
    public static void validateAgainstRule(String extLower, long size, QcFileProperties.ClassifyProperties rule) {
        Set<String> allowed;
        long max;
        if (rule != null && StringUtils.hasText(rule.getLimitExt())) {
            allowed = parseLimitExt(rule.getLimitExt());
            if (allowed.isEmpty()) {
                allowed = DEFAULT_ALLOWED_EXT;
            }
        } else {
            allowed = DEFAULT_ALLOWED_EXT;
        }
        if (rule != null && rule.getLimitSize() > 0) {
            max = rule.getLimitSize();
        } else {
            max = 10L * 1024 * 1024;
        }
        if (!allowed.contains(extLower)) {
            if (extLower.isEmpty()) {
                throw new FileStorageException("不允许的文件后缀: （空扩展名）");
            }
            throw new FileStorageException("不允许的文件后缀: ." + extLower);
        }
        if (size > max) {
            throw new FileStorageException("文件超过大小限制: 最大 " + max + " 字节");
        }
    }

    /**
     * 校验持久化键：禁止 {@code ..}、禁止前导 {@code /}、禁止反斜杠。
     */
    public static void validateRelativePath(String relativePath) {
        if (!StringUtils.hasText(relativePath)) {
            throw new FileStorageException("相对路径不能为空");
        }
        String p = relativePath.trim();
        if (p.startsWith("/") || p.startsWith("\\") || p.contains("..")) {
            throw new FileStorageException("非法相对路径");
        }
        if (p.chars().anyMatch(ch -> ch == '\0')) {
            throw new FileStorageException("非法相对路径");
        }
    }

    /**
     * 解析后的路径必须仍在 {@code root} 之下（防止穿越）。
     */
    public static Path resolveUnderRoot(Path root, String relativePath) {
        validateRelativePath(relativePath);
        Path base = root.toAbsolutePath().normalize();
        Path target = base.resolve(relativePath).normalize();
        if (!target.startsWith(base)) {
            throw new FileStorageException("路径逾越存储根目录");
        }
        return target;
    }

    public static Path requireRootDirectory(QcFileProperties props) {
        String configured = props.getLocal().getPath();
        Path root;
        if (StringUtils.hasText(configured)) {
            root = Paths.get(configured);
        } else {
            root = Paths.get(System.getProperty("java.io.tmpdir"), "quickboot-uploads");
        }
        return root.toAbsolutePath().normalize();
    }
}
