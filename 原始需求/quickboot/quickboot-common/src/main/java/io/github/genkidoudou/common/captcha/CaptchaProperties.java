package io.github.genkidoudou.common.captcha;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 验证码配置属性,其他的配置使用 SpringImageCaptchaProperties
 *
 * @author genkidoudou
 * @see https://doc.captcha.tianai.cloud/
 * @since 1.0.0
 */
@Data
@ConfigurationProperties(prefix = "qc.captcha")
public class CaptchaProperties {

    /**
     * 是否启用验证码
     */
    private Boolean enabled = true;

    /**
     * 验证码类型: 滑块SLIDER 、 旋转ROTATE、 拼接 WORD_IMAGE_CLICK、 文字图片点选CONCAT
     */
    private String type = "SLIDER";

}
