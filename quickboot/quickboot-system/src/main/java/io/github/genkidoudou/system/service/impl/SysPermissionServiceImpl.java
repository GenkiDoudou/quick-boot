package io.github.genkidoudou.system.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.github.genkidoudou.system.entity.SysMenu;
import io.github.genkidoudou.system.entity.SysRole;
import io.github.genkidoudou.system.entity.SysRoleMenu;
import io.github.genkidoudou.system.entity.SysUserRole;
import io.github.genkidoudou.system.mapper.SysMenuMapper;
import io.github.genkidoudou.system.mapper.SysRoleMapper;
import io.github.genkidoudou.system.mapper.SysRoleMenuMapper;
import io.github.genkidoudou.system.mapper.SysUserRoleMapper;
import io.github.genkidoudou.system.service.ISysPermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 登录 RBAC 实现。
 * <p>所有角色（含 {@code admin}）一律按角色-菜单绑定计算权限与路由，无超级权限短路。</p>
 */
@Service
@RequiredArgsConstructor
public class SysPermissionServiceImpl implements ISysPermissionService {

  private static final String STATUS_OK = "0";

  private final SysUserRoleMapper userRoleMapper;
  private final SysRoleMapper roleMapper;
  private final SysRoleMenuMapper roleMenuMapper;
  private final SysMenuMapper menuMapper;

  @Override
  public List<String> listRoleKeys(String userId) {
    List<SysRole> roles = listEnabledRoles(userId);
    return roles.stream().map(SysRole::getRoleKey).filter(StrUtil::isNotBlank).distinct().toList();
  }

  @Override
  public Set<String> listPermissions(String userId) {
    List<SysRole> roles = listEnabledRoles(userId);
    if (roles.isEmpty()) {
      return Collections.emptySet();
    }
    List<Long> roleIds = roles.stream().map(SysRole::getRoleId).toList();
    List<SysRoleMenu> binds = roleMenuMapper.selectList(new LambdaQueryWrapper<SysRoleMenu>()
      .in(SysRoleMenu::getRoleId, roleIds));
    if (CollUtil.isEmpty(binds)) {
      return Collections.emptySet();
    }
    Set<Long> menuIds = binds.stream().map(SysRoleMenu::getMenuId).collect(Collectors.toSet());
    List<SysMenu> menus = menuMapper.selectList(new LambdaQueryWrapper<SysMenu>()
      .in(SysMenu::getMenuId, menuIds)
      .eq(SysMenu::getStatus, STATUS_OK));
    Set<String> perms = new LinkedHashSet<>();
    for (SysMenu m : menus) {
      if (StrUtil.isBlank(m.getPerms())) {
        continue;
      }
      // 按钮等可配置多个权限，英文/中文逗号分隔
      for (String part : m.getPerms().split("[,，]")) {
        if (StrUtil.isNotBlank(part)) {
          perms.add(part.trim());
        }
      }
    }
    return perms;
  }

  @Override
  public List<Map<String, Object>> buildRouters(String userId) {
    List<SysMenu> menus = listRouterMenus(userId);
    List<SysMenu> roots = menus.stream()
      .filter(m -> m.getParentId() == null || Objects.equals(m.getParentId(), 0L))
      .sorted(this::compareMenu)
      .toList();
    List<Map<String, Object>> routers = new ArrayList<>();
    for (SysMenu root : roots) {
      routers.add(toRouter(root, menus, true));
    }
    return routers;
  }

  @Override
  public List<SysMenu> listAllEnabledMenus() {
    return menuMapper.selectList(new LambdaQueryWrapper<SysMenu>()
      .eq(SysMenu::getStatus, STATUS_OK)
      .orderByAsc(SysMenu::getParentId)
      .orderByAsc(SysMenu::getOrderNum));
  }

  @Override
  public List<Long> listMenuIdsByRoleId(Long roleId) {
    if (roleId == null) {
      return List.of();
    }
    return roleMenuMapper.selectList(new LambdaQueryWrapper<SysRoleMenu>()
        .eq(SysRoleMenu::getRoleId, roleId))
      .stream()
      .map(SysRoleMenu::getMenuId)
      .toList();
  }

  private List<SysRole> listEnabledRoles(String userId) {
    if (StrUtil.isBlank(userId)) {
      return List.of();
    }
    List<SysUserRole> urs = userRoleMapper.selectList(new LambdaQueryWrapper<SysUserRole>()
      .eq(SysUserRole::getUserId, userId.trim()));
    if (CollUtil.isEmpty(urs)) {
      return List.of();
    }
    List<Long> roleIds = urs.stream().map(SysUserRole::getRoleId).toList();
    return roleMapper.selectList(new LambdaQueryWrapper<SysRole>()
      .in(SysRole::getRoleId, roleIds)
      .eq(SysRole::getStatus, STATUS_OK));
  }

