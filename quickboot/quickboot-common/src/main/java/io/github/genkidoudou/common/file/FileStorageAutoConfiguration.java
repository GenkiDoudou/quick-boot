package io.github.genkidoudou.common.file;

import java.io.IOException;
import java.nio.file.Files;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;

import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;

import io.github.genkidoudou.common.file.url.FileUrlAnnotationIntrospector;

/**
 * 文件存储自动配置：{@link FileTemplate}、 Jackson {@link io.github.genkidoudou.common.file.url.FileUrl} 模块。
 */
@AutoConfiguration
@EnableConfigurationProperties(QcFileProperties.class)
public class FileStorageAutoConfiguration {

    @Bean
    @ConditionalOnProperty(prefix = "qc.file", name = "enabled", havingValue = "false")
    public FileTemplate disabledFileTemplate() {
        return new DisabledFileTemplate();
    }

    @Bean
    @ConditionalOnProperty(prefix = "qc.file", name = "enabled", havingValue = "true", matchIfMissing = true)
    public FileTemplate fileTemplate(QcFileProperties props, ObjectProvider<FileUploadHook> hookProvider) {
        java.util.List<FileUploadHook> hooks = new java.util.ArrayList<>();
        hookProvider.forEach(hooks::add);
        FileStorageOperations ops = createStorage(props);
        return new DefaultFileTemplate(props, ops, hooks);
    }

    @Bean
    @ConditionalOnClass(ObjectMapper.class)
    public Jackson2ObjectMapperBuilderCustomizer fileUrlJacksonCustomizer(QcFileProperties props) {
        return builder -> builder.postConfigurer(objectMapper -> {
            AnnotationIntrospector current = objectMapper.getSerializationConfig().getAnnotationIntrospector();
            if (current == null) {
                current = new JacksonAnnotationIntrospector();
            }
            AnnotationIntrospector primary = new FileUrlAnnotationIntrospector(props);
            objectMapper.setAnnotationIntrospector(AnnotationIntrospector.pair(primary, current));
        });
    }

    private static FileStorageOperations createStorage(QcFileProperties props) {
        if (props.getType() == FileStorageType.minio) {
            props.validateMinioIfNeeded();
            return new MinioFileStorageBackend(props);
        }
        java.nio.file.Path root = FilePathSupport.requireRootDirectory(props);
        try {
            Files.createDirectories(root);
        } catch (IOException e) {
            throw new IllegalStateException("无法创建本地存储根目录: " + root, e);
        }
        return new LocalFileStorageBackend(root);
    }
}
