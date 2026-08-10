package io.github.genkidoudou.common.captcha;

import cloud.tianai.captcha.application.ImageCaptchaApplication;
import cloud.tianai.captcha.application.vo.ImageCaptchaVO;
import cloud.tianai.captcha.common.response.ApiResponse;
import cloud.tianai.captcha.validator.common.model.dto.ImageCaptchaTrack;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;

/**
 * 天爱行为验证码：生成与校验（与前端 TAC SDK 默认契约一致）。
 *
 * @author genkidoudou
 * @since 1.0.0
 */
@Tag(name = "验证码")
@RestController
@RequestMapping("/api/captcha")
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "qc.captcha", name = "enabled", havingValue = "true", matchIfMissing = true)
public class CaptchaController {

  private final ImageCaptchaApplication application;

  private final CaptchaProperties captchaProperties;

  /**
   * 生成验证码实例。
   *
   * @return tianai 标准载荷
   */
  @Operation(summary = "生成验证码")
  @RequestMapping("/generate")
  public ApiResponse<ImageCaptchaVO> generate() {
    return application.generateCaptcha(captchaProperties.getType());
  }

  /**
   * 校验滑动轨迹等；成功时在 data 中回传 {@code id} 供登录二次校验。
   *
   * @param body id + 轨迹
   * @return 校验结果
   */
  @Operation(summary = "校验验证码")
  @PostMapping("/validate")
  public ApiResponse<?> validate(@RequestBody Data body) {
    ApiResponse<?> response = application.matching(body.getId(), body.getData());
    if (response.isSuccess()) {
      return ApiResponse.ofSuccess(Collections.singletonMap("id", body.getId()));
    }
    return response;
  }

  @lombok.Data
  public static class Data {
    private String id;
    private ImageCaptchaTrack data;
  }
}
