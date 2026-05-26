package io.github.genkidoudou.web.report.bridge;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.StrUtil;
import io.github.genkidoudou.report.bridge.JimuAuthBridge;
import io.github.genkidoudou.web.system.dict.data.domain.SysDictData;
import io.github.genkidoudou.web.system.dict.data.service.DictDataService;
import io.github.genkidoudou.web.system.menu.service.MenuService;
import io.github.genkidoudou.web.system.user.dto.SysUserDetailVo;
import io.github.genkidoudou.web.system.user.service.SysUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 积木报表权限/字典与 quickboot 系统模块桥接。
 */
@Component
@RequiredArgsConstructor
public class JimuAuthBridgeImpl implements JimuAuthBridge {

    private final MenuService menuService;
    private final DictDataService dictDataService;
    private final SysUserService sysUserService;

    @Override
    public List<String> listRoleKeysByToken(String token) {
        long uid = resolveUserId(token);
        if (uid <= 0) {
            return List.of();
        }
        return menuService.listRoleKeysByUserId(uid);
    }

    @Override
    public List<String> listPermissionsByToken(String token) {
        long uid = resolveUserId(token);
        if (uid <= 0) {
            return List.of();
        }
        return menuService.listPermissionsByUserId(uid);
    }

    @Override
    public String resolveUsername(String token) {
        long uid = resolveUserId(token);
        if (uid <= 0) {
            return null;
        }
        SysUserDetailVo user = sysUserService.get(uid);
        if (user == null) {
            return String.valueOf(uid);
        }
        return StrUtil.blankToDefault(user.getUserName(), String.valueOf(uid));
    }

    @Override
    public long resolveUserId(String token) {
        if (StrUtil.isNotBlank(token)) {
            try {
                Object loginId = StpUtil.getLoginIdByToken(token);
                if (loginId != null) {
                    return toLong(loginId);
                }
            } catch (Exception ignored) {
                // fall through
            }
        }
        try {
            if (StpUtil.isLogin()) {
                return StpUtil.getLoginIdAsLong();
            }
        } catch (Exception ignored) {
            // not logged in
        }
        return -1L;
    }

    @Override
    public List<JimuDictEntry> listDictByType(String dictType) {
        if (StrUtil.isBlank(dictType)) {
            return List.of();
        }
        List<SysDictData> rows = dictDataService.listByType(dictType);
        List<JimuDictEntry> out = new ArrayList<>(rows.size());
        for (SysDictData row : rows) {
            if (row == null) {
                continue;
            }
            out.add(new JimuDictEntry(row.getDictValue(), row.getDictLabel()));
        }
        return out;
    }

    private static long toLong(Object loginId) {
        if (loginId instanceof Number n) {
            return n.longValue();
        }
        return Long.parseLong(loginId.toString());
    }
}
