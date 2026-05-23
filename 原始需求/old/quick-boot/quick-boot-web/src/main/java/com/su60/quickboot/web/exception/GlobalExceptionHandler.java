package com.su60.quickboot.web.exception;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotPermissionException;
import com.su60.quickboot.common.core.R;
import com.su60.quickboot.common.exception.ErrorException;
import com.su60.quickboot.common.exception.GlobalMsgCode;
import com.su60.quickboot.common.exception.WarningException;
import com.su60.quickboot.common.sensitive.SensitiveWordException;
import com.su60.quickboot.data.utils.MessageUtils;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.Set;

/**
 * 全局异常
 *
 * @author luyanan
 * @since 2024/07/06
 **/
@RequiredArgsConstructor
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

	/**
	 * 参数校验异常拦截
	 *
	 * @param e 异常
	 * @return 拦截返回
	 * @since 2024/07/07
	 */
	@ResponseStatus(HttpStatus.OK)
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public R methodArgumentNotValidException(MethodArgumentNotValidException e) {
		List<ObjectError> allErrors = e.getBindingResult().getAllErrors();
		ObjectError objectError = allErrors.get(0);
		String defaultMessage = objectError.getDefaultMessage();
		Integer code = GlobalMsgCode.BAD_REQUEST;
		String msg = MessageUtils.getMessage(defaultMessage, code + "", defaultMessage, e);
		log.warn("全局参数异常信息 code:{},ex={}", code, msg);
		return R.failed(code, msg);
	}

	/**
	 * 参数校验
	 *
	 * @param e 异常
	 * @return 拦截返回
	 * @since 2024/07/07
	 */
	@ResponseStatus(HttpStatus.OK)
	@ExceptionHandler(ConstraintViolationException.class)
	public R constraintViolationException(ConstraintViolationException e) {
		// 使用 Stream API 获取第一个校验错误信息
		String defaultMessage = e.getConstraintViolations().stream()
				.findFirst()
				.map(ConstraintViolation::getMessage)
				.orElse("参数校验失败");
		Integer code = GlobalMsgCode.BAD_REQUEST;
		String msg = MessageUtils.getMessage(defaultMessage, "{" + code + "}", defaultMessage, e);
		log.warn("全局参数异常信息 code:{},ex={}", code, msg);
		return R.failed(code, msg);
	}

	/**
	 * 未权限
	 * @since 2025/9/18
	 * @param exception
	 * @return
	 */
	@ExceptionHandler(NotPermissionException.class)
	@ResponseStatus(HttpStatus.OK)
	public R notPermissionException(NotPermissionException exception) {
		log.warn("全局参数异常信息 ex={}", exception.getLocalizedMessage());
		return R.failed(GlobalMsgCode.FORBIDDEN, "未授权");
	}


	/**
	 * 未登录
	 * @since 2025/9/18
	 * @param exception
	 * @return
	 */
	@ExceptionHandler(NotLoginException.class)
	@ResponseStatus(HttpStatus.OK)
	public R notNotLoginException(NotLoginException exception) {
		log.warn("全局参数异常信息 ex={}", exception.getLocalizedMessage());
		return R.failed(GlobalMsgCode.UNAUTHORIZED, "未授权");
	}

	/**
	 * 自定义异常-错误异常
	 *
	 * @param e 异常
	 * @return 拦截返回
	 * @since 2024/07/07
	 */
	@ExceptionHandler(ErrorException.class)
	@ResponseStatus(HttpStatus.OK)
	public R errorException(ErrorException e) {
		Integer code = e.getCode();
		String message = MessageUtils.getMessage("{" + code + "}", e);
		log.error("全局异常信息 code:{},ex={}", code, message, e);
		return R.failed(code, message);
	}


	/**
	 * 自定义异常-警告异常
	 *
	 * @param e 异常
	 * @return 拦截返回
	 * @since 2024/07/07
	 */
	@ExceptionHandler(WarningException.class)
	@ResponseStatus(HttpStatus.OK)
	public R warningException(WarningException e) {
		Integer code = e.getCode();
		String message = MessageUtils.getMessage("{" + code + "}", e.getArgs());
		log.warn("全局异常信息 code:{},ex={}", code, message);
		return R.failed(code, message);
	}

	/**
	 * 自定义异常-警告异常
	 *
	 * @param e 异常
	 * @return 拦截返回
	 * @since 2024/07/07
	 */
	@ExceptionHandler(SensitiveWordException.class)
	@ResponseStatus(HttpStatus.OK)
	public R sensitiveWordException(SensitiveWordException e) {
		log.warn("发现敏感词 code:{},ex={}", GlobalMsgCode.FORBIDDEN, e.getMessage());
		return R.failed(GlobalMsgCode.FORBIDDEN, e.getMessage());
	}

	/**
	 * 异常拦截
	 *
	 * @param e 异常
	 * @return 拦截返回
	 * @since 2024/07/07
	 */
	@ExceptionHandler(Exception.class)
	@ResponseStatus(HttpStatus.OK)
	public R handleGlobalException(Exception e) {
		// 完整异常信息记录到日志
		log.error("全局异常信息", e);
		// 生产环境不返回详细异常信息，防止敏感信息泄露
		String message = "系统内部错误，请联系管理员";
		if (log.isDebugEnabled()) {
			// 开发环境可以返回详细信息
			message = e.getMessage();
		}
		return R.failed(GlobalMsgCode.INTERNAL_SERVER_ERROR, message);
	}
}
