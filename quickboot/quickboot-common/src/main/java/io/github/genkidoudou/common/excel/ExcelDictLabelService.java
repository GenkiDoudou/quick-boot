package io.github.genkidoudou.common.excel;

/**
 * Excel 字典标签转换服务。
 */
public interface ExcelDictLabelService {

    /**
     * 按字典类型和字典值查询字典标签。
     */
    String getDictLabel(String dictType, String dictValue);

    /**
     * 按字典类型和字典标签查询字典值。
     */
    default String getDictValue(String dictType, String dictLabel) {
        return null;
    }
}

