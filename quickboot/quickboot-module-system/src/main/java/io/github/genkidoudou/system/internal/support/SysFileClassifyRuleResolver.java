package io.github.genkidoudou.system.internal.support;

import io.github.genkidoudou.common.file.FileClassifyRule;
import io.github.genkidoudou.common.file.FileClassifyRuleResolver;
import io.github.genkidoudou.system.internal.entity.SysFileClassify;
import io.github.genkidoudou.system.internal.service.ISysFileClassifyService;
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
    SysFileClassify entity = sysFileClassifyService.getByClassifyKey(classify.trim());
    return Optional.ofNullable(toRule(entity));
  }

  @Override
  public List<FileClassifyRule> listEnabled() {
    List<SysFileClassify> entities = sysFileClassifyService.listEnabledEntities();
    List<FileClassifyRule> rules = new ArrayList<>(entities.size());
    for (SysFileClassify entity : entities) {
      FileClassifyRule rule = toRule(entity);
      if (rule != null) {
        rules.add(rule);
      }
    }
    return rules;
  }

  private static FileClassifyRule toRule(SysFileClassify entity) {
    if (entity == null || !StringUtils.hasText(entity.getClassify())) {
      return null;
    }
    FileClassifyRule rule = new FileClassifyRule();
    rule.setClassify(entity.getClassify());
    rule.setClassifyName(entity.getClassifyName());
    rule.setLimitExt(entity.getLimitExt());
    Long size = entity.getLimitSizeBytes();
    rule.setLimitSizeBytes(size == null || size <= 0 ? 10L * 1024 * 1024 : size);
    Integer count = entity.getLimitCount();
    rule.setLimitCount(count == null || count <= 0 ? 1 : count);
    rule.setCompressEnabled(entity.getCompressEnabled());
    rule.setCompressMinSizeKb(entity.getCompressMinSizeKb());
    rule.setCompressQuality(entity.getCompressQuality());
    rule.setCompressMaxEdge(entity.getCompressMaxEdge());
    rule.setAnonymous("1".equals(entity.getAnonymous()));
    rule.setStatus(entity.getStatus());
    return rule;
  }
}
