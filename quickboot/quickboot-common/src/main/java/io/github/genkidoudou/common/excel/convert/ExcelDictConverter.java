package io.github.genkidoudou.common.excel.convert;

import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.alibaba.excel.converters.Converter;
import com.alibaba.excel.enums.CellDataTypeEnum;
import com.alibaba.excel.metadata.GlobalConfiguration;
import com.alibaba.excel.metadata.data.ReadCellData;
import com.alibaba.excel.metadata.data.WriteCellData;
import com.alibaba.excel.metadata.property.ExcelContentProperty;
import io.github.genkidoudou.common.excel.ExcelDictLabelService;
import io.github.genkidoudou.common.excel.annotation.ExcelDictFormat;

/**
 * 字典值与标签转换器。
 */
public class ExcelDictConverter implements Converter<String> {
    @Override
    public Class<?> supportJavaTypeKey() {
        return String.class;
    }

    @Override
    public CellDataTypeEnum supportExcelTypeKey() {
        return CellDataTypeEnum.EMPTY;
    }

    @Override
    public WriteCellData<String> convertToExcelData(String value, ExcelContentProperty contentProperty, GlobalConfiguration globalConfiguration) {
        if (contentProperty == null || contentProperty.getField() == null) {
            return new WriteCellData<>(value);
        }
        ExcelDictFormat format = contentProperty.getField().getAnnotation(ExcelDictFormat.class);
        if (format == null) {
            return new WriteCellData<>(value);
        }
        if (StrUtil.isNotBlank(format.dictType())) {
            try {
                ExcelDictLabelService svc = SpringUtil.getBean(ExcelDictLabelService.class);
                String label = svc.getDictLabel(format.dictType(), value);
                if (StrUtil.isNotBlank(label)) {
                    return new WriteCellData<>(label);
                }
            } catch (Exception ignored) {
            }
        }
        for (String item : format.dictText()) {
            String[] arr = item.split(":");
            if (arr.length == 2 && StrUtil.equals(arr[0], value)) {
                return new WriteCellData<>(arr[1]);
            }
        }
        return new WriteCellData<>(value);
    }

    @Override
    public String convertToJavaData(ReadCellData<?> cellData, ExcelContentProperty contentProperty, GlobalConfiguration globalConfiguration) {
        String raw;
        CellDataTypeEnum type = cellData.getType();
        if (type == CellDataTypeEnum.NUMBER && cellData.getNumberValue() != null) {
            raw = cellData.getNumberValue().toPlainString();
        } else if (type == CellDataTypeEnum.BOOLEAN && cellData.getBooleanValue() != null) {
            raw = String.valueOf(cellData.getBooleanValue());
        } else {
            raw = StrUtil.blankToDefault(cellData.getStringValue(), String.valueOf(cellData.getData()));
        }
        raw = StrUtil.trim(raw);
        if (contentProperty == null || contentProperty.getField() == null || StrUtil.isBlank(raw)) {
            return raw;
        }
        ExcelDictFormat format = contentProperty.getField().getAnnotation(ExcelDictFormat.class);
        if (format == null) {
            return raw;
        }
        if (StrUtil.isNotBlank(format.dictType())) {
            try {
                ExcelDictLabelService svc = SpringUtil.getBean(ExcelDictLabelService.class);
                String value = svc.getDictValue(format.dictType(), raw);
                if (StrUtil.isNotBlank(value)) {
                    return value;
                }
            } catch (Exception ignored) {
            }
        }
        for (String item : format.dictText()) {
            String[] arr = item.split(":");
            if (arr.length == 2 && StrUtil.equals(arr[1], raw)) {
                return arr[0];
            }
        }
        return raw;
    }
}
