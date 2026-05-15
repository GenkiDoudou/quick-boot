package io.github.genkidoudou.common.captcha;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 验证码返回对象
 *
 * @author genkidoudou
 * @since 1.0.0
 */
@Data
@Schema(description = "验证码信息")
public class CaptchaVO {

    @Schema(description = "验证码ID")
    private String captchaId;

    @Schema(description = "验证码类型")
    private String type;

    @Schema(description = "背景图片(Base64)")
    private String backgroundImage;

    @Schema(description = "模板图片(Base64)")
    private String templateImage;

    @Schema(description = "资源图片(Base64)")
    private String resourceImage;
}
