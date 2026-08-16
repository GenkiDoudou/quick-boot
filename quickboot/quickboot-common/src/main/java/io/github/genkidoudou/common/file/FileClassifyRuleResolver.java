package io.github.genkidoudou.common.file;

import java.util.List;
import java.util.Optional;

/**
 * 分类规则解析 SPI：由 system 模块基于 {@code sys_file_classify} 提供实现。
 */
public interface FileClassifyRuleResolver {

  /**
   * 按分类键查询规则（含停用记录；软删不存在则 empty）。
   *
   * @param classify 分类键
   * @return 规则
   */
  Optional<FileClassifyRule> findByClassify(String classify);

  /**
   * 列出启用中的分类（未删且 status=0）。
   *
   * @return 启用规则列表
   */
  List<FileClassifyRule> listEnabled();
}
