package io.github.genkidoudou.common.file.storage;

import io.github.genkidoudou.common.file.FileProperties;
import io.github.genkidoudou.common.file.FileProperties.FileClassifyProperties;
import io.github.genkidoudou.common.file.FileException;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 文件存储抽象基类
 * 提供分类校验、路径生成等通用逻辑
 *
 * @author genkidoudou
 * @since 2026/03/11
 */
public abstract class AbstractFileStorage {

    private static final DateTimeFormatter YEAR_MONTH = DateTimeFormatter.ofPattern("yyyy/MM");
    private static final String PATH_TRAVERSAL = "..";

    protected final FileProperties properties;

    protected AbstractFileStorage(FileProperties properties) {
        this.properties = properties;
    }

    /**
     * 校验并获取分类配置
     */
    protected FileClassifyProperties resolveClassify(String classify) {
        if (classify == null || classify.isBlank()) {
            classify = properties.getDefaultClassify();
        }
        String finalClassify = classify;
        var classifies = properties.getClassifies();
        Optional<FileClassifyProperties> opt = classifies.stream()
                .filter(c -> finalClassify.equals(c.getClassify()))
                .findFirst();
        if (opt.isEmpty()) {
            opt = classifies.stream()
                    .filter(c -> properties.getDefaultClassify().equals(c.getClassify()))
                    .findFirst();
        }
        if (opt.isEmpty()) {
            // 无配置时使用默认分类（通用后缀和 10MB 限制）
            FileClassifyProperties defaultConfig = new FileClassifyProperties();
            defaultConfig.setClassify(properties.getDefaultClassify());
            defaultConfig.setLimitExt("jpg,jpeg,png,gif,webp,pdf,doc,docx,xls,xlsx");
            defaultConfig.setLimitSize(10485760L);
            return defaultConfig;
        }
        return opt.get();
    }

    /**
     * 校验文件（MultipartFile）
     */
    protected void validateFile(MultipartFile file, String classify) {
        if (file == null || file.isEmpty()) {
            throw new FileException("文件不能为空");
        }
        FileClassifyProperties config = resolveClassify(classify);
        String ext = extractExtension(file.getOriginalFilename());
        validateExtension(ext, config);
        validateSize(file.getSize(), config);
    }

    /**
     * 校验文件（字节数组）
     */
    protected void validateFile(byte[] bytes, String filename, String classify) {
        if (bytes == null || bytes.length == 0) {
            throw new FileException("文件内容不能为空");
        }
        FileClassifyProperties config = resolveClassify(classify);
        String ext = extractExtension(filename);
        if (ext == null || ext.isEmpty()) {
            throw new FileException("无法从文件名提取扩展名: " + filename);
        }
        validateExtension(ext, config);
        validateSize((long) bytes.length, config);
    }

    private void validateExtension(String ext, FileClassifyProperties config) {
        if (ext == null || ext.isEmpty()) {
            throw new FileException("文件扩展名不能为空");
        }
        String limitExt = config.getLimitExt();
        if (limitExt == null || limitExt.isBlank()) {
            return;
        }
        Set<String> allowed = Arrays.stream(limitExt.split(","))
                .map(String::trim)
                .map(String::toLowerCase)
                .collect(Collectors.toSet());
        if (!allowed.contains(ext.toLowerCase())) {
            throw new FileException("不允许的文件类型: " + ext + "，允许的类型: " + limitExt);
        }
    }

    private void validateSize(long size, FileClassifyProperties config) {
        Long limit = config.getLimitSize();
        if (limit != null && size > limit) {
            throw new FileException("文件大小超过限制: " + size + " 字节，最大允许: " + limit + " 字节");
        }
    }

    /**
     * 生成相对路径：{classify}/{year}/{month}/{uuid}.{ext}
     */
    protected String generateRelativePath(String classify, String ext) {
        String yearMonth = LocalDate.now().format(YEAR_MONTH);
        String uuid = UUID.randomUUID().toString().replace("-", "");
        return classify + "/" + yearMonth + "/" + uuid + "." + ext;
    }

    /**
     * 从文件名提取扩展名
     */
    protected String extractExtension(String filename) {
        if (filename == null || filename.isBlank()) {
            return null;
        }
        int lastDot = filename.lastIndexOf('.');
        if (lastDot < 0 || lastDot == filename.length() - 1) {
            return null;
        }
        return filename.substring(lastDot + 1);
    }

    /**
     * 校验相对路径，防止路径穿越
     */
    protected void validateRelativePath(String relativePath) {
        if (relativePath == null || relativePath.isBlank()) {
            throw new FileException("相对路径不能为空");
        }
        if (relativePath.contains(PATH_TRAVERSAL)) {
            throw new FileException("非法路径: 禁止使用 ..");
        }
        if (relativePath.startsWith("/")) {
            throw new FileException("相对路径不能以 / 开头");
        }
    }

    /**
     * 拼接完整 URL
     */
    protected String buildFullUrl(String relativePath) {
        String domain = properties.getDomain();
        if (domain == null || domain.isBlank()) {
            return relativePath;
        }
        domain = domain.endsWith("/") ? domain.substring(0, domain.length() - 1) : domain;
        String path = relativePath.startsWith("/") ? relativePath : "/" + relativePath;
        return domain + path;
    }
}
