package io.github.genkidoudou.web.bridge;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.StrUtil;
import io.github.genkidoudou.report.api.JimuAuthBridge;
import io.github.genkidoudou.system.api.SysUserQueryFacade;
import io.github.genkidoudou.system.api.SysUserView;
import io.github.genkidoudou.system.internal.service.ISysDictDataService;
import io.github.genkidoudou.system.internal.service.ISysPermissionService;
import io.github.genkidoudou.system.internal.vo.SysDictDataVo;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 积木报表权限/字典与 system 模块桥接（装配在 app）。
 */
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "qc.jimu", name = "enabled", havingValue = "true", matchIfMissing = true)
public class JimuAuthBridgeImpl implements JimuAuthBridge {

    private final ISysPermissionService permissionService;
    private final ISysDictDataService dictDataService;
    private final SysUserQueryFacade sysUserQueryFacade;

    /** {@inheritDoc} */
    @Override
    public List<String> listRoleKeysByToken(String token) {
        long uid = resolveUserId(token);
        if (uid <= 0) {
            return List.of();
        }
        return permissionService.listRoleKeys(String.valueOf(uid));
    }

    /** {@inheritDoc} */
    @Override
    public List<String> listPermissionsByToken(String token) {
        long uid = resolveUserId(token);
        if (uid <= 0) {
            return List.of();
        }
        return new ArrayList<>(permissionService.listPermissions(String.valueOf(uid)));
    }

    /** {@inheritDoc} */
    @Override
    public String resolveUsername(String token) {
        long uid = resolveUserId(token);
        if (uid <= 0) {
            return null;
        }
        SysUserView user = sysUserQueryFacade.findByUserId(uid);
        if (user == null) {
            return String.valueOf(uid);
        }
        return StrUtil.blankToDefault(user.userName(), String.valueOf(uid));
    }

    /** {@inheritDoc}：优先 token 解析，回退当前 StpUtil 登录态。 */
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

    /** {@inheritDoc} */
    @Override
    public List<JimuDictEntry> listDictByType(String dictType) {
        if (StrUtil.isBlank(dictType)) {
            return List.of();
        }
        List<SysDictDataVo> rows = dictDataService.listByType(dictType);
        List<JimuDictEntry> out = new ArrayList<>(rows.size());
        for (SysDictDataVo row : rows) {
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
