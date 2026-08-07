package io.github.genkidoudou.common.excel.annotation;

import java.lang.annotation.*;

/**
 * Excel 字典翻译注解。
 */
@Documented
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ExcelDictFormat {
  String dictType() default "";

  String[] dictText() default {};
}

