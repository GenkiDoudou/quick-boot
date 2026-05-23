package com.su60.quickboot.common.encryption.sm2;

/**
 * SM2加密异常
 *
 * @author luyanan
 * @since 2026/01/31
 */
public class SM2CryptoException extends RuntimeException {

	public SM2CryptoException(String message) {
		super(message);
	}

	public SM2CryptoException(String message, Throwable cause) {
		super(message, cause);
	}
}

