package io.github.genkidoudou.common.file;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * {@code qc.file.*} 配置绑定：本地存储基础项、域名与 {@link io.github.genkidoudou.common.file.url.FileUrl}。
 * <p>
 * 分类规则不以本配置列表为权威来源，请通过 {@link FileClassifyRuleResolver} 读取 DB。
 */
@ConfigurationProperties(prefix = "qc.file")
public class QcFileProperties {

  /**
   * 是否启用文件存储能力；为 false 时注入 {@link DisabledFileTemplate}。
   */
  private boolean enabled = true;

  private FileStorageType type = FileStorageType.local;

  /**
   * 对外访问域名，用于 {@code view} 与 {@link io.github.genkidoudou.common.file.url.FileUrl}
   * （如 {@code https://cdn.example.com}，无尾斜杠亦可）。
   */
  private String domain = "";

  /**
   * {@link io.github.genkidoudou.common.file.url.FileUrl} 在 {@link #domain} 为空时的回退前缀
   * （如管理端预览：{@code http://localhost:8800/dev-api/system/file/view}）。
   */
  private String viewUrlBase = "";

  private String defaultClassify = "default";

  private final LocalProperties local = new LocalProperties();

  private final CompressProperties compress = new CompressProperties();

  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  public FileStorageType getType() {
    return type;
  }

  public void setType(FileStorageType type) {
    this.type = type;
  }

  public String getDomain() {
    return domain;
  }

  public void setDomain(String domain) {
    this.domain = domain;
  }

  public String getViewUrlBase() {
    return viewUrlBase;
  }

  public void setViewUrlBase(String viewUrlBase) {
    this.viewUrlBase = viewUrlBase;
  }

  public String getDefaultClassify() {
    return defaultClassify;
  }

  public void setDefaultClassify(String defaultClassify) {
    this.defaultClassify = defaultClassify;
  }

  public LocalProperties getLocal() {
    return local;
  }

  public CompressProperties getCompress() {
    return compress;
  }

  public static class LocalProperties {
    /** 空则使用 {@code java.io.tmpdir}/quickboot-uploads */
    private String path = "";

    public String getPath() {
      return path;
    }

    public void setPath(String path) {
      this.path = path;
    }
  }

  /**
   * 图片压缩参数。
   * <p>
   * {@code min-size-kb}/{@code quality}/{@code max-edge} 作为分类未配置时的默认值；
   * 分类表字段优先。{@link #enabled} 仍为服务端压缩总开关（前端压缩为主时可关闭）。
   */
  public static class CompressProperties {
    /**
     * 是否启用服务端压缩；默认 false（前端压缩为主时可关闭服务端）。
     */
    private boolean enabled = false;
    /** 超过该大小（KB）才压缩；默认 200。 */
    private int minSizeKb = 200;
    /** JPEG 质量 0.1–1.0，默认 0.85。 */
    private float quality = 0.85f;
    /** 最长边像素；超出则等比缩小。0 表示不限制边长。默认 1920。 */
    private int maxEdge = 1920;

    public boolean isEnabled() {
      return enabled;
    }

    public void setEnabled(boolean enabled) {
      this.enabled = enabled;
    }

    public int getMinSizeKb() {
      return minSizeKb;
    }

    public void setMinSizeKb(int minSizeKb) {
      this.minSizeKb = minSizeKb;
    }

    public float getQuality() {
      return quality;
    }

    public void setQuality(float quality) {
      this.quality = quality;
    }

    public int getMaxEdge() {
      return maxEdge;
    }

    public void setMaxEdge(int maxEdge) {
      this.maxEdge = maxEdge;
    }

    public float resolveQuality() {
      if (quality < 0.1f) {
        return 0.1f;
      }
      if (quality > 1.0f) {
        return 1.0f;
      }
      return quality;
    }

    public int resolveMaxEdge() {
      return Math.max(0, maxEdge);
    }

    public int resolveMinSizeKb() {
      return Math.max(0, minSizeKb);
    }

    /** 低于该字节数不压缩。 */
    public long resolveMinSizeBytes() {
      return resolveMinSizeKb() * 1024L;
    }
  }
}
