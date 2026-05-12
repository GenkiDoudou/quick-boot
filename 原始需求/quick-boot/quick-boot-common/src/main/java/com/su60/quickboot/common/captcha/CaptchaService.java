package com.su60.quickboot.common.captcha;

/**
 * 验证码服务接口
 *
 * @author luyanan
 * @since 2024/12/19
 */
public interface CaptchaService {

    /**
     * 创建验证码
     *
     * @return 验证码信息
     */
    CaptchaVo create();

    /**
     * 验证验证码
     *
     * @param code 验证码
     * @param uuid 唯一标识
     * @return 是否验证通过
     */
    boolean check(String code, String uuid);
}
