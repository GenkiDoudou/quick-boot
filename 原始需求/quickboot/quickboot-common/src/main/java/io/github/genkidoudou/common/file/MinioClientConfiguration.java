package io.github.genkidoudou.common.file;

import io.github.genkidoudou.common.file.annotation.FileUrlContextHolder;
import io.github.genkidoudou.common.file.storage.MinioFileStorage;
import io.minio.MinioClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MinIO 客户端配置
 * 当 qc.file.type=minio 时创建 MinioClient
 *
 * @author genkidoudou
 * @since 2026/03/11
 */
@Slf4j
@Configuration
@ConditionalOnClass(MinioClient.class)
@ConditionalOnProperty(prefix = "qc.file", name = "type", havingValue = "minio")
public class MinioClientConfiguration {
    @Bean
    public FileTemplate minioFileTemplate(FileProperties properties, MinioClient minioClient) {
        log.info("初始化 MinIO 文件存储: bucket={}", properties.getMinio().getBucket());
        FileUrlContextHolder.setDomain(properties.getDomain());
        return new MinioFileStorage(properties, minioClient);
    }

    @Bean
    public MinioClient minioClient(FileProperties properties) {
        FileProperties.MinioProperties minio = properties.getMinio();
        return MinioClient.builder()
                .endpoint(minio.getEndpoint())
                .credentials(minio.getAccessKey(), minio.getSecretKey())
                .build();
    }
}
