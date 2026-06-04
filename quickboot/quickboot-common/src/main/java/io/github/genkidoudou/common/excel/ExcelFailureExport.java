package io.github.genkidoudou.common.excel;

import cn.hutool.core.util.ReflectUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.annotation.ExcelProperty;
import io.github.genkidoudou.common.excel.exception.ExcelException;
import lombok.experimental.UtilityClass;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * 导入失败明细导出：在原始 Excel 列之后追加「失败原因」列。
 */
@UtilityClass
public class ExcelFailureExport {

    private static final String ERROR_REASON_HEAD = "失败原因";
    private static final String DEFAULT_SHEET = "导入失败明细";

    /**
     * @param rowClass 业务导入行模型（与模板列一致）
     * @param failures   失败行（sourceRow 为解析后的行对象，可为 null）
     */
    public static byte[] writeBytes(Class<?> rowClass, List<FailureItem> failures) {
        if (failures == null || failures.isEmpty()) {
            return ExcelUtils.writeBytes(DEFAULT_SHEET, ExcelImportErrorRow.class, List.of());
        }
        List<ExcelColumn> columns = resolveColumns(rowClass);
        List<List<String>> head = new ArrayList<>();
        for (ExcelColumn col : columns) {
            head.add(List.of(col.headName()));
        }
        head.add(List.of(ERROR_REASON_HEAD));

        List<List<Object>> body = new ArrayList<>(failures.size());
        for (FailureItem item : failures) {
            List<Object> line = new ArrayList<>(columns.size() + 1);
            Object source = item.sourceRow();
            for (ExcelColumn col : columns) {
                line.add(source == null ? "" : ReflectUtil.getFieldValue(source, col.field()));
            }
            line.add(item.errorMsg() == null ? "" : item.errorMsg());
            body.add(line);
        }
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            EasyExcel.write(out).head(head).sheet(DEFAULT_SHEET).doWrite(body);
            return out.toByteArray();
        } catch (IOException e) {
            throw new ExcelException("导出失败明细 Excel 异常", e);
        }
    }

    private static List<ExcelColumn> resolveColumns(Class<?> rowClass) {
        Field[] fields = rowClass.getDeclaredFields();
        List<IndexedField> excelFields = new ArrayList<>();
        int order = 0;
        for (Field f : fields) {
            ExcelProperty ep = f.getAnnotation(ExcelProperty.class);
            if (ep != null) {
                int sortKey = ep.index() >= 0 ? ep.index() : 10_000 + order;
                excelFields.add(new IndexedField(f, ep, sortKey));
                order++;
            }
        }
        excelFields.sort(Comparator.comparingInt(IndexedField::sortKey));
        List<ExcelColumn> columns = new ArrayList<>();
        for (IndexedField item : excelFields) {
            columns.add(new ExcelColumn(item.field(), headName(item.ep())));
        }
        return columns;
    }

    private static String headName(ExcelProperty ep) {
        if (ep == null) {
            return "";
        }
        String[] values = ep.value();
        if (values != null && values.length > 0 && values[0] != null && !values[0].isBlank()) {
            return values[0];
        }
        return "";
    }

    /**
     * 单条失败记录。
     *
     * @param rowNo     Excel 行号（1-based，含表头偏移由调用方传入）
     * @param errorMsg  失败原因
     * @param sourceRow 原始行对象（与 {@code rowClass} 一致）
     */
    public record FailureItem(int rowNo, String errorMsg, Object sourceRow) {
    }

    private record ExcelColumn(Field field, String headName) {
    }

    private record IndexedField(Field field, ExcelProperty ep, int sortKey) {
    }
}
