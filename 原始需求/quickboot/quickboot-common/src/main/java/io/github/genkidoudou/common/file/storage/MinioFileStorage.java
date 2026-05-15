package io.github.genkidoudou.common.file.storage;

import io.github.genkidoudou.common.file.FileException;
import io.github.genkidoudou.common.file.FileProperties;
import io.github.genkidoudou.common.file.FileTemplate;
import io.minio.GetObjectArgs;
import io.minio.GetObjectResponse;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.StatObjectArgs;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.http.Method;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.time.Duration;

/**
 * MinIO 文件存储实现
 *
 * @author genkidoudou
 * @since 2026/03/11
 */
@Slf4j
public class MinioFileStorage extends AbstractFileStorage implements FileTemplate {

    private final MinioClient minioClient;
    private final String bucket;

    public MinioFileStorage(FileProperties properties, MinioClient minioClient) {
        super(properties);
        this.minioClient = minioClient;
        this.bucket = properties.getMinio().getBucket();
    }

    @Override
    public String upload(MultipartFile file, String classify) {
        validateFile(file, classify);
        String ext = extractExtension(file.getOriginalFilename());
        String relativePath = generateRelativePath(classify, ext);
        try {
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucket)
                    .object(relativePath)
                    .stream(file.getInputStream(), file.getSize(), -1)
                    .contentType(file.getContentType())
                    .build());
            return relativePath;
        } catch (Exception e) {
            throw new FileException("MinIO 文件上传失败: " + e.getMessage(), e);
        }
    }

    @Override
    public String upload(byte[] bytes, String filename, String classify) {
        validateFile(bytes, filename, classify);
        String ext = extractExtension(filename);
        String relativePath = generateRelativePath(classify, ext);
        try {
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucket)
                    .object(relativePath)
                    .stream(new java.io.ByteArrayInputStream(bytes), bytes.length, -1)
                    .build());
            return relativePath;
        } catch (Exception e) {
            throw new FileException("MinIO 文件上传失败: " + e.getMessage(), e);
        }
    }

    @Override
    public String upload(MultipartFile file) {
        return upload(file, properties.getDefaultClassify());
    }

    @Override
    public Resource download(String relativePath) {
        validateRelativePath(relativePath);
        try {
            GetObjectResponse response = minioClient.getObject(GetObjectArgs.builder()
                    .bucket(bucket)
                    .object(relativePath)
                    .build());
            return new InputStreamResource(response) {
                @Override
                public String getFilename() {
                    int lastSlash = relativePath.lastIndexOf('/');
                    return lastSlash >= 0 ? relativePath.substring(lastSlash + 1) : relativePath;
                }
            };
        } catch (Exception e) {
            throw new FileException("MinIO 文件下载失败: " + relativePath + ", " + e.getMessage(), e);
        }
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
        return view(relativePath);
    }

    @Override
    public String getPresignedUrl(String relativePath, long expireSeconds) {
        validateRelativePath(relativePath);
        try {
            return minioClient.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
                    .method(Method.GET)
                    .bucket(bucket)
                    .object(relativePath)
                    .expiry((int) expireSeconds)
                    .build());
        } catch (Exception e) {
            throw new FileException("获取 MinIO 临时 URL 失败: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean delete(String relativePath) {
        validateRelativePath(relativePath);
        try {
            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(bucket)
                    .object(relativePath)
                    .build());
            return true;
        } catch (Exception e) {
            log.warn("MinIO 删除文件失败: {}", relativePath, e);
            return false;
        }
    }

    @Override
    public boolean exists(String relativePath) {
        if (relativePath == null || relativePath.isBlank()) {
            return false;
        }
        try {
            minioClient.statObject(StatObjectArgs.builder()
                    .bucket(bucket)
                    .object(relativePath)
                    .build());
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
