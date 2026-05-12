package com.su60.quickboot.common.idempotent;

import com.su60.quickboot.common.exception.BaseException;
import com.su60.quickboot.common.exception.GlobalMsgCode;

/**
 * 幂等异常
 * <p>
 * 当检测到重复请求时抛出此异常
 * </p>
 *
 * @author luyanan
 * @since 2026/01/18
 */
public class IdempotentException extends BaseException {

	/**
	 * 默认错误码：重复请求
	 */
	private static final int DEFAULT_CODE = 409;

	/**
	 * 默认错误消息
	 */
	private static final String DEFAULT_MESSAGE = "请勿重复提交";

	/**
	 * 构造方法
	 *
	 * @param message 错误消息
	 */
	public IdempotentException(String message) {
		super(DEFAULT_CODE, message);
	}

	/**
	 * 构造方法
	 *
	 * @param code    错误码
	 * @param message 错误消息
	 */
	public IdempotentException(int code, String message) {
		super(code, message);
	}

	/**
	 * 构造方法（使用默认错误码）
	 *
	 * @param message 错误消息
	 * @param cause   原因
	 */
	public IdempotentException(String message, Throwable cause) {
		super(DEFAULT_CODE, message, cause);
	}
}
