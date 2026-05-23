package io.github.genkidoudou.web.system.menu;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import io.github.genkidoudou.common.exception.WarningException;
import io.github.genkidoudou.web.system.menu.domain.SysMenu;
import io.github.genkidoudou.web.system.menu.dto.SysMenuSaveRequest;
import io.github.genkidoudou.web.system.menu.mapper.SysMenuMapper;
import io.github.genkidoudou.web.system.menu.mapper.SysRoleMapper;
import io.github.genkidoudou.web.system.menu.mapper.SysRoleMenuMapper;
import io.github.genkidoudou.web.system.menu.mapper.SysUserRoleMapper;
import io.github.genkidoudou.web.system.menu.service.impl.MenuServiceImpl;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * {@link MenuServiceImpl} 单测（Mapper 打桩，不连库）。
 */
class MenuServiceImplTest {

    @Test
    void shouldBuildFullTreeAndPruneByMenuName() {
        SysMenuMapper menuMapper = mock(SysMenuMapper.class);
        SysRoleMapper roleMapper = mock(SysRoleMapper.class);
        SysRoleMenuMapper roleMenuMapper = mock(SysRoleMenuMapper.class);
        SysUserRoleMapper userRoleMapper = mock(SysUserRoleMapper.class);
        when(menuMapper.selectList(ArgumentMatchers.<Wrapper<SysMenu>>any())).thenReturn(sampleMenus());

        MenuServiceImpl service = new MenuServiceImpl(menuMapper, roleMapper, roleMenuMapper, userRoleMapper);

        var all = service.listTree(null, null);
        var pruned = service.listTree("菜单", null);

        assertThat(all).hasSize(1);
        assertThat(all.get(0).getChildren()).hasSize(1);
        assertThat(pruned).hasSize(1);
        assertThat(pruned.get(0).getChildren()).hasSize(1);
        assertThat(pruned.get(0).getChildren().get(0).getMenuName()).isEqualTo("菜单管理");
    }

    @Test
    void shouldUpdateParentWhenParentExists() {
        SysMenuMapper menuMapper = mock(SysMenuMapper.class);
        when(menuMapper.selectById(2001L)).thenReturn(menu(2001L, 2000L, "C", "菜单管理"));
        when(menuMapper.selectById(2000L)).thenReturn(menu(2000L, -1L, "M", "系统管理"));
        when(menuMapper.selectList(ArgumentMatchers.<Wrapper<SysMenu>>any())).thenReturn(sampleMenus());

        MenuServiceImpl service = new MenuServiceImpl(
                menuMapper, mock(SysRoleMapper.class), mock(SysRoleMenuMapper.class), mock(SysUserRoleMapper.class));

        SysMenuSaveRequest req = new SysMenuSaveRequest();
        req.setMenuId(2001L);
        req.setParentId(2000L);
        req.setMenuType("C");
        req.setMenuName("菜单管理");
        req.setOrderNum(1);
        req.setPath("menu");
        req.setComponent("system/menu/index");
        req.setVisible("0");
        req.setStatus("0");

        service.update(req);
    }

    @Test
    void shouldRejectCycleWhenUpdatingParent() {
        SysMenuMapper menuMapper = mock(SysMenuMapper.class);
        when(menuMapper.selectById(2001L)).thenReturn(menu(2001L, 2000L, "C", "菜单管理"));
        when(menuMapper.selectById(2002L)).thenReturn(menu(2002L, 2001L, "F", "按钮"));
        when(menuMapper.selectList(ArgumentMatchers.<Wrapper<SysMenu>>any())).thenReturn(sampleMenus());

        MenuServiceImpl service = new MenuServiceImpl(
                menuMapper, mock(SysRoleMapper.class), mock(SysRoleMenuMapper.class), mock(SysUserRoleMapper.class));

        SysMenuSaveRequest req = new SysMenuSaveRequest();
        req.setMenuId(2001L);
        req.setParentId(2002L);
        req.setMenuType("C");
        req.setMenuName("菜单管理");
        req.setOrderNum(1);
        req.setPath("menu");
        req.setComponent("system/menu/index");
        req.setVisible("0");
        req.setStatus("0");

        assertThatThrownBy(() -> service.update(req))
                .isInstanceOf(WarningException.class)
                .hasMessageContaining("形成环");
    }

    @Test
    void shouldRejectDeleteWhenHasChildren() {
        SysMenuMapper menuMapper = mock(SysMenuMapper.class);
        when(menuMapper.selectById(2000L)).thenReturn(menu(2000L, -1L, "M", "系统管理"));
        when(menuMapper.selectCount(ArgumentMatchers.<Wrapper<SysMenu>>any())).thenReturn(1L);

        MenuServiceImpl service = new MenuServiceImpl(
                menuMapper, mock(SysRoleMapper.class), mock(SysRoleMenuMapper.class), mock(SysUserRoleMapper.class));

        assertThatThrownBy(() -> service.remove(2000L))
                .isInstanceOf(WarningException.class)
                .hasMessageContaining("子菜单");
    }

    private static List<SysMenu> sampleMenus() {
        return List.of(
                menu(2000L, -1L, "M", "系统管理"),
                menu(2001L, 2000L, "C", "菜单管理"),
                menu(2002L, 2001L, "F", "查询"));
    }

    private static SysMenu menu(Long id, Long parentId, String type, String name) {
        SysMenu m = new SysMenu();
        m.setMenuId(id);
        m.setParentId(parentId);
        m.setMenuType(type);
        m.setMenuName(name);
        m.setOrderNum(0);
        m.setStatus("0");
        m.setVisible("0");
        m.setIsFrame("0");
        m.setIsCache("0");
        m.setDelFlag("0");
        return m;
    }
}
