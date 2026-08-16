package io.github.genkidoudou.common.monitor.operlog;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 控制操作日志切面是否记录或记录粒度。
 */
@Documented
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface IgnoreLogger {

  /**
   * 忽略粒度。
   *
   * @return 忽略类型，默认 {@link Type#ALL}
   */
  Type type() default Type.ALL;

  enum Type {
    /** 不发布操作日志事件。 */
    ALL,
    /** 不记录请求参数。 */
    PARAMS,
    /** 不记录返回结果。 */
    RESULT
  }
}
