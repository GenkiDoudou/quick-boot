package com.su60.quickboot.data.excel.convert;

import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.alibaba.excel.converters.Converter;
import com.alibaba.excel.metadata.GlobalConfiguration;
import com.alibaba.excel.metadata.data.WriteCellData;
import com.alibaba.excel.metadata.property.ExcelContentProperty;
import com.su60.quickboot.data.excel.annotation.ExcelDictFormat;

public class ExcelDictConverter implements Converter<String> {
	@Override
	public Class<?> supportJavaTypeKey() {
		return String.class;
	}

	@Override
	public WriteCellData<String> convertToExcelData(String value, ExcelContentProperty contentProperty, GlobalConfiguration globalConfiguration) throws Exception {

		ExcelDictFormat excelDictFormat = contentProperty.getField().getAnnotation(ExcelDictFormat.class);
		if (null == excelDictFormat) {
			return new WriteCellData<>(value);
		}
		String dictType = excelDictFormat.dictType();
		if (StrUtil.isNotBlank(dictType)) {
			DictService dictService = SpringUtil.getBean(DictService.class);
			if (null == dictService) {
				throw new RuntimeException("dictService is null");
			}
			String dictLabel = dictService.getDictLabel(dictType, value);
			return new WriteCellData<>(StrUtil.isBlank(dictLabel) ? value : dictLabel);
		} else if (excelDictFormat.dictText().length > 0) {
			for (String s : excelDictFormat.dictText()) {
				String[] split = s.split(":");
				String dictValue = split[0];
				if (dictValue.equals(value)) {
					return new WriteCellData<>(split[1]);
				}
			}
		}

		return new WriteCellData<>(value);
	}
}
