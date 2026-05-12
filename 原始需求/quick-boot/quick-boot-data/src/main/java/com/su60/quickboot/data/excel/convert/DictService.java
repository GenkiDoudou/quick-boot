package com.su60.quickboot.data.excel.convert;

public interface DictService {

	/**
	 * 根据dictValue 获取字典名
	 * @since 2025/11/29 
	 * @param dictType  type
	 * @param dictValue value
	 * @return
	 */
	String getDictLabel(String dictType, String dictValue);


	/**
	 * 根据名字匹配value
	 * @since 2025/11/29
 * @param dictType
 * @param dictLabel
	 * @return
	 */
	String getDictValue(String dictType, String dictLabel);
}
