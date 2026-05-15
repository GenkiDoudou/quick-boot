package io.github.genkidoudou.web.system.user.datascope;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 登录后写入 Sa-Token Session 的数据权限快照（参照旧 quick-boot 登录阶段预计算模型）。
 *
 * @param userId          当前用户 id
 * @param loginDeptId     用户所属部门（{@code sys_user.dept_id}）
 * @param scopeType       归并后的范围类型
 * @param visibleDeptIds  部门范围内可见的部门 id（ALL 时可为空）
 * @param denyAll         无角色等场景下强制查不到数据
 */
public record DataScopeSession(
        long userId,
        Long loginDeptId,
        DataScopeType scopeType,
        List<Long> visibleDeptIds,
        boolean denyAll) implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 规范化列表为不可变副本。
     */
    public DataScopeSession {
        visibleDeptIds = visibleDeptIds == null ? List.of() : List.copyOf(visibleDeptIds);
    }
}
