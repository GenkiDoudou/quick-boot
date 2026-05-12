package com.su60.quickboot.common.exception;

/**
 * 错误提示
 *
 * @author luyanan
 * @since 2023/09/09
 **/
public class ErrorException extends BaseException {
	public ErrorException(Integer code, String msg, String... args) {
		super(code, msg, args);
	}

	public ErrorException(Integer code, String... args) {
		super(code, args);
	}

	public ErrorException(String msg, String... args) {
		super(msg, args);
	}
}
