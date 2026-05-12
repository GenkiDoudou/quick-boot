package com.su60.quickboot.common.exception;

/**
 * 基础异常类
 *
 * @author luyanan
 * @since 2023/09/08
 **/
public class BaseException extends RuntimeException {


	/**
	 * 错误编码
	 *
	 * @since 2023/09/08
	 */

	private String code;


	/**
	 * 错误信息
	 *
	 * @since 2023/09/08
	 */

	private String msg;
	/**
	 * 参数
	 *
	 * @since 2024/11/12
	 */

	private Object[] args;

	/**
	 * 全局
	 *
	 * @param code 错误code
	 * @param msg  消息
	 * @param args 参数
	 * @since 2024/11/12
	 */
	public BaseException(String code, String msg, String... args) {
		super(msg);
		this.code = code;
		this.msg = msg;
		this.args = args;
	}


	public BaseException(String code, Object... args) {
		super(code.toString());
		this.code = code;
		this.args = args;
	}


	public BaseException(String msg, String... args) {
		super(msg);
		this.code = GlobalMsgCode.INTERNAL_SERVER_ERROR;
		this.msg = msg;
		this.args = args;
	}


	public Integer getCode() {
		return code;
	}

	public String getMsg() {
		return msg;
	}

	public Object[] getArgs() {
		return args;
	}
}
