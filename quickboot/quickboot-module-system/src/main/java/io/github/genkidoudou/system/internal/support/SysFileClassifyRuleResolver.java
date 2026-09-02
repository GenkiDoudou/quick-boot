package io.github.genkidoudou.system.internal.support;

import io.github.genkidoudou.common.file.FileClassifyRule;
import io.github.genkidoudou.common.file.FileClassifyRuleResolver;
import io.github.genkidoudou.system.internal.service.ISysFileClassifyService;
import io.github.genkidoudou.system.internal.vo.SysFileClassifyVo;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 基于 {@code sys_file_classify} 的分类规则解析（覆盖 common 占位实现）。
 */
@Primary
@Component
@RequiredArgsConstructor
public class SysFileClassifyRuleResolver implements FileClassifyRuleResolver {

  private final ISysFileClassifyService sysFileClassifyService;

  @Override
  public Optional<FileClassifyRule> findByClassify(String classify) {
    if (!StringUtils.hasText(classify)) {
      return Optional.empty();
    }
    SysFileClassifyVo vo = sysFileClassifyService.getByClassifyKey(classify.trim());
    return Optional.ofNullable(toRule(vo));
  }

  @Override
  public List<FileClassifyRule> listEnabled() {
    List<SysFileClassifyVo> rows = sysFileClassifyService.listEnabled();
    List<FileClassifyRule> rules = new ArrayList<>(rows.size());
    for (SysFileClassifyVo vo : rows) {
      FileClassifyRule rule = toRule(vo);
      if (rule != null) {
        rules.add(rule);
      }
    }
    return rules;
  }

  private static FileClassifyRule toRule(SysFileClassifyVo vo) {
    if (vo == null || !StringUtils.hasText(vo.getClassify())) {
      return null;
    }
    FileClassifyRule rule = new FileClassifyRule();
    rule.setClassify(vo.getClassify());
    rule.setClassifyName(vo.getClassifyName());
    rule.setLimitExt(vo.getLimitExt());
    Long size = vo.getLimitSizeBytes();
    rule.setLimitSizeBytes(size == null || size <= 0 ? 10L * 1024 * 1024 : size);
    Integer count = vo.getLimitCount();
    rule.setLimitCount(count == null || count <= 0 ? 1 : count);
    rule.setCompressEnabled(vo.getCompressEnabled());
    rule.setCompressMinSizeKb(vo.getCompressMinSizeKb());
    rule.setCompressQuality(vo.getCompressQuality());
    rule.setCompressMaxEdge(vo.getCompressMaxEdge());
    rule.setAnonymous("1".equals(vo.getAnonymous()));
    rule.setStatus(vo.getStatus());
    return rule;
  }
}
