package io.github.genkidoudou.common.excel.annotation;

import io.github.genkidoudou.common.excel.dict.DictMissPolicy;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Excel 字典翻译注解：导出 value→label，导入 label→value。
 *
 * <p>优先级：非空 {@link #dictType()} 走系统字典；否则用 {@link #dictText()} 内联映射。
 */
@Documented
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ExcelDictFormat {

  /**
   * 系统字典类型；非空时优先于 {@link #dictText()}。
   *
   * @return 字典类型编码
   */
  String dictType() default "";

  /**
   * 内联映射，形如 {@code "0=男","1=女"}；仅在 {@link #dictType()} 为空时生效。
   *
   * @return 内联项
   */
  String[] dictText() default {};

  /**
   * 多值分隔符；空串表示整格按单值处理。
   *
   * @return 分隔符
   */
  String separator() default ",";

  /**
   * 未匹配策略。
   *
   * @return 策略
   */
  DictMissPolicy missPolicy() default DictMissPolicy.KEEP;
}
