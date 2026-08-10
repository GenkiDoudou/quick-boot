package io.github.genkidoudou.common.excel.template;

import java.lang.reflect.Field;

/**
 * 带 {@code @ExcelProperty} 的字段及其 Excel 列索引（0-based）。
 *
 * @param field       字段
 * @param columnIndex 列索引
 */
public record ExcelPropertyColumn(Field field, int columnIndex) {
}
