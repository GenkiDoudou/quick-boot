package io.github.genkidoudou.web.monitor.online.service;

import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.StrUtil;
import io.github.genkidoudou.common.api.PageInfo;
import io.github.genkidoudou.common.exception.ErrorCodes;
import io.github.genkidoudou.common.exception.WarningException;
import io.github.genkidoudou.web.monitor.online.dto.ForceLogoutBo;
import io.github.genkidoudou.web.monitor.online.dto.SysUserOnlineQueryBo;
import io.github.genkidoudou.web.monitor.online.dto.SysUserOnlineVo;
import io.github.genkidoudou.web.monitor.online.support.OnlineSessionKeys;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * 在线用户：基于 Sa-Token 会话检索与强退。
 */
@Service
public class SysUserOnlineService {

    /**
     * 分页列出当前有效 token 会话。
     *
     * @param query 筛选与分页
     * @return 分页结果
     */
    public PageInfo<SysUserOnlineVo> page(SysUserOnlineQueryBo query) {
        List<SysUserOnlineVo> all = listAll(query.getIpaddr(), query.getUserName());
        all.sort(Comparator.comparing(SysUserOnlineVo::getLoginTime, Comparator.nullsLast(String::compareTo)).reversed());
        int pageNum = query.getPageNum() == null || query.getPageNum() < 1 ? 1 : query.getPageNum();
        int pageSize = query.getPageSize() == null || query.getPageSize() < 1 ? 10 : query.getPageSize();
        int from = Math.min((pageNum - 1) * pageSize, all.size());
        int to = Math.min(from + pageSize, all.size());
        List<SysUserOnlineVo> slice = from >= to ? List.of() : all.subList(from, to);
        PageInfo<SysUserOnlineVo> page = new PageInfo<>();
        page.setCurrent(pageNum);
        page.setSize(pageSize);
        page.setRecords(slice);
        page.setTotal(all.size());
        page.setPages(PageInfo.computePages(all.size(), pageSize));
        return page;
    }

    /**
     * 按 token 强退会话。
     *
     * @param req token 值
     */
    public void forceLogout(ForceLogoutBo req) {
        String tokenId = req.getTokenId().trim();
        Object loginId;
        try {
            loginId = StpUtil.getLoginIdByToken(tokenId);
        } catch (Exception ex) {
            loginId = null;
        }
        if (loginId == null) {
            throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "会话不存在或已失效");
        }
        StpUtil.logoutByTokenValue(tokenId);
    }

    private List<SysUserOnlineVo> listAll(String ipaddr, String userName) {
        // searchTokenValue 返回的是持久层 key（如 Authorization:login:token:{uuid}），需剥离前缀后再查会话
        String tokenKeyPrefix = StpUtil.getStpLogic().splicingKeyTokenValue("");
        List<String> tokenKeys = StpUtil.searchTokenValue("", 0, -1, false);
        List<SysUserOnlineVo> rows = new ArrayList<>(tokenKeys.size());
        for (String storageKey : tokenKeys) {
            String token = resolveTokenValue(storageKey, tokenKeyPrefix);
            if (StrUtil.isBlank(token)) {
                continue;
            }
            Object loginIdObj;
            try {
                loginIdObj = StpUtil.getLoginIdByToken(token);
            } catch (Exception ex) {
                continue;
            }
            if (loginIdObj == null) {
                continue;
            }
            SaSession tokenSession;
            try {
                tokenSession = StpUtil.getTokenSessionByToken(token);
            } catch (Exception ex) {
                continue;
            }
            SysUserOnlineVo vo = new SysUserOnlineVo();
            vo.setTokenId(token);
            vo.setUserId(Long.parseLong(loginIdObj.toString()));
            vo.setUserName(sessionString(tokenSession, OnlineSessionKeys.USER_NAME));
            vo.setDeptName(sessionString(tokenSession, OnlineSessionKeys.DEPT_NAME));
            vo.setIpaddr(sessionString(tokenSession, OnlineSessionKeys.IPADDR));
            vo.setLoginLocation(sessionString(tokenSession, OnlineSessionKeys.LOGIN_LOCATION));
            vo.setBrowser(sessionString(tokenSession, OnlineSessionKeys.BROWSER));
            vo.setOs(sessionString(tokenSession, OnlineSessionKeys.OS));
            vo.setLoginTime(sessionString(tokenSession, OnlineSessionKeys.LOGIN_TIME));
            if (StrUtil.isNotBlank(ipaddr) && !StrUtil.contains(vo.getIpaddr(), ipaddr.trim())) {
                continue;
            }
            if (StrUtil.isNotBlank(userName) && !StrUtil.containsIgnoreCase(vo.getUserName(), userName.trim())) {
                continue;
            }
            rows.add(vo);
        }
        return rows;
    }

    private static String sessionString(SaSession session, String key) {
        Object v = session.get(key);
        return v == null ? "" : v.toString();
    }

    /**
     * 将 Sa-Token 持久层 token key 转为裸 token 值，供 {@link StpUtil#getLoginIdByToken(String)} 使用。
     */
    private static String resolveTokenValue(String storageKey, String tokenKeyPrefix) {
        if (StrUtil.isBlank(storageKey)) {
            return "";
        }
        if (StrUtil.isNotBlank(tokenKeyPrefix) && storageKey.startsWith(tokenKeyPrefix)) {
            return storageKey.substring(tokenKeyPrefix.length());
        }
        return storageKey;
    }
}
