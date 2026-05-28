package io.github.genkidoudou.report.token;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.stp.StpUtil;
import io.github.genkidoudou.report.bridge.JimuAuthBridge;
import io.github.genkidoudou.report.security.JimuShareAccessFilter;
import io.github.genkidoudou.report.security.JimuShareUriMatcher;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import cn.hutool.core.util.StrUtil;
import org.jeecg.modules.jmreport.api.JmReportTokenServiceI;
import org.jeecg.modules.jmreport.common.util.JimuSpringContextUtils;
import org.jeecg.modules.jmreport.common.vo.JmDictModel;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.LinkedHashSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * 积木报表 Token：登录态走 Sa-Token；分享态识别 shareToken / IS_PASS。
 */
@Slf4j
@Primary
@Component
@RequiredArgsConstructor
public class JimuReportTokenServiceImpl implements JmReportTokenServiceI {

    private static final String HEADER_AUTHORIZATION = "Authorization";
    private static final String HEADER_TOKEN = "token";
    private static final String HEADER_X_ACCESS_TOKEN = "X-Access-Token";



    private static final String[] SHARE_READONLY_PERMISSIONS = new String[]{
            "drag:design:getTotalData"
    };

    /** 积木 BI 设计器敏感操作权限（管理员 *:*:* 时合并） */
    private static final String[] JIMU_BI_ADMIN_PERMISSIONS = new String[]{
            "drag:datasource:testConnection",
            "onl:drag:clear:recovery",
            "drag:analysis:sql",
            "drag:design:getTotalData",
            "onl:drag:page:delete",
            "drag:dataset:save",
            "drag:dataset:delete",
            "drag:datasource:saveOrUpate",
            "drag:datasource:delete"
    };

    private final JimuAuthBridge jimuAuthBridge;

    @Override
    public String getToken(HttpServletRequest request) {
        String token = extractBearer(request);
        if (StrUtil.isBlank(token) && request != null) {
            token = firstNonBlank(
                    request.getParameter("token"),
                    request.getHeader(HEADER_TOKEN),
                    request.getHeader(HEADER_X_ACCESS_TOKEN));
        }
        if (StrUtil.isBlank(token)) {
            try {
                token = StpUtil.getTokenValue();
            } catch (Exception ignored) {
                // 未登录且 URL 未带 token
            }
        }
        if (StrUtil.isNotBlank(token) && request != null && isShareContext(request)) {
            return token;
        }
        if (StrUtil.isNotBlank(token) && request != null) {
            try {
                StpUtil.setTokenValue(token);
            } catch (Exception e) {
                log.debug("StpUtil.setTokenValue skipped: {}", e.getMessage());
            }
        }
        return token;
    }

