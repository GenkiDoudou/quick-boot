package io.github.genkidoudou.common.file.url;

import org.springframework.util.StringUtils;

import io.github.genkidoudou.common.file.QcFileProperties;

/**
 * {@link io.github.genkidoudou.common.file.FileTemplate#view} 与 {@link FileUrl} 共用的 domain 拼接/剥离。
 */
public final class FileUrlSupport {

  private FileUrlSupport() {
  }

  /**
   * 将相对路径解析为对外绝对 URL：优先 {@code qc.file.domain}，否则 {@code qc.file.viewUrlBase}。
   *
   * @param props        文件配置
   * @param relativePath 相对路径
   * @return 绝对 URL；无配置前缀时返回相对路径本身
   */
  public static String resolvePublicUrl(QcFileProperties props, String relativePath) {
    if (!StringUtils.hasText(relativePath)) {
      return relativePath;
    }
    String p = relativePath.trim();
    if (p.startsWith("http://") || p.startsWith("https://")) {
      return p;
    }
    if (props == null) {
      return p;
    }
    if (StringUtils.hasText(props.getDomain())) {
      return join(props.getDomain().trim(), p);
    }
    if (StringUtils.hasText(props.getViewUrlBase())) {
      return join(props.getViewUrlBase().trim(), p);
    }
    return p;
  }

  /**
   * 拼接 domain 与相对路径，自动处理 domain 尾斜杠。
   *
   * @param domain        访问域名或 viewUrlBase
   * @param relativePath  相对路径
   * @return 拼接后的 URL
   */
  public static String join(String domain, String relativePath) {
    if (!StringUtils.hasText(domain)) {
      return relativePath;
    }
    String d = domain.trim();
    while (d.endsWith("/")) {
      d = d.substring(0, d.length() - 1);
    }
    String p = StringUtils.hasText(relativePath) ? relativePath.trim() : "";
    return d + "/" + p;
  }

  /**
   * 若 value 以 domain 为前缀（忽略 domain 尾斜杠），返回相对路径段；否则返回原值。
   */
  public static String stripDomainIfPresent(String value, String domain) {
    if (!StringUtils.hasText(value) || !StringUtils.hasText(domain)) {
      return value;
    }
    String v = value.trim();
    String d = domain.trim();
    while (d.endsWith("/")) {
      d = d.substring(0, d.length() - 1);
    }
    if (v.startsWith(d + "/")) {
      return v.substring(d.length() + 1);
    }
    if (v.equals(d)) {
      return "";
    }
    return v;
  }
}
