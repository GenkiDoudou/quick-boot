package com.su60.quickboot.common.validation;

import lombok.experimental.UtilityClass;
import org.springframework.context.i18n.LocaleContextHolder;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.util.Locale;
import java.util.Set;

/**
 * 参数校验
 *
 * @author luyanan
 * @since 2024/07/08
 **/
@UtilityClass
public class BeanValidators {
	/**
	 * 对象属性校验
	 *
	 * @param validator validator
	 * @param object    对象
	 * @param groups    分组
	 * @return
	 * @since 2024/07/08
	 */
	public void validate(Validator validator, Object object, Class<?>... groups) throws ConstraintViolationException {
		Set<ConstraintViolation<Object>> constraintViolations = validator.validate(object, groups);
		if (!constraintViolations.isEmpty()) {
			throw new ConstraintViolationException(constraintViolations);
		}

	}


	/**
	 * 参数校验
	 *
	 * @param object 对象
	 * @param groups 分组
	 * @return
	 * @since 2024/07/08
	 */
	public void validate(Object object, Class<?>... groups) {
		Locale.setDefault(LocaleContextHolder.getLocale());
		Validator validator = Validation.byDefaultProvider().configure().buildValidatorFactory().getValidator();
		validate(validator, object, groups);

	}
}
