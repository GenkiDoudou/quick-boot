package io.github.genkidoudou.common.file;

import lombok.Data;

/**
 * 上传分类配置（对外展示）。压缩参数优先取分类表字段，空则回退 {@code qc.file.compress}。
 */
@Data
public class FileClassifyVo {

  /** 分类名（上传时 classify 参数）。 */
  private String classify;

  /** 展示名。 */
  private String classifyName;

  /** 允许扩展名，逗号分隔（如 png,jpg,pdf）；空表示使用内置默认白名单。 */
  private String limitExt;

  /** 大小上限字节数（前端校验用）。 */
  private long limitSizeBytes;

  /** 单次上传允许的最大文件个数。 */
  private int limitCount;

  /**
   * 分类是否开启压缩：{@code 0}/{@code 1}。为 1 时前端按下方参数压缩；服务端另受 {@code qc.file.compress.enabled} 控制。
   */
  private String compressEnabled;

  /** 超过该 KB 才压缩。 */
  private int compressMinSizeKb = 200;

  /** JPEG 质量 0–1。 */
  private float compressQuality = 0.85f;

  /** 最长边像素。 */
  private int compressMaxEdge = 1920;

  /** 该分类上传是否允许匿名（未登录）。 */
  private boolean anonymous;

  /** 状态：{@code "0"} 正常 / {@code "1"} 停用。 */
  private String status;

  /**
   * 由分类规则构造 VO（压缩参数回退全局默认）。
   *
   * @param rule 分类规则
   * @return VO；{@code rule} 为 null 时返回空 VO
   */
  public static FileClassifyVo from(FileClassifyRule rule) {
    return from(rule, null);
  }

  /**
   * 由分类规则构造 VO，压缩参数可指定回退配置。
   *
   * @param rule     分类规则
   * @param compress 压缩默认配置，可 null
   * @return VO
   */
  public static FileClassifyVo from(FileClassifyRule rule, QcFileProperties.CompressProperties compress) {
    FileClassifyVo vo = new FileClassifyVo();
    if (rule == null) {
      return vo;
    }
    vo.setClassify(rule.resolveClassifyKey());
    vo.setClassifyName(rule.getClassifyName());
    vo.setLimitExt(rule.getLimitExt());
    vo.setLimitSizeBytes(rule.resolveLimitSizeBytes());
    vo.setLimitCount(rule.resolveLimitCount());
    vo.setCompressEnabled(rule.getCompressEnabled());
    vo.setAnonymous(rule.isAnonymous());
    vo.setStatus(rule.getStatus());
    QcFileProperties.CompressProperties cfg =
        compress != null ? compress : new QcFileProperties.CompressProperties();
    vo.setCompressMinSizeKb(rule.resolveCompressMinSizeKb(cfg));
    vo.setCompressQuality(rule.resolveCompressQuality(cfg));
    vo.setCompressMaxEdge(rule.resolveCompressMaxEdge(cfg));
    return vo;
  }
}
