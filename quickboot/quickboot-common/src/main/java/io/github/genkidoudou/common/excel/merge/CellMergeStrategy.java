package io.github.genkidoudou.common.excel.merge;

import com.alibaba.excel.metadata.Head;
import com.alibaba.excel.write.merge.AbstractMergeStrategy;
import io.github.genkidoudou.common.excel.annotation.CellMerge;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * 基于 {@link CellMerge} 注解的单元格合并策略。
 */
public class CellMergeStrategy extends AbstractMergeStrategy {

    private final List<?> rows;
    private final boolean hasTitle;

    public CellMergeStrategy(List<?> rows, boolean hasTitle) {
        this.rows = rows;
        this.hasTitle = hasTitle;
    }

    @Override
    protected void merge(Sheet sheet, Cell cell, Head head, Integer relativeRowIndex) {
        if (rows == null || rows.isEmpty()) {
            return;
        }
        int triggerRow = hasTitle ? 1 : 0;
        if (cell.getRowIndex() != triggerRow || cell.getColumnIndex() != 0) {
            return;
        }
        for (CellRangeAddress range : buildRanges(rows, hasTitle)) {
            sheet.addMergedRegion(range);
        }
    }

    private static List<CellRangeAddress> buildRanges(List<?> rows, boolean hasTitle) {
        List<CellRangeAddress> result = new ArrayList<>();
        Object first = rows.get(0);
        Field[] fields = first.getClass().getDeclaredFields();
        int startRowOffset = hasTitle ? 1 : 0;
        for (Field field : fields) {
            CellMerge anno = field.getAnnotation(CellMerge.class);
            if (anno == null) {
                continue;
            }
            field.setAccessible(true);
            int col = anno.index() >= 0 ? anno.index() : resolveFieldIndex(fields, field.getName());
            int start = 0;
            Object last = readField(rows.get(0), field);
            for (int i = 1; i <= rows.size(); i++) {
                Object curr = i < rows.size() ? readField(rows.get(i), field) : null;
                boolean same = i < rows.size() && java.util.Objects.equals(last, curr) && last != null && !String.valueOf(last).isBlank();
                if (!same) {
                    if (i - start > 1) {
                        result.add(new CellRangeAddress(start + startRowOffset, i - 1 + startRowOffset, col, col));
                    }
                    start = i;
                    last = curr;
                }
            }
        }
        return result;
    }

    private static int resolveFieldIndex(Field[] fields, String fieldName) {
        for (int i = 0; i < fields.length; i++) {
            if (fields[i].getName().equals(fieldName)) {
                return i;
            }
        }
        return 0;
    }

    private static Object readField(Object row, Field field) {
        try {
            return field.get(row);
        } catch (IllegalAccessException e) {
            return null;
        }
    }
}
