package io.github.genkidoudou.common.file.url;

import org.springframework.lang.Nullable;

import com.fasterxml.jackson.annotation.JsonInclude;
import tools.jackson.databind.cfg.MapperConfig;
import tools.jackson.databind.introspect.Annotated;
import tools.jackson.databind.introspect.AnnotatedMethod;
import tools.jackson.databind.introspect.JacksonAnnotationIntrospector;

import io.github.genkidoudou.common.file.QcFileProperties;

/**
 * 识别 {@link FileUrl}，注册专用序列化/反序列化并强制保留 null。
 */
public class FileUrlAnnotationIntrospector extends JacksonAnnotationIntrospector {

  private final QcFileProperties props;

  /**
   * @param props 文件模块配置，用于 domain / viewUrlBase 回退
   */
  public FileUrlAnnotationIntrospector(@Nullable QcFileProperties props) {
    this.props = props;
  }

  @Override
  public Object findSerializer(MapperConfig<?> config, Annotated am) {
    FileUrl ann = resolveFileUrl(am);
    if (ann != null) {
      return new FileUrlSerializer(ann.domain(), props);
    }
    return super.findSerializer(config, am);
  }

  @Override
  public Object findDeserializer(MapperConfig<?> config, Annotated am) {
    FileUrl ann = resolveFileUrl(am);
    if (ann != null) {
      return new FileUrlDeserializer(ann.domain(), props);
    }
    return super.findDeserializer(config, am);
  }

  @Override
  public JsonInclude.Value findPropertyInclusion(MapperConfig<?> config, Annotated a) {
    if (resolveFileUrl(a) != null) {
      return JsonInclude.Value.construct(JsonInclude.Include.ALWAYS, JsonInclude.Include.ALWAYS);
    }
    return super.findPropertyInclusion(config, a);
  }

  /**
   * Lombok {@code @Data} 生成 getter 时，Jackson 常从方法序列化，须回退到同名字段上的 {@link FileUrl}。
   */
  private FileUrl resolveFileUrl(Annotated am) {
    FileUrl direct = am.getAnnotation(FileUrl.class);
    if (direct != null) {
      return direct;
    }
    if (am instanceof AnnotatedMethod method && method.getMember() != null) {
      String fieldName = propertyNameFromAccessor(method.getName());
      if (fieldName != null) {
        try {
          java.lang.reflect.Field reflectField =
              method.getMember().getDeclaringClass().getDeclaredField(fieldName);
          return reflectField.getAnnotation(FileUrl.class);
        } catch (NoSuchFieldException ignored) {
          // ignore
        }
      }
    }
    return null;
  }

  private static String propertyNameFromAccessor(String methodName) {
    if (methodName == null || methodName.length() < 4) {
      return null;
    }
    if (methodName.startsWith("get") && methodName.length() > 3) {
      return Character.toLowerCase(methodName.charAt(3)) + methodName.substring(4);
    }
    if (methodName.startsWith("set") && methodName.length() > 3) {
      return Character.toLowerCase(methodName.charAt(3)) + methodName.substring(4);
    }
    if (methodName.startsWith("is") && methodName.length() > 2) {
      return Character.toLowerCase(methodName.charAt(2)) + methodName.substring(3);
    }
    return null;
  }
}
