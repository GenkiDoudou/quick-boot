package io.github.genkidoudou.common.excel.conver;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.ObjectUtil;
import com.alibaba.excel.converters.Converter;
import com.alibaba.excel.enums.CellDataTypeEnum;
import com.alibaba.excel.metadata.GlobalConfiguration;
import com.alibaba.excel.metadata.data.ReadCellData;
import com.alibaba.excel.metadata.data.WriteCellData;
import com.alibaba.excel.metadata.property.ExcelContentProperty;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;

/**
 * Long 型大数 Excel 转换器：超过 15 位以字符串写入，避免 Excel 精度丢失。
 */
@Slf4j
public class ExcelBigNumberConvert implements Converter<Long> {

  /**
   * {@inheritDoc}
   */
  @Override
  public Class<Long> supportJavaTypeKey() {
    return Long.class;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public CellDataTypeEnum supportExcelTypeKey() {
    return CellDataTypeEnum.STRING;
  }

  /**
   * 读取单元格为 Long；委托 Hutool {@link Convert#toLong(Object)}。
   */
  @Override
  public Long convertToJavaData(ReadCellData<?> cellData, ExcelContentProperty contentProperty, GlobalConfiguration globalConfiguration) {
    return Convert.toLong(cellData.getData());
  }

  /**
   * 写入 Excel：字符串长度超过 15 时用文本单元格，否则用数值单元格。
   */
  @Override
  public WriteCellData<Object> convertToExcelData(Long object, ExcelContentProperty contentProperty, GlobalConfiguration globalConfiguration) {
    if (ObjectUtil.isNotNull(object)) {
      String str = Convert.toStr(object);
      if (str.length() > 15) {
        return new WriteCellData<>(str);
      }
    }
    WriteCellData<Object> cellData = new WriteCellData<>(new BigDecimal(object));
    cellData.setType(CellDataTypeEnum.NUMBER);
    return cellData;
  }

}
