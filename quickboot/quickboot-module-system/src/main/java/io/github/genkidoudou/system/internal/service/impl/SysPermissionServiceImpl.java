package io.github.genkidoudou.system.internal.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.github.genkidoudou.system.internal.entity.SysMenu;
import io.github.genkidoudou.system.internal.entity.SysRole;
import io.github.genkidoudou.system.internal.entity.SysRoleMenu;
import io.github.genkidoudou.system.internal.entity.SysUserRole;
import io.github.genkidoudou.system.internal.mapper.SysMenuMapper;
import io.github.genkidoudou.system.internal.mapper.SysRoleMapper;
import io.github.genkidoudou.system.internal.mapper.SysRoleMenuMapper;
import io.github.genkidoudou.system.internal.mapper.SysUserRoleMapper;
import io.github.genkidoudou.system.internal.service.ISysPermissionService;
import io.github.genkidoudou.system.internal.vo.H5WorkbenchGroupVo;
import io.github.genkidoudou.system.internal.vo.H5WorkbenchItemVo;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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

  @Value("${qc.jimu.enabled:true}")
  private boolean jimuEnabled;

  @Value("${qc.jimu.base-url:http://localhost:9993}")
  private String jimuBaseUrl;

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
    // PC 侧栏排除 H5 uni 页面节点（path 以 /pages/ 开头）
    List<SysMenu> menus = listRouterMenus(userId).stream()
      .filter(m -> !"C".equals(m.getMenuType()) || !isH5PagePath(m.getPath()))
      .collect(Collectors.toList());
    List<SysMenu> roots = menus.stream()
      .filter(this::isRouterRoot)
      .sorted(this::compareMenu)
      .toList();
    List<Map<String, Object>> routers = new ArrayList<>();
    for (SysMenu root : roots) {
      routers.add(toRouter(root, menus, true));
    }
    return pruneEmptyDirectories(routers);
  }

  @Override
  public List<H5WorkbenchItemVo> listH5PageItems(String userId) {
    List<SysMenu> menus = listRouterMenus(userId);
    List<H5WorkbenchItemVo> items = new ArrayList<>();
    for (SysMenu m : menus) {
      if (!isH5PageMenu(m)) {
        continue;
      }
      H5WorkbenchItemVo item = new H5WorkbenchItemVo();
      item.setId(String.valueOf(m.getMenuId()));
      item.setLabel(m.getMenuName());
      item.setPath(m.getPath().trim());
      item.setIcon(StrUtil.blankToDefault(m.getIcon(), ""));
      item.setOrderNum(m.getOrderNum());
      items.add(item);
    }
    items.sort(Comparator.comparingInt(i -> i.getOrderNum() == null ? 0 : i.getOrderNum()));
    return items;
  }

  @Override
  public List<H5WorkbenchGroupVo> buildH5Workbench(String userId) {
    List<SysMenu> menus = listRouterMenus(userId);
    Map<Long, SysMenu> byId = menus.stream()
      .collect(Collectors.toMap(SysMenu::getMenuId, m -> m, (a, b) -> a, LinkedHashMap::new));
    Map<Long, H5WorkbenchGroupVo> groups = new LinkedHashMap<>();
    for (SysMenu m : menus) {
      if (!isH5PageMenu(m)) {
        continue;
      }
      SysMenu parent = byId.get(m.getParentId());
      Long groupId = parent != null ? parent.getMenuId() : 0L;
      String title = parent != null ? parent.getMenuName() : "工作台";
      Integer groupOrder = parent != null ? parent.getOrderNum() : 0;
      H5WorkbenchGroupVo group = groups.computeIfAbsent(groupId, id -> {
        H5WorkbenchGroupVo g = new H5WorkbenchGroupVo();
        g.setId(String.valueOf(id));
        g.setTitle(title);
        g.setOrderNum(groupOrder);
        return g;
      });
      H5WorkbenchItemVo item = new H5WorkbenchItemVo();
      item.setId(String.valueOf(m.getMenuId()));
      item.setLabel(m.getMenuName());
      item.setPath(m.getPath().trim());
      item.setIcon(StrUtil.blankToDefault(m.getIcon(), ""));
      item.setOrderNum(m.getOrderNum());
      group.getItems().add(item);
    }
    List<H5WorkbenchGroupVo> result = new ArrayList<>(groups.values());
    result.sort(Comparator.comparingInt(g -> g.getOrderNum() == null ? 0 : g.getOrderNum()));
    for (H5WorkbenchGroupVo g : result) {
      g.getItems().sort(Comparator.comparingInt(i -> i.getOrderNum() == null ? 0 : i.getOrderNum()));
    }
    // 去掉无入口的空分组
    return result.stream().filter(g -> !g.getItems().isEmpty()).toList();
  }

  /**
   * H5 页面菜单：C + path 以 /pages/ 开头 + visible≠隐藏。
   */
  private static boolean isH5PageMenu(SysMenu m) {
    if (m == null || !"C".equals(m.getMenuType())) {
      return false;
    }
    if (!isH5PagePath(m.getPath())) {
      return false;
    }
    // visible：0=显示 1=隐藏
    return !"1".equals(m.getVisible());
  }

  /** H5 uni 页面 path 约定：必须以 /pages/ 开头 */
  private static boolean isH5PagePath(String path) {
    return StrUtil.isNotBlank(path) && path.trim().startsWith("/pages/");
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
    List<SysMenu> bound = menuMapper.selectList(new LambdaQueryWrapper<SysMenu>()
      .eq(SysMenu::getStatus, STATUS_OK)
      .in(SysMenu::getMenuType, List.of("M", "C"))
      .in(SysMenu::getMenuId, menuIds));
    // 已授权叶子/中间节点时，自动补齐祖先目录，避免挪菜单纯缺中间 M 导致整枝不显示
    Map<Long, SysMenu> byId = new LinkedHashMap<>();
    for (SysMenu m : bound) {
      byId.put(m.getMenuId(), m);
    }
    Set<Long> pendingParents = new LinkedHashSet<>();
    for (SysMenu m : bound) {
      Long pid = m.getParentId();
      if (isRouterRootParent(pid)) {
        continue;
      }
      if (!byId.containsKey(pid)) {
        pendingParents.add(pid);
      }
    }
    while (!pendingParents.isEmpty()) {
      List<SysMenu> parents = menuMapper.selectList(new LambdaQueryWrapper<SysMenu>()
        .in(SysMenu::getMenuId, pendingParents)
        .eq(SysMenu::getStatus, STATUS_OK)
        .in(SysMenu::getMenuType, List.of("M", "C")));
      pendingParents.clear();
      for (SysMenu parent : parents) {
        if (byId.putIfAbsent(parent.getMenuId(), parent) != null) {
          continue;
        }
        Long pid = parent.getParentId();
        if (!isRouterRootParent(pid) && !byId.containsKey(pid)) {
          pendingParents.add(pid);
        }
      }
    }
    return byId.values().stream()
      .sorted(this::compareMenu)
      .collect(Collectors.toList());
  }

  /** 顶级：null / 0 / -1（前端主类目约定） */
  private boolean isRouterRootParent(Long parentId) {
    return parentId == null || parentId == 0L || parentId == -1L;
  }

  private boolean isRouterRoot(SysMenu menu) {
    return isRouterRootParent(menu.getParentId());
  }

  private Map<String, Object> toRouter(SysMenu menu, List<SysMenu> all, boolean isRoot) {
    Map<String, Object> node = new LinkedHashMap<>();
    String routeName = StrUtil.blankToDefault(menu.getRouteName(), "Menu" + menu.getMenuId());
    node.put("name", routeName);
    boolean isDir = "M".equals(menu.getMenuType());
    boolean isFrame = "1".equals(menu.getIsFrame());
    // 目录 path 不能为空，否则嵌套 ParentView 扁平化后子路由会变成绝对 path，侧栏点击 404
    String path = StrUtil.blankToDefault(StrUtil.trim(menu.getPath()), "menu" + menu.getMenuId());
    String frameLink = isFrame ? resolveFrameLink(menu) : null;
    boolean hasFrameLink = StrUtil.isNotBlank(frameLink);

    if (isRoot && isDir) {
      String p = path;
      node.put("path", p.startsWith("/") ? p : "/" + p);
      node.put("component", "Layout");
      node.put("redirect", "noRedirect");
      node.put("alwaysShow", true);
    } else if (isFrame && hasFrameLink) {
      // 内嵌 iframe：完整地址只放 meta.link；路由 path 用菜单相对段，避免嵌套成 /父/http/host/... → 404
      String routePath;
      if (isHttpUrl(path)) {
        routePath = encodeInnerLinkPath(path);
      } else {
        routePath = path.startsWith("/") ? path.substring(1) : path;
        if (StrUtil.isBlank(routePath)) {
          routePath = "link" + menu.getMenuId();
        }
      }
      if (isRoot) {
        node.put("path", routePath.startsWith("/") ? routePath : "/" + routePath);
      } else {
        node.put("path", routePath.startsWith("/") ? routePath.substring(1) : routePath);
      }
      node.put("component", "InnerLink");
    } else {
      // 非顶级 path 用相对段，避免带前导 / 被 vue-router 当成绝对路径
      node.put("path", path.startsWith("/") ? path.substring(1) : path);
      if (isDir) {
        // 非顶级目录默认 ParentView，并始终展开，避免「单子节点」被侧栏折叠成空叶子点进 404
        String dirComponent = StrUtil.blankToDefault(menu.getComponent(), "ParentView");
        if ("Layout".equals(dirComponent)) {
          dirComponent = "ParentView";
        }
        node.put("component", dirComponent);
        node.put("alwaysShow", true);
      } else {
        node.put("component", StrUtil.blankToDefault(menu.getComponent(), "ParentView"));
      }
    }

    // visible：0=显示 1=隐藏
    node.put("hidden", "1".equals(menu.getVisible()));

    // 积木/BI 的 query 是路径串（如 /drag/view?pageId=），已拼进 meta.link，勿再当作 vue-router query
    if (StrUtil.isNotBlank(menu.getQuery()) && !(isFrame && hasFrameLink)) {
      node.put("query", menu.getQuery().trim());
    }

    Map<String, Object> meta = new LinkedHashMap<>();
    meta.put("title", menu.getMenuName());
    meta.put("icon", StrUtil.blankToDefault(menu.getIcon(), ""));
    // is_cache：0=缓存 1=不缓存 → noCache
    meta.put("noCache", "1".equals(menu.getIsCache()));
    if (isFrame && hasFrameLink) {
      meta.put("link", frameLink);
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
    } else if (isRoot && isFrame && hasFrameLink) {
      // 顶级外链由前端 wrapRootInnerLinkRaw 再包一层 Layout
    }
    return node;
  }

  /**
   * 去掉无子节点的目录（ParentView/Layout），避免侧栏出现可点进 404 的空目录。
   */
  @SuppressWarnings("unchecked")
  private List<Map<String, Object>> pruneEmptyDirectories(List<Map<String, Object>> routers) {
    if (routers == null || routers.isEmpty()) {
      return routers;
    }
    List<Map<String, Object>> result = new ArrayList<>();
    for (Map<String, Object> node : routers) {
      Object rawChildren = node.get("children");
      if (rawChildren instanceof List<?> list && !list.isEmpty()) {
        List<Map<String, Object>> children = pruneEmptyDirectories((List<Map<String, Object>>) list);
        if (children.isEmpty()) {
          node.remove("children");
        } else {
          node.put("children", children);
        }
      }
      if (isDirectoryRouter(node)) {
        Object children = node.get("children");
        if (!(children instanceof List<?> c) || c.isEmpty()) {
          continue;
        }
      }
      result.add(node);
    }
    return result;
  }

  private static boolean isDirectoryRouter(Map<String, Object> node) {
    Object component = node.get("component");
    return "Layout".equals(component) || "ParentView".equals(component);
  }

  /**
   * 积木菜单：query（如 /jmreport/list）拼 qc.jimu.base-url；绝对 URL 直通。
   */
  private String resolveFrameLink(SysMenu menu) {
    String linkPath = StrUtil.trim(menu.getQuery());
    if (StrUtil.isBlank(linkPath)) {
      linkPath = StrUtil.trim(menu.getPath());
    }
    if (StrUtil.isBlank(linkPath)) {
      return null;
    }
    if (isHttpUrl(linkPath)) {
      return linkPath;
    }
    String base = "";
    if (jimuEnabled && StrUtil.isNotBlank(jimuBaseUrl)) {
      base = StrUtil.removeSuffix(jimuBaseUrl.trim(), "/");
    }
    if (linkPath.startsWith("/")) {
      return base + linkPath;
    }
    return base + "/" + linkPath;
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
