package io.github.genkidoudou.common.excel.annotation;

import java.lang.annotation.*;

/**
 * 标记导出合并列。
 */
@Documented
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface CellMerge {
  int index() default -1;
}

