package io.github.genkidoudou.common.file;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * 无 system 实现时的占位：不返回任何分类（上传将因分类不存在失败）。
 */
public class EmptyFileClassifyRuleResolver implements FileClassifyRuleResolver {

  @Override
  public Optional<FileClassifyRule> findByClassify(String classify) {
    return Optional.empty();
  }

  @Override
  public List<FileClassifyRule> listEnabled() {
    return Collections.emptyList();
  }
}
