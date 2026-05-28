package io.github.genkidoudou.auth;

import io.github.genkidoudou.common.api.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 登录页「扫码登录」占位：与 {@code quick-ui} {@code getQRCode()} 契约一致（{@code data.img} 为可展示的 data URL）。
 * <p>
 * 未接入真实扫码登录时返回固定占位图，避免打开登录页即请求失败。
 */
@Tag(name = "登录扫码占位")
@RestController
public class QrcodeImageController {

    /**
     * 与前端 {@code login.vue} 中请求失败时的占位 SVG 一致，便于联调。
     */
    private static final String PLACEHOLDER_IMG =
            "data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMjAwIiBoZWlnaHQ9IjIwMCIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj48cmVjdCB3aWR0aD0iMjAwIiBoZWlnaHQ9IjIwMCIgZmlsbD0id2hpdGUiLz48cmVjdCB4PSIxMCIgeT0iMTAiIHdpZHRoPSI0MCIgaGVpZ2h0PSI0MCIgZmlsbD0iYmxhY2siLz48cmVjdCB4PSIxNTAiIHk9IjEwIiB3aWR0aD0iNDAiIGhlaWdodD0iNDAiIGZpbGw9ImJsYWNrIi8+PHJlY3QgeD0iMTAiIHk9IjE1MCIgd2lkdGg9IjQwIiBoZWlnaHQ9IjQwIiBmaWxsPSJibGFjayIvPjwvc3ZnPg==";

    /**
     * 返回占位二维码图片地址（data URL），供登录页展示。
     *
     * @return {@code data.img}
     */
    @Operation(summary = "获取扫码登录占位二维码")
    @GetMapping("/qrcodeImage")
    public R<Map<String, String>> qrcodeImage() {
        return R.ok(Map.of("img", PLACEHOLDER_IMG));
    }
}
