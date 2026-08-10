package io.github.genkidoudou.common.excel.dict;

import io.github.genkidoudou.common.excel.annotation.ExcelDictFormat;
import io.github.genkidoudou.common.excel.exception.ExcelDataCheckException;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Excel 字典双向转换引擎（导出 value→label，导入 label→value）。
 */
public final class DictConvertEngine {

  private DictConvertEngine() {
  }

  /**
   * 导出：字典键值 → 标签。
   *
   * @param raw       字段原值
   * @param format    注解
   * @param fieldName 字段名（错误信息）
   * @return 转换后字符串；blank 原样
   */
  public static String toLabels(String raw, ExcelDictFormat format, String fieldName) {
    if (StringUtils.isBlank(raw) || format == null) {
      return raw;
    }
    TokenMapper mapper = exportMapper(format, fieldName);
    return convertTokens(raw, format.separator(), format.missPolicy(), fieldName, format.dictType(), mapper);
  }

  /**
   * 导入：标签 → 字典键值（混填：先按 label，再认合法 value）。
   *
   * @param raw       单元格原文
   * @param format    注解
   * @param fieldName 字段名（错误信息）
   * @return 转换后字符串；blank 原样
   */
  public static String toValues(String raw, ExcelDictFormat format, String fieldName) {
    if (StringUtils.isBlank(raw) || format == null) {
      return raw;
    }
    TokenMapper mapper = importMapper(format, fieldName);
    return convertTokens(raw, format.separator(), format.missPolicy(), fieldName, format.dictType(), mapper);
  }

  private static TokenMapper exportMapper(ExcelDictFormat format, String fieldName) {
    String dictType = format.dictType();
    if (StringUtils.isNotBlank(dictType)) {
      DictLookup lookup = requireLookupOrEmpty(format, fieldName, dictType.trim());
      if (lookup == null) {
        return token -> null;
      }
      String type = dictType.trim();
      return token -> lookup.getLabel(type, token);
    }
    Map<String, String> valueToLabel = DictTextParser.parseValueToLabel(format.dictText());
    return valueToLabel::get;
  }

  private static TokenMapper importMapper(ExcelDictFormat format, String fieldName) {
    String dictType = format.dictType();
    if (StringUtils.isNotBlank(dictType)) {
      DictLookup lookup = requireLookupOrEmpty(format, fieldName, dictType.trim());
      if (lookup == null) {
        return token -> null;
      }
      String type = dictType.trim();
      return token -> {
        String byLabel = lookup.getValue(type, token);
        if (byLabel != null) {
          return byLabel;
        }
        // 原文已是合法键值
        if (lookup.getLabel(type, token) != null) {
          return token;
        }
        return null;
      };
    }
    Map<String, String> valueToLabel = DictTextParser.parseValueToLabel(format.dictText());
    Map<String, String> labelToValue = DictTextParser.invert(valueToLabel);
    return token -> {
      String byLabel = labelToValue.get(token);
      if (byLabel != null) {
        return byLabel;
      }
      if (valueToLabel.containsKey(token)) {
        return token;
      }
      return null;
    };
  }

  /**
   * @return Lookup；未注册且非 ERROR 时返回 {@code null}（全部走 missPolicy）
   */
  private static DictLookup requireLookupOrEmpty(ExcelDictFormat format, String fieldName, String dictType) {
    DictLookup lookup = DictLookupHolder.get();
    if (lookup != null) {
      return lookup;
    }
    if (format.missPolicy() == DictMissPolicy.ERROR) {
      throw new ExcelDataCheckException(
        "字典服务未就绪: field=" + fieldName + ", dictType=" + dictType);
    }
    return null;
  }

  private static String convertTokens(String raw,
                                      String separator,
                                      DictMissPolicy missPolicy,
                                      String fieldName,
                                      String dictType,
                                      TokenMapper mapper) {
    if (separator == null || separator.isEmpty()) {
      return mapToken(raw, mapper, missPolicy, fieldName, dictType, false);
    }
    String[] parts = StringUtils.splitByWholeSeparatorPreserveAllTokens(raw, separator);
    List<String> out = new ArrayList<>(parts.length);
    for (String part : parts) {
      String mapped = mapToken(part, mapper, missPolicy, fieldName, dictType, true);
      if (mapped != null) {
        out.add(mapped);
      }
    }
    return String.join(separator, out);
  }

  /**
   * @param skipOnEmpty 为 true 时 EMPTY 策略返回 {@code null} 以便多值跳过该段；单值 EMPTY 返回空串
   */
  private static String mapToken(String token,
                                 TokenMapper mapper,
                                 DictMissPolicy missPolicy,
                                 String fieldName,
                                 String dictType,
                                 boolean skipOnEmpty) {
    String mapped = mapper.map(token);
    if (mapped != null) {
      return mapped;
    }
    DictMissPolicy effective = missPolicy == null ? DictMissPolicy.KEEP : missPolicy;
    return switch (effective) {
      case KEEP -> token;
      case EMPTY -> skipOnEmpty ? null : "";
      case ERROR -> {
        String typePart = StringUtils.isNotBlank(dictType) ? ", dictType=" + dictType : "";
        throw new ExcelDataCheckException(
          "字典项未匹配: field=" + fieldName + typePart + ", token=" + token);
      }
    };
  }

  @FunctionalInterface
  private interface TokenMapper {
    /**
     * @return 映射结果；无映射返回 {@code null}
     */
    String map(String token);
  }
}
