package io.github.genkidoudou.common.file;

import io.github.genkidoudou.common.file.annotation.FileUrlContextHolder;
import io.github.genkidoudou.common.file.aspect.FileUploadAspect;
import io.github.genkidoudou.common.file.storage.LocalFileStorage;
import io.github.genkidoudou.common.file.storage.MinioFileStorage;
import io.minio.MinioClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 文件上传模块自动配置
 *
 * @author genkidoudou
 * @since 2026/03/11
 */
@Slf4j
@Configuration
@EnableConfigurationProperties(FileProperties.class)
@ConditionalOnProperty(prefix = "qc.file", name = "enabled", havingValue = "true", matchIfMissing = true)
public class FileConfiguration {




    @Bean
    @ConditionalOnProperty(prefix = "qc.file", name = "type", havingValue = "local", matchIfMissing = true)
    public FileTemplate localFileTemplate(FileProperties properties) {
        log.info("初始化本地文件存储: {}", properties.getLocal().getPath());
        FileUrlContextHolder.setDomain(properties.getDomain());
        return new LocalFileStorage(properties);
    }

    @Bean
    @ConditionalOnBean(FileTemplate.class)
    public FileUploadAspect fileUploadAspect(FileProperties fileProperties,
                                             @Autowired(required = false) java.util.List<FileUploadHook> hooks) {
        return new FileUploadAspect(fileProperties, hooks);
    }
}
