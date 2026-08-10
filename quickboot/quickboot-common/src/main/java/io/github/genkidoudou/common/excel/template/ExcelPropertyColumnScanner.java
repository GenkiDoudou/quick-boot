package io.github.genkidoudou.common.excel.template;

import com.alibaba.excel.annotation.ExcelProperty;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 扫描类上带 {@link ExcelProperty} 的字段并解析列序。
 *
 * <p>规则：显式 {@code index >= 0} 优先；未指定 index 的字段按声明序填入空闲列号。
 */
public final class ExcelPropertyColumnScanner {

  private ExcelPropertyColumnScanner() {
  }

  /**
   * 扫描 head 类（含父类）上带 {@link ExcelProperty} 的字段。
   *
   * @param clazz EasyExcel 行模型
   * @return 按列索引升序的列定义；无注解时为空列表
   */
  public static List<ExcelPropertyColumn> scan(Class<?> clazz) {
    if (clazz == null) {
      return List.of();
    }
    List<Field> fields = collectExcelFields(clazz);
    if (fields.isEmpty()) {
      return List.of();
    }
    boolean anyIndex = fields.stream()
      .anyMatch(f -> f.getAnnotation(ExcelProperty.class).index() >= 0);

    List<ExcelPropertyColumn> columns = new ArrayList<>(fields.size());
    if (!anyIndex) {
      int i = 0;
      for (Field field : fields) {
        columns.add(new ExcelPropertyColumn(field, i++));
      }
      return columns;
    }

    Set<Integer> used = new HashSet<>();
    for (Field field : fields) {
      int index = field.getAnnotation(ExcelProperty.class).index();
      if (index >= 0) {
        used.add(index);
      }
    }
    int next = 0;
    for (Field field : fields) {
      int index = field.getAnnotation(ExcelProperty.class).index();
      if (index < 0) {
        while (used.contains(next)) {
          next++;
        }
        index = next;
        used.add(index);
        next++;
      }
      columns.add(new ExcelPropertyColumn(field, index));
    }
    columns.sort(Comparator.comparingInt(ExcelPropertyColumn::columnIndex));
    return columns;
  }

  private static List<Field> collectExcelFields(Class<?> clazz) {
    List<Class<?>> hierarchy = new ArrayList<>();
    for (Class<?> c = clazz; c != null && c != Object.class; c = c.getSuperclass()) {
      hierarchy.add(c);
    }
    // 父类字段在前，与常见 EasyExcel 行为一致
    List<Field> result = new ArrayList<>();
    for (int i = hierarchy.size() - 1; i >= 0; i--) {
      for (Field field : hierarchy.get(i).getDeclaredFields()) {
        if (Modifier.isStatic(field.getModifiers())) {
          continue;
        }
        if (field.getAnnotation(ExcelProperty.class) == null) {
          continue;
        }
        field.setAccessible(true);
        result.add(field);
      }
    }
    return result;
  }
}
