package io.github.genkidoudou.common.excel.conver;

import com.alibaba.excel.converters.Converter;
import com.alibaba.excel.enums.CellDataTypeEnum;
import com.alibaba.excel.metadata.GlobalConfiguration;
import com.alibaba.excel.metadata.data.ReadCellData;
import com.alibaba.excel.metadata.data.WriteCellData;
import com.alibaba.excel.metadata.property.ExcelContentProperty;
import io.github.genkidoudou.common.excel.annotation.ExcelDictFormat;
import io.github.genkidoudou.common.excel.dict.DictConvertEngine;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Field;
import java.math.BigDecimal;

/**
 * EasyExcel 全局 String 字典转换：仅当字段带 {@link ExcelDictFormat} 时生效，否则直通。
 */
public class ExcelDictConvert implements Converter<String> {

  @Override
  public Class<?> supportJavaTypeKey() {
    return String.class;
  }

  @Override
  public String convertToJavaData(ReadCellData<?> cellData,
                                  ExcelContentProperty contentProperty,
                                  GlobalConfiguration globalConfiguration) {
    String raw = readCellAsString(cellData);
    ExcelDictFormat format = resolveFormat(contentProperty);
    if (format == null) {
      return raw;
    }
    return DictConvertEngine.toValues(raw, format, fieldName(contentProperty));
  }

  @Override
  public WriteCellData<?> convertToExcelData(String value,
                                             ExcelContentProperty contentProperty,
                                             GlobalConfiguration globalConfiguration) {
    ExcelDictFormat format = resolveFormat(contentProperty);
    if (format == null) {
      return new WriteCellData<>(value == null ? "" : value);
    }
    String label = DictConvertEngine.toLabels(value, format, fieldName(contentProperty));
    return new WriteCellData<>(label == null ? "" : label);
  }

  private static ExcelDictFormat resolveFormat(ExcelContentProperty contentProperty) {
    if (contentProperty == null || contentProperty.getField() == null) {
      return null;
    }
    return contentProperty.getField().getAnnotation(ExcelDictFormat.class);
  }

  private static String fieldName(ExcelContentProperty contentProperty) {
    Field field = contentProperty == null ? null : contentProperty.getField();
    return field == null ? "?" : field.getName();
  }

  private static String readCellAsString(ReadCellData<?> cellData) {
    if (cellData == null) {
      return null;
    }
    CellDataTypeEnum type = cellData.getType();
    if (type == CellDataTypeEnum.NUMBER && cellData.getNumberValue() != null) {
      BigDecimal number = cellData.getNumberValue();
      return number.stripTrailingZeros().toPlainString();
    }
    if (type == CellDataTypeEnum.BOOLEAN && cellData.getBooleanValue() != null) {
      return String.valueOf(cellData.getBooleanValue());
    }
    String str = cellData.getStringValue();
    if (StringUtils.isNotEmpty(str)) {
      return str;
    }
    return cellData.getData() == null ? null : String.valueOf(cellData.getData());
  }
}
