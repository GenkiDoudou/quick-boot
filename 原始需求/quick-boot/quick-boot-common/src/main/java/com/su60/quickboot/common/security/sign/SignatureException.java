package com.su60.quickboot.common.security.sign;

/**
 * 签名验证异常
 *
 * @author luyanan
 * @since 2026/01/31
 */
public class SignatureException extends RuntimeException {

	public SignatureException(String message) {
		super(message);
	}

	public SignatureException(String message, Throwable cause) {
		super(message, cause);
	}
}

