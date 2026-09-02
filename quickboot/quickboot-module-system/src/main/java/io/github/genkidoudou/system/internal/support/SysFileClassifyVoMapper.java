package io.github.genkidoudou.system.internal.support;

import io.github.genkidoudou.common.file.FileClassifyRule;
import io.github.genkidoudou.common.file.QcFileProperties;
import io.github.genkidoudou.system.internal.vo.SysFileClassifyVo;

/**
 * 将 common 层 {@link FileClassifyRule} 映射为对外 {@link SysFileClassifyVo}（上传分类查询 API 专用）。
 */
public final class SysFileClassifyVoMapper {

  private SysFileClassifyVoMapper() {
  }

  /**
   * 由运行时分类规则构造上传侧 Vo；压缩参数取规则解析值（含 YAML 回退）。
   *
   * @param rule     分类规则，null 时返回空 Vo
   * @param compress 全局压缩默认配置，可 null
   * @return 供 {@code /file/classifies} 使用的 Vo
   */
  public static SysFileClassifyVo fromUploadRule(FileClassifyRule rule, QcFileProperties.CompressProperties compress) {
    SysFileClassifyVo vo = new SysFileClassifyVo();
    if (rule == null) {
      return vo;
    }
    QcFileProperties.CompressProperties cfg =
        compress != null ? compress : new QcFileProperties.CompressProperties();
    vo.setClassify(rule.resolveClassifyKey());
    vo.setClassifyName(rule.getClassifyName());
    vo.setLimitExt(rule.getLimitExt());
    vo.setLimitSizeBytes(rule.resolveLimitSizeBytes());
    vo.setLimitCount(rule.resolveLimitCount());
    vo.setCompressEnabled(rule.getCompressEnabled());
    vo.setCompressMinSizeKb(rule.resolveCompressMinSizeKb(cfg));
    vo.setCompressQuality(rule.resolveCompressQuality(cfg));
    vo.setCompressMaxEdge(rule.resolveCompressMaxEdge(cfg));
    vo.setAnonymous(rule.isAnonymous() ? "1" : "0");
    vo.setStatus(rule.getStatus());
    return vo;
  }
}
