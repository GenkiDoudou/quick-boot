package io.github.genkidoudou.common.excel.dict;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 解析 {@code @ExcelDictFormat#dictText()} 内联项（首个 {@code =} 为分隔）。
 */
public final class DictTextParser {

  private static final Logger log = LoggerFactory.getLogger(DictTextParser.class);

  private DictTextParser() {
  }

  /**
   * 解析内联字典文本为 value→label 有序映射。
   *
   * @param dictText 注解数组；非法项（无 {@code =}）跳过并 warn
   * @return 不可变映射；入参空时返回空映射
   */
  public static Map<String, String> parseValueToLabel(String[] dictText) {
    if (dictText == null || dictText.length == 0) {
      return Collections.emptyMap();
    }
    Map<String, String> map = new LinkedHashMap<>();
    for (String item : dictText) {
      if (item == null || item.isBlank()) {
        continue;
      }
      int idx = item.indexOf('=');
      if (idx < 0) {
        log.warn("忽略非法 dictText 项（缺少 '='）: {}", item);
        continue;
      }
      String value = item.substring(0, idx);
      String label = item.substring(idx + 1);
      map.put(value, label);
    }
    return Collections.unmodifiableMap(map);
  }

  /**
   * 由 value→label 反转得到 label→value（后写覆盖先写，与 LinkedHashMap 遍历顺序一致）。
   *
   * @param valueToLabel value→label
   * @return label→value
   */
  public static Map<String, String> invert(Map<String, String> valueToLabel) {
    if (valueToLabel == null || valueToLabel.isEmpty()) {
      return Collections.emptyMap();
    }
    Map<String, String> labelToValue = new LinkedHashMap<>();
    for (Map.Entry<String, String> e : valueToLabel.entrySet()) {
      labelToValue.put(e.getValue(), e.getKey());
    }
    return Collections.unmodifiableMap(labelToValue);
  }
}
