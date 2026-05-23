package com.su60.quickboot.common.captcha;

import cn.hutool.captcha.AbstractCaptcha;
import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.generator.CodeGenerator;
import cn.hutool.captcha.generator.MathGenerator;
import cn.hutool.core.util.IdUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

/**
 * 验证码服务实现类
 *
 * @author luyanan
 * @since 2024/12/19
 */
@Slf4j
@RequiredArgsConstructor
public class CaptchaServiceImpl implements CaptchaService {

	private final CacheManager cacheManager;

	/**
	 * 验证码存储的key
	 *
	 * @since 2024/07/17
	 */

	private static final String KEY = "CAPTCHA:";


	@Override
	public CaptchaVo create() {
		// 使用默认配置
		CaptchaTypeEnum captchaType = CaptchaTypeEnum.LINE;
		Integer height = 40;
		Integer width = 120;
		Integer codeCount = 4;
		Integer lineCount = 5;
		AbstractCaptcha captcha = null;
		if (CaptchaTypeEnum.CIRCLE == captchaType) {
			captcha = CaptchaUtil.createCircleCaptcha(width, height);
		} else if (CaptchaTypeEnum.GIF == captchaType) {
			captcha = CaptchaUtil.createGifCaptcha(width, height, codeCount);
		} else if (CaptchaTypeEnum.LINE == captchaType) {
			captcha = CaptchaUtil.createLineCaptcha(width, height, codeCount, lineCount);
		} else if (CaptchaTypeEnum.SHEAR == captchaType) {
			captcha = CaptchaUtil.createShearCaptcha(width, height, codeCount, lineCount);
		} else {
			throw new IllegalArgumentException("Invalid captcha type: " + captchaType);
		}
		captcha.setGenerator(new MathGenerator(1));
		captcha.setTextAlpha(1);

		String code = captcha.getCode();
		String imageBase64Data = captcha.getImageBase64Data();
		// 保存验证码

		String key = IdUtil.fastSimpleUUID();
		log.debug("生成验证码,key:{},code:{}", key, code);

		cacheManager.getCache(KEY + "#" + (5 * 60)).put(key, code); // 默认5分钟过期
		return new CaptchaVo()
				.setUuid(key)
				.setCaptchaEnabled(true)
				.setImg(imageBase64Data);
	}


	@Override
	public boolean check(String key, String code) {
		CodeGenerator codeGenerator = new MathGenerator();
		Cache cache = cacheManager.getCache(KEY + "#" + (5 * 60)); // 默认5分钟过期
		Cache.ValueWrapper valueWrapper = cache.get(key);
		if (null == valueWrapper || null == valueWrapper.get()) {
			return false;
		}
		boolean verify = codeGenerator.verify(valueWrapper.get().toString(), code);
		cache.evict(key);
		return verify;
	}
}