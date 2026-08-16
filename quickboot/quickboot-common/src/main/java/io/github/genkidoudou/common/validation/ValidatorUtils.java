package io.github.genkidoudou.common.validation;

import cn.hutool.extra.spring.SpringUtil;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import lombok.experimental.UtilityClass;

import java.util.Set;

/**
 * Jakarta Bean Validation 静态入口，委托 Spring 容器中的 {@link Validator} 执行分组校验。
 */
@UtilityClass
public class ValidatorUtils {
  private static final Validator VALID = SpringUtil.getBean(Validator.class);

  /**
   * 校验对象；存在约束违例时抛出 {@link ConstraintViolationException}。
   *
   * @param object 待校验对象
   * @param groups 校验分组，可空表示默认分组
   * @param <T>    对象类型
   * @throws ConstraintViolationException 任一约束不满足
   */
  public static <T> void validate(T object, Class<?>... groups) {
    Set<ConstraintViolation<T>> validate = VALID.validate(object, groups);
    if (!validate.isEmpty()) {
      throw new ConstraintViolationException("参数校验异常", validate);
    }
  }
}
