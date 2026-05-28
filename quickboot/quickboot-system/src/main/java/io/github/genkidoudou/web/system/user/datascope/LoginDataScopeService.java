package io.github.genkidoudou.web.system.user.datascope;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import io.github.genkidoudou.web.system.dept.DeptSubtreeHelper;
import io.github.genkidoudou.web.system.dept.domain.SysDept;
import io.github.genkidoudou.web.system.dept.mapper.SysDeptMapper;
import io.github.genkidoudou.web.system.menu.domain.SysRole;
import io.github.genkidoudou.web.system.menu.domain.SysUserRole;
import io.github.genkidoudou.web.system.menu.mapper.SysRoleMapper;
import io.github.genkidoudou.web.system.menu.mapper.SysUserRoleMapper;
import io.github.genkidoudou.web.system.role.domain.SysRoleDept;
import io.github.genkidoudou.web.system.role.mapper.SysRoleDeptMapper;
import io.github.genkidoudou.web.system.user.domain.SysUser;
import io.github.genkidoudou.web.system.user.mapper.SysUserMapper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 登录时按角色 {@code data_scope} 预计算数据权限并写入 Session（参照旧 quick-boot {@code SaTokenLoginService}）。
 */
@Service
public class LoginDataScopeService {

    private final SysUserMapper userMapper;
    private final SysUserRoleMapper userRoleMapper;
    private final SysRoleMapper roleMapper;
    private final SysRoleDeptMapper roleDeptMapper;
    private final SysDeptMapper deptMapper;

    public LoginDataScopeService(
            SysUserMapper userMapper,
            SysUserRoleMapper userRoleMapper,
            SysRoleMapper roleMapper,
            SysRoleDeptMapper roleDeptMapper,
            SysDeptMapper deptMapper) {
        this.userMapper = userMapper;
        this.userRoleMapper = userRoleMapper;
        this.roleMapper = roleMapper;
        this.roleDeptMapper = roleDeptMapper;
        this.deptMapper = deptMapper;
    }

    /**
     * 计算并写入当前 Sa 会话。
     *
     * @param userId 登录用户 id
     */
    public void refreshSession(long userId) {
        DataScopeSessionStore.save(compute(userId));
    }

    /**
     * 按旧 quick-boot 规则归并角色数据范围。
     *
     * @param userId 用户 id
     * @return 会话模型
     */
    public DataScopeSession compute(long userId) {
        SysUser u = userMapper.selectById(userId);
        if (u == null) {
            return new DataScopeSession(userId, null, DataScopeType.DEPT, List.of(), true);
        }
        Long loginDeptId = u.getDeptId();
        List<SysUserRole> urs = userRoleMapper.selectList(Wrappers.<SysUserRole>lambdaQuery()
                .eq(SysUserRole::getUserId, userId));
        if (urs.isEmpty()) {
            return new DataScopeSession(userId, loginDeptId, DataScopeType.DEPT, List.of(), true);
        }
        Set<Long> roleIdSet = urs.stream().map(SysUserRole::getRoleId).filter(r -> r != null && r > 0).collect(Collectors.toCollection(LinkedHashSet::new));
        if (roleIdSet.isEmpty()) {
            return new DataScopeSession(userId, loginDeptId, DataScopeType.DEPT, List.of(), true);
        }
        List<SysRole> roleEntities = roleMapper.selectList(Wrappers.<SysRole>lambdaQuery()
                .in(SysRole::getRoleId, roleIdSet)
                .eq(SysRole::getDelFlag, "0"));
        if (roleEntities.isEmpty()) {
            return new DataScopeSession(userId, loginDeptId, DataScopeType.DEPT, List.of(), true);
        }
        if (roleEntities.stream().anyMatch(a -> "1".equals(a.getDataScope()))) {
            return new DataScopeSession(userId, loginDeptId, DataScopeType.ALL, List.of(), false);
        }
        if (roleEntities.size() == 1 && "5".equals(roleEntities.get(0).getDataScope())) {
            return new DataScopeSession(userId, loginDeptId, DataScopeType.SELF, List.of(), false);
        }
        List<Long> deptIds = new ArrayList<>();
        List<Long> customRoleIds = roleEntities.stream()
                .filter(a -> "2".equals(a.getDataScope()))
                .map(SysRole::getRoleId)
                .distinct()
                .toList();
        if (!customRoleIds.isEmpty()) {
            List<SysRoleDept> rds = roleDeptMapper.selectList(Wrappers.<SysRoleDept>lambdaQuery()
                    .in(SysRoleDept::getRoleId, customRoleIds));
            for (SysRoleDept rd : rds) {
                if (rd.getDeptId() != null) {
                    deptIds.add(rd.getDeptId());
                }
            }
        }
        if (roleEntities.stream().anyMatch(a -> "3".equals(a.getDataScope()))) {
            if (loginDeptId != null) {
                deptIds.add(loginDeptId);
            }
        }
        if (roleEntities.stream().anyMatch(a -> "4".equals(a.getDataScope()))) {
            if (loginDeptId != null) {
                List<SysDept> allDepts = deptMapper.selectList(new LambdaQueryWrapper<SysDept>().orderByAsc(SysDept::getOrderNum));
                deptIds.addAll(DeptSubtreeHelper.collectDeptSubtreeIds(allDepts, loginDeptId));
            }
        }
        List<Long> distinct = deptIds.stream().distinct().toList();
        return new DataScopeSession(userId, loginDeptId, DataScopeType.DEPT, distinct, false);
    }
}
