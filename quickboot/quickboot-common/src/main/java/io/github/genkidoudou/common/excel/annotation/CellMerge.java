package io.github.genkidoudou.common.excel.annotation;

import java.lang.annotation.*;

/**
 * 标记导出合并列。
 */
@Documented
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface CellMerge {
  /**
   * 合并列索引（0-based）；{@code -1} 表示按字段声明序推断。
   *
   * @return 列索引
   */
  int index() default -1;
}

