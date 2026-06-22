package io.github.genkidoudou.web.system.role.service.impl;

import io.github.genkidoudou.common.exception.WarningException;
import io.github.genkidoudou.web.system.menu.domain.SysRole;
import io.github.genkidoudou.web.system.menu.mapper.SysRoleMapper;
import io.github.genkidoudou.web.system.menu.mapper.SysRoleMenuMapper;
import io.github.genkidoudou.web.system.menu.mapper.SysUserRoleMapper;
import io.github.genkidoudou.web.system.role.dto.RoleDataScopeRequest;
import io.github.genkidoudou.web.system.role.dto.RoleMenuRequest;
import io.github.genkidoudou.web.system.role.mapper.SysRoleDeptMapper;
import io.github.genkidoudou.web.system.user.mapper.SysUserMapper;
import io.github.genkidoudou.web.system.user.service.SysUserRoleBindService;
import io.github.genkidoudou.web.system.user.authcache.UserAuthCacheService;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
/**
 * {@link SysRoleServiceImpl} 关键业务规则单测（Mock，不连库）。
 */
class SysRoleServiceImplTest {

    @Test
    void removeBatch_shouldRejectAdminRole() {
        SysRoleMapper roleMapper = mock(SysRoleMapper.class);
        SysRoleServiceImpl service = new SysRoleServiceImpl(
                roleMapper,
                mock(SysRoleMenuMapper.class),
                mock(SysRoleDeptMapper.class),
                mock(SysUserRoleMapper.class),
                mock(SysUserMapper.class),
                mock(SysUserRoleBindService.class),
                mock(UserAuthCacheService.class),
                mock(JdbcTemplate.class));

        assertThatThrownBy(() -> service.removeBatch(List.of(2L, SysRoleServiceImpl.ADMIN_ROLE_ID)))
                .isInstanceOf(WarningException.class)
                .hasMessageContaining("内置超级管理员");
    }

    @Test
    void removeBatch_shouldRejectWhenUsersAssigned() {
        SysRoleMapper roleMapper = mock(SysRoleMapper.class);
        SysUserRoleMapper userRoleMapper = mock(SysUserRoleMapper.class);
        when(userRoleMapper.selectCount(any())).thenReturn(1L);
        SysRoleServiceImpl service = new SysRoleServiceImpl(
                roleMapper,
                mock(SysRoleMenuMapper.class),
                mock(SysRoleDeptMapper.class),
                userRoleMapper,
                mock(SysUserMapper.class),
                mock(SysUserRoleBindService.class),
                mock(UserAuthCacheService.class),
                mock(JdbcTemplate.class));

        SysRole r = new SysRole();
        r.setRoleId(2L);
        r.setRoleName("测试角色");
        when(roleMapper.selectById(2L)).thenReturn(r);

        assertThatThrownBy(() -> service.removeBatch(List.of(2L)))
                .isInstanceOf(WarningException.class)
                .hasMessageContaining("已分配用户");
    }

    @Test
    void updateDataScope_shouldRejectAdmin() {
        SysRoleServiceImpl service = new SysRoleServiceImpl(
                mock(SysRoleMapper.class),
                mock(SysRoleMenuMapper.class),
                mock(SysRoleDeptMapper.class),
                mock(SysUserRoleMapper.class),
                mock(SysUserMapper.class),
                mock(SysUserRoleBindService.class),
                mock(UserAuthCacheService.class),
                mock(JdbcTemplate.class));

        RoleDataScopeRequest req = new RoleDataScopeRequest();
        req.setRoleId(SysRoleServiceImpl.ADMIN_ROLE_ID);
        req.setDataScope("3");
        req.setDeptIds(null);

        assertThatThrownBy(() -> service.updateDataScope(req))
                .isInstanceOf(WarningException.class)
                .hasMessageContaining("数据权限不允许修改");
    }

    @Test
    void updateMenus_shouldAllowAdminRole() {
        SysRoleMapper roleMapper = mock(SysRoleMapper.class);
        SysRoleMenuMapper roleMenuMapper = mock(SysRoleMenuMapper.class);
        SysRole admin = new SysRole();
        admin.setRoleId(SysRoleServiceImpl.ADMIN_ROLE_ID);
        when(roleMapper.selectById(SysRoleServiceImpl.ADMIN_ROLE_ID)).thenReturn(admin);

        SysRoleServiceImpl service = new SysRoleServiceImpl(
                roleMapper,
                roleMenuMapper,
                mock(SysRoleDeptMapper.class),
                mock(SysUserRoleMapper.class),
                mock(SysUserMapper.class),
                mock(SysUserRoleBindService.class),
                mock(UserAuthCacheService.class),
                mock(JdbcTemplate.class));

        RoleMenuRequest req = new RoleMenuRequest();
        req.setRoleId(SysRoleServiceImpl.ADMIN_ROLE_ID);
        req.setMenuIds(List.of(1L, 2L));

        service.updateMenus(req);
    }
}
