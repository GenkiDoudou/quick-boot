package io.github.genkidoudou.web.monitor.online.support;

import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.useragent.UserAgent;
import cn.hutool.http.useragent.UserAgentUtil;
import io.github.genkidoudou.web.system.dept.domain.SysDept;
import io.github.genkidoudou.web.system.dept.mapper.SysDeptMapper;
import io.github.genkidoudou.web.system.user.domain.SysUser;
import io.github.genkidoudou.web.system.user.mapper.SysUserMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 登录成功后向 Token-Session 写入在线用户展示信息。
 */
@Component
@RequiredArgsConstructor
public class OnlineSessionRecorder {

    private final SysUserMapper userMapper;
    private final SysDeptMapper deptMapper;

    /**
     * 在 {@link StpUtil#login(Object)} 之后调用。
     *
     * @param request 当前请求
     * @param userId  用户主键
     */
    public void record(HttpServletRequest request, long userId) {
        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            return;
        }
        SaSession tokenSession = StpUtil.getTokenSession();
        tokenSession.set(OnlineSessionKeys.USER_NAME, StrUtil.blankToDefault(user.getUserName(), ""));
        tokenSession.set(OnlineSessionKeys.DEPT_NAME, resolveDeptName(user.getDeptId()));
        String ip = clientIp(request);
        tokenSession.set(OnlineSessionKeys.IPADDR, ip);
        tokenSession.set(OnlineSessionKeys.LOGIN_LOCATION, "");
        String uaHeader = request.getHeader("User-Agent");
        UserAgent ua = UserAgentUtil.parse(StrUtil.nullToEmpty(uaHeader));
        tokenSession.set(OnlineSessionKeys.BROWSER,
            ua.getBrowser() != null ? StrUtil.blankToDefault(ua.getBrowser().getName(), "Unknown") : "Unknown");
        tokenSession.set(OnlineSessionKeys.OS,
            ua.getOs() != null ? StrUtil.blankToDefault(ua.getOs().getName(), "Unknown") : "Unknown");
        tokenSession.set(OnlineSessionKeys.LOGIN_TIME, LocalDateTime.now().toString());
    }

    private String resolveDeptName(Long deptId) {
        if (deptId == null) {
            return "";
        }
        SysDept dept = deptMapper.selectById(deptId);
        return dept == null || dept.getDeptName() == null ? "" : dept.getDeptName();
    }

    private static String clientIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (StrUtil.isNotBlank(xff)) {
            return xff.split(",")[0].trim();
        }
        return request.getRemoteAddr() == null ? "" : request.getRemoteAddr();
    }
}
