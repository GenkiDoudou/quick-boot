package io.github.genkidoudou.common.excel.dict;

import java.util.Collections;
import java.util.List;

/**
 * 系统字典查询 SPI（仅服务 {@code dictType}；内联映射不走此接口）。
 */
public interface DictLookup {

  /**
   * 按字典类型与键值取标签。
   *
   * @param dictType 字典类型
   * @param value    字典键值
   * @return 标签；无映射时返回 {@code null}
   */
  String getLabel(String dictType, String value);

  /**
   * 按字典类型与标签取键值。
   *
   * @param dictType 字典类型
   * @param label    字典标签
   * @return 键值；无映射时返回 {@code null}
   */
  String getValue(String dictType, String label);

  /**
   * 列出字典类型下全部标签（导入模板下拉用）。
   *
   * @param dictType 字典类型
   * @return 有序标签列表；无数据时返回空列表（不为 {@code null}）
   */
  default List<String> listLabels(String dictType) {
    return Collections.emptyList();
  }
}
