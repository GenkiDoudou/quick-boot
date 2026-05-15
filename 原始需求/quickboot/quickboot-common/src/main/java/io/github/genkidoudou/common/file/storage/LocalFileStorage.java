package io.github.genkidoudou.common.file.storage;

import io.github.genkidoudou.common.file.FileException;
import io.github.genkidoudou.common.file.FileProperties;
import io.github.genkidoudou.common.file.FileTemplate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 本地文件存储实现
 *
 * @author genkidoudou
 * @since 2026/03/11
 */
@Slf4j
public class LocalFileStorage extends AbstractFileStorage implements FileTemplate {

    private final Path basePath;

    public LocalFileStorage(FileProperties properties) {
        super(properties);
        String path = properties.getLocal().getPath();
        if (path == null || path.isBlank()) {
            path = System.getProperty("java.io.tmpdir") + "/uploads";
        }
        this.basePath = Paths.get(path);
        ensureBasePathExists();
    }

    private void ensureBasePathExists() {
        try {
            if (!Files.exists(basePath)) {
                Files.createDirectories(basePath);
                log.info("创建本地存储目录: {}", basePath.toAbsolutePath());
            }
        } catch (IOException e) {
            throw new FileException("无法创建存储目录: " + basePath, e);
        }
    }

    @Override
    public String upload(MultipartFile file, String classify) {
        validateFile(file, classify);
        String ext = extractExtension(file.getOriginalFilename());
        String relativePath = generateRelativePath(classify, ext);
        Path targetPath = basePath.resolve(relativePath);
        try {
            Files.createDirectories(targetPath.getParent());
            file.transferTo(targetPath.toFile());
            return relativePath;
        } catch (IOException e) {
            throw new FileException("文件上传失败: " + e.getMessage(), e);
        }
    }

    @Override
    public String upload(byte[] bytes, String filename, String classify) {
        validateFile(bytes, filename, classify);
        String ext = extractExtension(filename);
        String relativePath = generateRelativePath(classify, ext);
        Path targetPath = basePath.resolve(relativePath);
        try {
            Files.createDirectories(targetPath.getParent());
            Files.write(targetPath, bytes);
            return relativePath;
        } catch (IOException e) {
            throw new FileException("文件上传失败: " + e.getMessage(), e);
        }
    }

    @Override
    public String upload(MultipartFile file) {
        return upload(file, properties.getDefaultClassify());
    }

    @Override
    public Resource download(String relativePath) {
        validateRelativePath(relativePath);
        Path filePath = basePath.resolve(relativePath);
        if (!Files.exists(filePath) || !Files.isRegularFile(filePath)) {
            throw new FileException("文件不存在: " + relativePath);
        }
        return new FileSystemResource(filePath.toFile());
    }

    @Override
    public String view(String relativePath) {
        validateRelativePath(relativePath);
        return buildFullUrl(relativePath);
    }

    @Override
    public String getShortUrl(String relativePath) {
        validateRelativePath(relativePath);
        if (!properties.getShortUrl().getEnabled()) {
            return view(relativePath);
        }
        // 简化实现：短链未启用时返回完整 URL；完整短链需配合 ShortUrlService
        return view(relativePath);
    }

    @Override
    public String getPresignedUrl(String relativePath, long expireSeconds) {
        validateRelativePath(relativePath);
        // 本地存储：返回完整 URL（可后续扩展为带 token 的临时接口）
        return view(relativePath);
    }

    @Override
    public boolean delete(String relativePath) {
        validateRelativePath(relativePath);
        try {
            Path filePath = basePath.resolve(relativePath);
            return Files.deleteIfExists(filePath);
        } catch (IOException e) {
            log.warn("删除文件失败: {}", relativePath, e);
            return false;
        }
    }

    @Override
    public boolean exists(String relativePath) {
        if (relativePath == null || relativePath.isBlank()) {
            return false;
        }
        Path filePath = basePath.resolve(relativePath);
        return Files.exists(filePath) && Files.isRegularFile(filePath);
    }
}