  private List<SysMenu> listRouterMenus(String userId) {
    List<SysRole> roles = listEnabledRoles(userId);
    if (roles.isEmpty()) {
      return List.of();
    }
    List<Long> roleIds = roles.stream().map(SysRole::getRoleId).toList();
    List<SysRoleMenu> binds = roleMenuMapper.selectList(new LambdaQueryWrapper<SysRoleMenu>()
      .in(SysRoleMenu::getRoleId, roleIds));
    if (CollUtil.isEmpty(binds)) {
      return List.of();
    }
    Set<Long> menuIds = binds.stream().map(SysRoleMenu::getMenuId).collect(Collectors.toSet());
    return menuMapper.selectList(new LambdaQueryWrapper<SysMenu>()
      .eq(SysMenu::getStatus, STATUS_OK)
      .in(SysMenu::getMenuType, List.of("M", "C"))
      .in(SysMenu::getMenuId, menuIds)
      .orderByAsc(SysMenu::getParentId)
      .orderByAsc(SysMenu::getOrderNum));
  }

  private Map<String, Object> toRouter(SysMenu menu, List<SysMenu> all, boolean isRoot) {
    Map<String, Object> node = new LinkedHashMap<>();
    String routeName = StrUtil.blankToDefault(menu.getRouteName(), "Menu" + menu.getMenuId());
    node.put("name", routeName);
    boolean isDir = "M".equals(menu.getMenuType());
    boolean isFrame = "1".equals(menu.getIsFrame());
    String path = StrUtil.blankToDefault(menu.getPath(), "menu" + menu.getMenuId());
    boolean httpPath = isHttpUrl(path);

    if (isRoot && isDir) {
      String p = StrUtil.blankToDefault(menu.getPath(), "system");
      node.put("path", p.startsWith("/") ? p : "/" + p);
      node.put("component", "Layout");
      node.put("redirect", "noRedirect");
      node.put("alwaysShow", true);
    } else if (isFrame && httpPath) {
      // 外链：侧栏内嵌 InnerLink，meta.link 为完整 URL
      node.put("path", encodeInnerLinkPath(path));
      node.put("component", "InnerLink");
    } else {
      node.put("path", path);
      node.put("component", StrUtil.blankToDefault(menu.getComponent(), "ParentView"));
    }

    // visible：0=显示 1=隐藏
    node.put("hidden", "1".equals(menu.getVisible()));

    if (StrUtil.isNotBlank(menu.getQuery())) {
      node.put("query", menu.getQuery().trim());
    }

    Map<String, Object> meta = new LinkedHashMap<>();
    meta.put("title", menu.getMenuName());
    meta.put("icon", StrUtil.blankToDefault(menu.getIcon(), ""));
    // is_cache：0=缓存 1=不缓存 → noCache
    meta.put("noCache", "1".equals(menu.getIsCache()));
    if (isFrame && httpPath) {
      meta.put("link", path);
    }
    node.put("meta", meta);

    List<SysMenu> children = all.stream()
      .filter(m -> Objects.equals(m.getParentId(), menu.getMenuId()))
      .sorted(this::compareMenu)
      .toList();
    if (!children.isEmpty()) {
      List<Map<String, Object>> childRouters = new ArrayList<>();
      for (SysMenu c : children) {
        childRouters.add(toRouter(c, all, false));
      }
      node.put("children", childRouters);
    } else if (isRoot && isFrame && httpPath) {
      // 顶级外链由前端 wrapRootInnerLinkRaw 再包一层 Layout
    }
    return node;
  }

  private static boolean isHttpUrl(String path) {
    if (StrUtil.isBlank(path)) {
      return false;
    }
    String p = path.trim().toLowerCase();
    return p.startsWith("http://") || p.startsWith("https://");
  }

  /**
   * InnerLink 路由 path 不能含 ://，做简单替换供前端注册。
   */
  private static String encodeInnerLinkPath(String url) {
    return url.replace("://", "/").replaceAll("^https?", "http");
  }

  private int compareMenu(SysMenu a, SysMenu b) {
    int oa = a.getOrderNum() == null ? 0 : a.getOrderNum();
    int ob = b.getOrderNum() == null ? 0 : b.getOrderNum();
    return Integer.compare(oa, ob);
  }
}
