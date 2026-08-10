package io.github.genkidoudou.system.api;

import java.util.List;

/**
 * 字典只读查询门面（对齐 Excel {@code DictLookup} 语义，供他域进程内调用）。
 */
public interface SysDictQueryFacade {

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
   * 列出字典类型下全部标签。
   *
   * @param dictType 字典类型
   * @return 有序标签列表；无数据时返回空列表
   */
  List<String> listLabels(String dictType);
}
