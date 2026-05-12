package io.github.genkidoudou.common.validation;

import cn.hutool.extra.spring.SpringUtil;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import lombok.experimental.UtilityClass;

import java.util.Set;

@UtilityClass
public class ValidatorUtils {
  private static final Validator VALID = SpringUtil.getBean(Validator.class);

  public static <T> void validate(T object, Class<?>... groups) {
    Set<ConstraintViolation<T>> validate = VALID.validate(object, groups);
    if (!validate.isEmpty()) {
      throw new ConstraintViolationException("参数校验异常", validate);
    }
  }
}
