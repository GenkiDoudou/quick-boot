package com.su60.quickboot.data.file.exception;

/**
 * 文件大小限制
 *
 * @author luyanan
 * @since 2024/06/05
 **/
public class FileSizeLimitException extends RuntimeException {



	public FileSizeLimitException(String message) {
		super(message);
	}
}