    @Override
    public String getToken() {
        HttpServletRequest request = currentRequest();
        if (request != null) {
            return getToken(request);
        }
        try {
            return StpUtil.getTokenValue();
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public String getUsername(String token) {
        if (isShareContext(currentRequest())) {
            return "share";
        }
        if (StrUtil.isNotBlank(token)) {
            try {
                Object loginId = StpUtil.getLoginIdByToken(token);
                if (loginId != null) {
                    String name = jimuAuthBridge.resolveUsername(token);
                    return StrUtil.isNotBlank(name) ? name : String.valueOf(loginId);
                }
            } catch (Exception ignored) {
                // fall through
            }
        }
        try {
            return String.valueOf(StpUtil.getLoginId());
        } catch (Exception e) {
            return StrUtil.blankToDefault(token, "anonymous");
        }
    }

    @Override
    public String[] getRoles(String token) {
        if (isShareContext(currentRequest())) {
            return new String[0];
        }
        return jimuAuthBridge.listRoleKeysByToken(token).toArray(String[]::new);
    }

    @Override
    public String[] getPermissions(String token) {
        if (isShareContext(currentRequest())) {
            return SHARE_READONLY_PERMISSIONS;
        }
        List<String> perms = jimuAuthBridge.listPermissionsByToken(token);
        if (perms.stream().anyMatch(p -> "*:*:*".equals(p))) {
            return mergePermissions(JIMU_BI_ADMIN_PERMISSIONS, perms);
        }
        return perms.toArray(String[]::new);
    }

    @Override
    public List<JmDictModel> getDictItems(String dictCode) {
        if (StrUtil.isBlank(dictCode)) {
            return List.of();
        }
        List<JimuAuthBridge.JimuDictEntry> rows = jimuAuthBridge.listDictByType(dictCode);
        List<JmDictModel> out = new ArrayList<>(rows.size());
        for (JimuAuthBridge.JimuDictEntry e : rows) {
            if (e == null) {
                continue;
            }
            out.add(new JmDictModel()
                    .setDictCode(dictCode)
                    .setValue(e.value())
                    .setText(e.text()));
        }
        return out;
    }

    @Override
    public Boolean verifyToken(String token) {
        HttpServletRequest request = currentRequest();
        if (isShareContext(request)) {
            if (JimuShareUriMatcher.hasShareTokenParam(request.getQueryString())) {
                return true;
            }
            Object pass = request != null ? request.getAttribute(JimuShareAccessFilter.ATTR_IS_PASS) : null;
            return Boolean.TRUE.equals(pass);
        }
        if (StrUtil.isBlank(token)) {
            return false;
        }
        try {
            Object loginId = StpUtil.getLoginIdByToken(token);
            return loginId != null;
        } catch (NotLoginException ex) {
            log.debug("verifyToken failed: {}", ex.getMessage());
            return false;
        } catch (Exception ex) {
            try {
                StpUtil.setTokenValue(token);
                StpUtil.checkLogin();
                return true;
            } catch (Exception inner) {
                log.debug("verifyToken fallback failed: {}", inner.getMessage());
                return false;
            }
        }
    }

    @Override
    public String getTenantId() {
        HttpServletRequest request = JimuSpringContextUtils.getHttpServletRequest();
        if (request == null) {
            request = currentRequest();
        }
        if (request != null) {
            String tenant = firstNonBlank(
                    request.getHeader("X-Tenant-Id"),
                    request.getHeader("tenant-id"),
                    request.getParameter("tenantId"));
            if (StrUtil.isNotBlank(tenant)) {
                return tenant;
            }
        }
        return "0";
    }

    @Override
    public HttpHeaders customApiHeader() {
        HttpHeaders headers = new HttpHeaders();
        String token = getToken();
        if (StrUtil.isNotBlank(token)) {
            headers.add(HEADER_AUTHORIZATION, "Bearer " + token);
            headers.add(HEADER_TOKEN, token);
            headers.add(HEADER_X_ACCESS_TOKEN, token);
        }
        return headers;
    }

    private static boolean isShareContext(HttpServletRequest request) {
        if (request == null) {
            return false;
        }
        if (Boolean.TRUE.equals(request.getAttribute(JimuShareAccessFilter.ATTR_IS_PASS))) {
            return true;
        }
        return JimuShareUriMatcher.isShareUri(request.getRequestURI());
    }

    private static String extractBearer(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        String auth = request.getHeader(HEADER_AUTHORIZATION);
        if (StrUtil.isNotBlank(auth) && auth.startsWith("Bearer ")) {
            return auth.substring(7).trim();
        }
        return null;
    }

    private static HttpServletRequest currentRequest() {
        var attrs = RequestContextHolder.getRequestAttributes();
        if (attrs instanceof ServletRequestAttributes servletAttrs) {
            return servletAttrs.getRequest();
        }
        return null;
    }

    private static String firstNonBlank(String... values) {
        if (values == null) {
            return null;
        }
        for (String v : values) {
            if (StrUtil.isNotBlank(v)) {
                return v;
            }
        }
        return null;
    }

    private static String[] mergePermissions(String[] extra, List<String> fromDb) {
        Set<String> merged = new LinkedHashSet<>();
        if (extra != null) {
            for (String p : extra) {
                merged.add(p);
            }
        }
        if (fromDb != null) {
            merged.addAll(fromDb);
        }
        return merged.toArray(String[]::new);
    }
}
