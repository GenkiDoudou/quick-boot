package com.su60.quickboot.web.controller;

import cn.dev33.satoken.annotation.SaIgnore;
import com.su60.quickboot.common.core.R;
import com.su60.quickboot.common.captcha.CaptchaService;
import com.su60.quickboot.common.captcha.CaptchaVo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 验证码控制层
 *
 * @author luyanan
 * @since 2024/07/16
 **/
@Slf4j
@RequiredArgsConstructor
@RestController
public class CaptchaController {

	private final CaptchaService captchaService;

	/**
	 * 创建验证码
	 *
	 * @return 验证码结果
	 * @since 2024/07/17
	 */

	@SaIgnore
	@GetMapping("captchaImage")
	public R<CaptchaVo> create() {
		CaptchaVo captchaVo = captchaService.create();
		return R.ok(captchaVo);
	}


}
