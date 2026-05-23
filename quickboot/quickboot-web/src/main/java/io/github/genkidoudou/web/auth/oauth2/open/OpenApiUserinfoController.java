package io.github.genkidoudou.web.auth.oauth2.open;

import cn.dev33.satoken.oauth2.data.model.AccessTokenModel;
import cn.dev33.satoken.oauth2.template.SaOAuth2Util;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import io.github.genkidoudou.common.api.R;
import io.github.genkidoudou.web.system.oauthclient.domain.SysOauthUserOpenid;
import io.github.genkidoudou.web.system.oauthclient.mapper.SysOauthUserOpenidMapper;
import io.github.genkidoudou.web.system.user.domain.SysUser;
import io.github.genkidoudou.web.system.user.mapper.SysUserMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * OAuth2 开放 API：按 scope 返回只读用户信息。
 */
@Tag(name = "开放 API")
@RestController
@RequestMapping("/open-api/v1")
@RequiredArgsConstructor
public class OpenApiUserinfoController {

    private final SysOauthUserOpenidMapper openidMapper;
    private final SysUserMapper userMapper;

    /**
     * 需要有效 OAuth2 access_token；按 scope 裁剪字段。
     */
    @Operation(summary = "OAuth2 用户信息")
    @GetMapping("/userinfo")
    public R<Map<String, Object>> userinfo() {
        AccessTokenModel token = SaOAuth2Util.currentAccessToken();
        String clientId = token.clientId;
        Long userId = Long.valueOf(String.valueOf(token.loginId));
        List<String> scopes = token.scopes;

        SysOauthUserOpenid mapping = openidMapper.selectOne(Wrappers.<SysOauthUserOpenid>lambdaQuery()
                .eq(SysOauthUserOpenid::getClientId, clientId)
                .eq(SysOauthUserOpenid::getUserId, userId)
                .last("LIMIT 1"));

        Map<String, Object> body = new LinkedHashMap<>();
        if (mapping != null) {
            body.put("openid", mapping.getOpenid());
        }
        if (scopes != null && scopes.stream().anyMatch(s -> "profile".equalsIgnoreCase(s))) {
            SysUser user = userMapper.selectById(userId);
            if (user != null) {
                body.put("userName", user.getUserName());
                body.put("nickName", user.getNickName());
            }
        }
        return R.ok(body);
    }
}
