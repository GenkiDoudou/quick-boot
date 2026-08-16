package io.github.genkidoudou.common.file;

import lombok.Data;
import org.springframework.util.StringUtils;

/**
 * 运行时分类规则（由 {@link FileClassifyRuleResolver} 从 DB 等来源解析）。
 */
@Data
public class FileClassifyRule {

  /** 分类键（上传参数 classify）。 */
  private String classify;

  /** 展示名（可选）。 */
  private String classifyName;

  /** 允许扩展名，逗号分隔；空表示使用内置默认白名单。 */
  private String limitExt;

  /** 单文件大小上限（字节）。 */
  private long limitSizeBytes = 10L * 1024 * 1024;

  /** 单次上传允许的最大文件个数。 */
  private int limitCount = 1;

  /**
   * 是否启用图片压缩：{@code "0"}/{@code "1"}；为 1 时上传链路对 jpg/png/bmp 等比缩小并重编码。
   */
  private String compressEnabled = "0";

  /** 超过该 KB 才压缩；null 表示回退 YAML / 默认。 */
  private Integer compressMinSizeKb;

  /** JPEG 质量 0.1–1.0；null 表示回退 YAML / 默认。 */
  private Float compressQuality;

  /** 最长边像素；null 表示回退 YAML / 默认；0 表示不限制。 */
  private Integer compressMaxEdge;

  /** 该分类上传是否允许匿名（未登录）。 */
  private boolean anonymous;

  /**
   * 状态：{@code "0"} 正常 / {@code "1"} 停用。
   */
  private String status = "0";

  /** 是否可上传（存在且 status=0）。 */
  public boolean isEnabled() {
    return "0".equals(status);
  }

  public boolean isCompressEnabledFlag() {
    return "1".equals(compressEnabled);
  }

  public long resolveLimitSizeBytes() {
    return limitSizeBytes > 0 ? limitSizeBytes : 10L * 1024 * 1024;
  }

  public int resolveLimitCount() {
    return limitCount > 0 ? limitCount : 1;
  }

  public String resolveClassifyKey() {
    return StringUtils.hasText(classify) ? classify.trim() : "";
  }

  /**
   * 分类压缩阈值（KB）；非法或空时回退 YAML / 200。
   */
  public int resolveCompressMinSizeKb(QcFileProperties.CompressProperties fallback) {
    if (compressMinSizeKb != null && compressMinSizeKb > 0) {
      return compressMinSizeKb;
    }
    return fallback != null ? fallback.resolveMinSizeKb() : 200;
  }

  /**
   * 分类 JPEG 质量；非法或空时回退 YAML / 0.85。
   */
  public float resolveCompressQuality(QcFileProperties.CompressProperties fallback) {
    if (compressQuality != null) {
      float q = compressQuality;
      if (q < 0.1f) {
        return 0.1f;
      }
      if (q > 1.0f) {
        return 1.0f;
      }
      return q;
    }
    return fallback != null ? fallback.resolveQuality() : 0.85f;
  }

  /**
   * 分类最长边；null 时回退 YAML / 1920；显式 0 表示不限制。
   */
  public int resolveCompressMaxEdge(QcFileProperties.CompressProperties fallback) {
    if (compressMaxEdge != null) {
      return Math.max(0, compressMaxEdge);
    }
    return fallback != null ? fallback.resolveMaxEdge() : 1920;
  }
}
