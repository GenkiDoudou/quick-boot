package io.github.genkidoudou.report.internal.security;

import io.github.genkidoudou.report.internal.config.JimuProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * 识别积木对外分享/预览路径，供 {@link io.github.genkidoudou.report.internal.token.JimuReportTokenServiceImpl} 走分享态鉴权。
 */
@Component
@RequiredArgsConstructor
public class JimuShareAccessFilter extends OncePerRequestFilter {

    public static final String ATTR_IS_PASS = "QC_JIMU_IS_PASS";
    public static final String ATTR_LOGIN_USER = "QC_JIMU_LOGIN_USER";

    private final JimuProperties jimuProperties;

    /**
     * 分享路径命中时在 request 上标记 IS_PASS，供积木 Token 服务走分享态鉴权。
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        if (jimuProperties.getShare().isEnabled() && JimuShareUriMatcher.isShareUri(request.getRequestURI())) {
            request.setAttribute(ATTR_IS_PASS, Boolean.TRUE);
            request.setAttribute(ATTR_LOGIN_USER, "share");
        }
        filterChain.doFilter(request, response);
    }
}
