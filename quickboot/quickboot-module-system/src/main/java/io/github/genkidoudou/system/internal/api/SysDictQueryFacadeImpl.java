package io.github.genkidoudou.system.internal.api;

import io.github.genkidoudou.common.excel.dict.DictLookup;
import io.github.genkidoudou.system.api.SysDictQueryFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * {@link SysDictQueryFacade} 实现：委托已注册的 {@link DictLookup}（系统字典）。
 */
@Service
@RequiredArgsConstructor
public class SysDictQueryFacadeImpl implements SysDictQueryFacade {

  private final DictLookup dictLookup;

  @Override
  public String getLabel(String dictType, String value) {
    return dictLookup.getLabel(dictType, value);
  }

  @Override
  public String getValue(String dictType, String label) {
    return dictLookup.getValue(dictType, label);
  }

  @Override
  public List<String> listLabels(String dictType) {
    return dictLookup.listLabels(dictType);
  }
}
