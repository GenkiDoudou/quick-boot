package io.github.genkidoudou.auth;

import io.github.genkidoudou.common.api.ClientIds;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.useragent.UserAgent;
import cn.hutool.http.useragent.UserAgentUtil;
import io.github.genkidoudou.web.system.logininfor.domain.SysLogininfor;
import io.github.genkidoudou.web.system.logininfor.mapper.SysLogininforMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 登录访问日志写入：从请求解析 IP/UA，失败不影响登录主流程。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysLogininforLogService {

    private static final String STATUS_SUCCESS = "0";
    private static final String STATUS_FAIL = "1";

    private final SysLogininforMapper logininforMapper;

    /**
     * 记录成功登录。
     *
     * @param request 当前请求
     * @param userId  用户主键
     * @param userName 登录名
     */
    public void recordSuccess(HttpServletRequest request, long userId, String userName) {
        insertQuiet(buildEntity(request, userId, userName, STATUS_SUCCESS, "登录成功"));
    }

    /**
     * 记录失败登录（含用户不存在、密码错误等）。
     *
     * @param request 当前请求
     * @param userName 登录名（可能无对应用户）
     * @param message  失败描述
     */
    public void recordFailure(HttpServletRequest request, String userName, String message) {
        insertQuiet(buildEntity(request, null, userName, STATUS_FAIL, StrUtil.blankToDefault(message, "登录失败")));
    }

    private SysLogininfor buildEntity(HttpServletRequest request, Long userId, String userName, String status, String msg) {
        SysLogininfor row = new SysLogininfor();
        row.setUserId(userId);
        row.setUserName(userName);
        String ip = clientIp(request);
        row.setIpaddr(ip);
        row.setLoginLocation("");
        String uaHeader = request.getHeader("User-Agent");
        UserAgent ua = UserAgentUtil.parse(StrUtil.nullToEmpty(uaHeader));
        row.setBrowser(ua.getBrowser() != null ? StrUtil.blankToDefault(ua.getBrowser().getName(), "Unknown") : "Unknown");
        row.setOs(ua.getOs() != null ? StrUtil.blankToDefault(ua.getOs().getName(), "Unknown") : "Unknown");
        row.setStatus(status);
        row.setMsg(msg);
        row.setClientId(ClientIds.normalizeHeader(request.getHeader(ClientIds.HEADER_NAME)));
        row.setLoginTime(LocalDateTime.now());
        return row;
    }

    private void insertQuiet(SysLogininfor row) {
        try {
            logininforMapper.insert(row);
        } catch (Exception e) {
            log.error("写入登录日志失败 userName={}", row.getUserName(), e);
        }
    }

    private static String clientIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (StrUtil.isNotBlank(xff)) {
            return xff.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
