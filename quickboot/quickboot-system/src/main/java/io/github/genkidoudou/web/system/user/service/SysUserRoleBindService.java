package io.github.genkidoudou.web.system.user.service;

import java.util.List;

/**
 * 用户与角色关联写入服务：统一维护 {@code sys_user_role}，供用户域与角色域复用，避免重复插入/删除规则分叉。
 */
public interface SysUserRoleBindService {

    /**
     * 将某用户的角色关联全量替换为给定列表（事务内先删后插）。
     *
     * @param userId  用户主键
     * @param roleIds 角色 id 列表，允许为空（表示清空角色，调用方应在业务层限制）
     */
    void replaceAllRolesForUser(Long userId, List<Long> roleIds);

    /**
     * 若用户尚未拥有该角色则插入一行关联（角色侧批量授权用）。
     *
     * @param userId 用户主键
     * @param roleId 角色主键
     */
    void ensureUserHasRole(Long userId, Long roleId);
}
