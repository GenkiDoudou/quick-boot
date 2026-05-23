package com.su60.quickboot.common.exception;

/**
 * 警告异常
 *
 * @author luyanan
 * @since 2023/09/09
 **/
public class WarningException extends BaseException {


	public WarningException(Integer code, Object... args) {
		super(code, args);
	}

	public WarningException(String msg, String... args) {
		super(msg, args);
	}


}
