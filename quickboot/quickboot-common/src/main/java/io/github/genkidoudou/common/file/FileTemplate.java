package io.github.genkidoudou.common.file;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

/**
 * 统一文件门面：上传、下载、访问 URL、预签名、删除等；不暴露 Web Controller。
 */
public interface FileTemplate {

    String upload(MultipartFile file, String classify);

    String upload(byte[] content, String filename, String classify);

    Resource download(String relativePath);

    String view(String relativePath);

    String getShortUrl(String relativePath);

    String getPresignedUrl(String relativePath, int expireSeconds);

    void delete(String relativePath);

    boolean exists(String relativePath);
}
