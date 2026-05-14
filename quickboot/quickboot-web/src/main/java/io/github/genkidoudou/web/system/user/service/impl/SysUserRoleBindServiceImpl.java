package io.github.genkidoudou.web.system.user.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import io.github.genkidoudou.web.system.menu.domain.SysUserRole;
import io.github.genkidoudou.web.system.menu.mapper.SysUserRoleMapper;
import io.github.genkidoudou.web.system.user.service.SysUserRoleBindService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * {@link SysUserRoleBindService} 实现。
 */
@Service
public class SysUserRoleBindServiceImpl implements SysUserRoleBindService {

    private final SysUserRoleMapper userRoleMapper;

    public SysUserRoleBindServiceImpl(SysUserRoleMapper userRoleMapper) {
        this.userRoleMapper = userRoleMapper;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void replaceAllRolesForUser(Long userId, List<Long> roleIds) {
        userRoleMapper.delete(Wrappers.<SysUserRole>lambdaQuery().eq(SysUserRole::getUserId, userId));
        if (roleIds == null || roleIds.isEmpty()) {
            return;
        }
        for (Long rid : roleIds) {
            if (rid == null || rid < 1) {
                continue;
            }
            SysUserRole ur = new SysUserRole();
            ur.setUserId(userId);
            ur.setRoleId(rid);
            userRoleMapper.insert(ur);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void ensureUserHasRole(Long userId, Long roleId) {
        if (userId == null || userId < 1 || roleId == null || roleId < 1) {
            return;
        }
        long c = userRoleMapper.selectCount(Wrappers.<SysUserRole>lambdaQuery()
                .eq(SysUserRole::getRoleId, roleId)
                .eq(SysUserRole::getUserId, userId));
        if (c == 0) {
            SysUserRole ur = new SysUserRole();
            ur.setRoleId(roleId);
            ur.setUserId(userId);
            userRoleMapper.insert(ur);
        }
    }
}
