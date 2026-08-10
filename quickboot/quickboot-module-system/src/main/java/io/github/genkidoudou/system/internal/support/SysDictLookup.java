package io.github.genkidoudou.system.internal.support;

import io.github.genkidoudou.common.excel.dict.DictLookup;
import io.github.genkidoudou.system.internal.service.ISysDictDataService;
import io.github.genkidoudou.system.internal.vo.SysDictDataVo;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 基于 {@link ISysDictDataService#listByType(String)} 缓存的系统字典 Lookup。
 */
public class SysDictLookup implements DictLookup {

  private final ISysDictDataService dictDataService;

  /**
   * @param dictDataService 字典数据服务（须带 listByType 缓存）
   */
  public SysDictLookup(ISysDictDataService dictDataService) {
    this.dictDataService = dictDataService;
  }

  @Override
  public String getLabel(String dictType, String value) {
    if (StringUtils.isBlank(dictType) || value == null) {
      return null;
    }
    for (SysDictDataVo item : list(dictType)) {
      if (value.equals(item.getDictValue())) {
        return item.getDictLabel();
      }
    }
    return null;
  }

  @Override
  public String getValue(String dictType, String label) {
    if (StringUtils.isBlank(dictType) || label == null) {
      return null;
    }
    for (SysDictDataVo item : list(dictType)) {
      if (label.equals(item.getDictLabel())) {
        return item.getDictValue();
      }
    }
    return null;
  }

  @Override
  public List<String> listLabels(String dictType) {
    if (StringUtils.isBlank(dictType)) {
      return List.of();
    }
    List<String> labels = new ArrayList<>();
    for (SysDictDataVo item : list(dictType)) {
      if (item != null && StringUtils.isNotBlank(item.getDictLabel())) {
        labels.add(item.getDictLabel());
      }
    }
    return labels;
  }

  private List<SysDictDataVo> list(String dictType) {
    List<SysDictDataVo> list = dictDataService.listByType(dictType.trim());
    return list == null ? List.of() : list;
  }
}
