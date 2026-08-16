package io.github.genkidoudou.common.file;

import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;

import tools.jackson.databind.AnnotationIntrospector;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.introspect.JacksonAnnotationIntrospector;

import io.github.genkidoudou.common.file.url.FileUrlAnnotationIntrospector;

/**
 * 文件存储自动配置：本地 {@link FileTemplate}、{@link FileAccessService}、Jackson {@code @FileUrl}。
 */
@AutoConfiguration
@EnableConfigurationProperties(QcFileProperties.class)
public class FileStorageAutoConfiguration {

  @Bean
  @ConditionalOnMissingBean(FileClassifyRuleResolver.class)
  public FileClassifyRuleResolver emptyFileClassifyRuleResolver() {
    return new EmptyFileClassifyRuleResolver();
  }

  @Bean
  @ConditionalOnProperty(prefix = "qc.file", name = "enabled", havingValue = "false")
  public FileTemplate disabledFileTemplate() {
    return new DisabledFileTemplate();
  }

  @Bean
  @ConditionalOnProperty(prefix = "qc.file", name = "enabled", havingValue = "true", matchIfMissing = true)
  public FileTemplate fileTemplate(
      QcFileProperties props,
      FileClassifyRuleResolver classifyResolver,
      ObjectProvider<FileUploadHook> hookProvider) {
    List<FileUploadHook> hooks = new ArrayList<>();
    hookProvider.forEach(hooks::add);
    FileStorageOperations ops = createLocalStorage(props);
    return new DefaultFileTemplate(props, ops, classifyResolver, hooks);
  }

  @Bean
  @ConditionalOnMissingBean(FileAccessService.class)
  public FileAccessService fileAccessService(
      QcFileProperties props,
      FileTemplate fileTemplate,
      FileClassifyRuleResolver classifyResolver) {
    return new FileAccessService(props, fileTemplate, classifyResolver);
  }

  @Bean
  @ConditionalOnClass(ObjectMapper.class)
  public JsonMapperBuilderCustomizer fileUrlJacksonCustomizer(QcFileProperties props) {
    AnnotationIntrospector primary = new FileUrlAnnotationIntrospector(props);
    AnnotationIntrospector fallback = new JacksonAnnotationIntrospector();
    return builder -> builder.annotationIntrospector(AnnotationIntrospector.pair(primary, fallback));
  }

  private static FileStorageOperations createLocalStorage(QcFileProperties props) {
    if (props.getType() == FileStorageType.minio) {
      throw new IllegalStateException(
          "qc.file.type=minio 本期不支持；请使用 qc.file.type=local");
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
