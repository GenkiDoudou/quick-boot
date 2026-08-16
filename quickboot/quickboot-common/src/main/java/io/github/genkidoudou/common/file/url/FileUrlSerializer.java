package io.github.genkidoudou.common.file.url;

import org.springframework.util.StringUtils;

import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueSerializer;

import io.github.genkidoudou.common.file.QcFileProperties;

/**
 * {@link FileUrl} 字段序列化：{@code null} 写 JSON null；已是 http(s) 则原样；否则拼接 domain。
 */
public class FileUrlSerializer extends ValueSerializer<String> {

  private final String explicitDomain;
  private final QcFileProperties props;

  /**
   * @param explicitDomain 注解 {@link FileUrl#domain()}，非空时优先
   * @param props          全局文件配置
   */
  public FileUrlSerializer(String explicitDomain, QcFileProperties props) {
    this.explicitDomain = explicitDomain;
    this.props = props;
  }

  @Override
  public void serialize(String value, JsonGenerator gen, SerializationContext serializers) {
    if (value == null) {
      gen.writeNull();
      return;
    }
    if (isAbsoluteUrl(value)) {
      gen.writeString(value);
      return;
    }
    String domain = resolveDomain();
    if (!StringUtils.hasText(domain)) {
      gen.writeString(value);
      return;
    }
    gen.writeString(FileUrlSupport.join(domain, value));
  }

  private String resolveDomain() {
    if (StringUtils.hasText(explicitDomain)) {
      return explicitDomain.trim();
    }
    if (props == null) {
      return "";
    }
    if (StringUtils.hasText(props.getDomain())) {
      return props.getDomain().trim();
    }
    if (StringUtils.hasText(props.getViewUrlBase())) {
      return props.getViewUrlBase().trim();
    }
    return "";
  }

  static boolean isAbsoluteUrl(String s) {
    String t = s.trim().toLowerCase();
    return t.startsWith("http://") || t.startsWith("https://");
  }
}
