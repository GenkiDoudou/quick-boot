package io.github.genkidoudou.common.captcha;

import cloud.tianai.captcha.application.ImageCaptchaApplication;
import cloud.tianai.captcha.application.vo.ImageCaptchaVO;
import cloud.tianai.captcha.common.constant.CaptchaTypeConstant;
import cloud.tianai.captcha.common.response.ApiResponse;
import cloud.tianai.captcha.generator.impl.StandardConcatImageCaptchaGenerator;
import cloud.tianai.captcha.validator.common.model.dto.ImageCaptchaTrack;
import cn.dev33.satoken.annotation.SaIgnore;
import io.github.genkidoudou.common.core.R;
import io.github.genkidoudou.common.exception.ErrorException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;


/**
 * 验证码控制器
 *
 * @author genkidoudou
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/captcha")
@RequiredArgsConstructor
@Tag(name = "验证码管理", description = "验证码生成和验证接口")
public class CaptchaController {

    private final ImageCaptchaApplication application;

    private final CaptchaProperties captchaProperties;

    /**
     * 生成验证码
     */
    @SaIgnore
    @RequestMapping(value = "/generate")
    @Operation(summary = "生成验证码", description = "生成指定类型的验证码")
    public ApiResponse<ImageCaptchaVO> generate() {
        return application.generateCaptcha(captchaProperties.getType());
    }

    /**
     * 验证验证码
     */
    @SaIgnore
    @PostMapping("/validate")
    @Operation(summary = "验证验证码", description = "验证用户提交的验证码")
    public ApiResponse<?> validate(@RequestBody Data data) {

        ApiResponse<?> response = application.matching(data.getId(), data.getData());
        if (response.isSuccess()) {
            return ApiResponse.ofSuccess(Collections.singletonMap("id", data.getId()));
        }
        return response;
    }

    @lombok.Data
    public static class Data {
        private String id;
        private ImageCaptchaTrack data;
    }
}
