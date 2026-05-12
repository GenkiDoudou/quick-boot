package com.su60.quickboot.common.captcha;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 验证码vo类
 *
 * @author luyanan
 * @since 2024/07/16
 **/
@Data
@Accessors(chain = true)
public class CaptchaVo implements Serializable {
	private static final long serialVersionUID = 1L;


	/**
	 * 验证码的key
	 *
	 * @since 2024/07/16
	 */

	private String uuid;

	/**
	 * base64
	 *
	 * @since 2024/07/16
	 */

	private String img;


	/**
	 * 验证码是否开启
	 *
	 * @since 2024/8/16
	 */

	private Boolean captchaEnabled;
}