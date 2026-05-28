package io.github.genkidoudou.web.system.oauthclient.service;

import cn.hutool.core.util.StrUtil;
import io.github.genkidoudou.common.exception.ErrorCodes;
import io.github.genkidoudou.common.exception.WarningException;
import io.github.genkidoudou.web.system.oauthclient.clientsign.ClientRequestPathSupport;
import io.github.genkidoudou.web.system.oauthclient.clientsign.ClientSignService;
import io.github.genkidoudou.web.system.oauthclient.domain.SysOauthClient;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import org.springframework.util.AntPathMatcher;

import java.util.ArrayList;
import java.util.List;

/**
 * 按 {@code sys_oauth_client.api_path_patterns} 中的 Ant 路径模式校验请求 path 是否允许访问。
 */
@Service
public class OauthClientApiPathAuthService {

    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    /** OAuth2 授权页签约用 scope，与接口路径授权分离 */
    public static final String DEFAULT_OAUTH_SCOPES = "openid,profile";

    /**
     * 校验客户端是否允许访问该 path；不允许则抛 {@link WarningException}。
     *
     * @param client 已加载的客户端
     * @param path   servlet path（不含 query）
     */
    public void assertPathAllowed(SysOauthClient client, String path) {
        List<String> patterns = parseAntPatterns(client.getApiPathPatterns());
        if (patterns.isEmpty()) {
            throw forbidden("未配置接口授权规则（Ant 路径）");
        }
        String normalized = normalizePath(path);
        for (String pattern : patterns) {
            if (PATH_MATCHER.match(pattern, normalized)) {
                return;
            }
        }
        throw forbidden("无权访问该接口: " + normalized);
    }

    /**
     * 保存前校验 Ant 路径配置（非空、以 {@code /} 开头）。
     *
     * @param raw 多行/逗号分隔的 Ant 路径
     */
    public void validatePathPatterns(String raw) {
        List<String> patterns = parseAntPatterns(raw);
        if (patterns.isEmpty()) {
            throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "请配置至少一条接口路径（Ant 风格）");
        }
        for (String pattern : patterns) {
            if (!pattern.startsWith("/")) {
                throw new WarningException(ErrorCodes.Common.INVALID_PARAM,
                        "路径模式须以 / 开头: " + pattern);
            }
        }
    }

    /**
     * @param raw 配置原文
     * @return Ant 路径模式列表
     */
    public List<String> parseAntPatterns(String raw) {
        if (StrUtil.isBlank(raw)) {
            return List.of();
        }
        List<String> list = new ArrayList<>();
        for (String line : raw.split("[\\r\\n,;]+")) {
            String pattern = line.trim();
            if (!pattern.isEmpty()) {
                list.add(pattern);
            }
        }
        return list;
    }

    /**
     * 与 {@link ClientSignService} 一致的 path 解析。
     */
    public String resolveServletPath(HttpServletRequest request) {
        return ClientRequestPathSupport.resolveServletPath(request);
    }

    private static String normalizePath(String path) {
        if (path == null || path.isEmpty()) {
            return "/";
        }
        return path.startsWith("/") ? path : "/" + path;
    }

    private static WarningException forbidden(String msg) {
        return new WarningException(ErrorCodes.Security.FORBIDDEN, msg);
    }
}
