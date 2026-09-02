package io.github.genkidoudou.web.system;

import io.github.genkidoudou.system.internal.controller.MenuRouteController;
import io.github.genkidoudou.system.internal.service.ISysPermissionService;
import io.github.genkidoudou.web.support.QuickbootIntegrationTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 动态菜单路由冒烟：admin 用户 {@link ISysPermissionService#buildRouters} 产出可挂载的前端路由树。
 * <p>
 * HTTP 层 {@link MenuRouteController} 在集成环境需 Sa-Token Web 上下文；此处验证路由组装核心逻辑与 Controller Bean 就绪。
 */
class MenuRouteIT extends QuickbootIntegrationTestBase {

  @Autowired
  private ISysPermissionService permissionService;

  @Autowired
  private MenuRouteController menuRouteController;

  @Test
  void adminBuildRoutersReturnsNonEmptyTree() {
    List<Map<String, Object>> routes = permissionService.buildRouters("1");
    assertNotNull(routes);
    assertFalse(routes.isEmpty(), "admin 应至少有一条动态路由");
    Map<String, Object> root = routes.get(0);
    assertNotNull(root.get("path"));
    assertNotNull(root.get("component"), "路由节点须含 component 供前端 lazy load");
  }

  @Test
  void menuRouteControllerBeanReady() {
    assertNotNull(menuRouteController);
  }

  @Test
  void adminRoutesContainLeafPageComponent() {
    List<Map<String, Object>> routes = permissionService.buildRouters("1");
    assertTrue(hasLeafWithPageComponent(routes), "应存在非 Layout 的页面 component");
  }

  @Test
  void adminRoutesContainHiddenPageRoutes() {
    List<Map<String, Object>> routes = permissionService.buildRouters("1");
    assertTrue(hasRouteName(routes, "SysDictData"), "字典数据 hidden 路由应由 sys_menu 提供");
    assertTrue(hasRouteName(routes, "SysUserAuthRole"), "分配角色 hidden 路由应由 sys_menu 提供");
    assertTrue(hasRouteName(routes, "ToolGenEdit"), "代码生成编辑 hidden 路由应由 sys_menu 提供");
  }

  private static boolean hasRouteName(List<Map<String, Object>> nodes, String name) {
    if (nodes == null) {
      return false;
    }
    for (Map<String, Object> node : nodes) {
      if (name.equals(node.get("name"))) {
        return true;
      }
      @SuppressWarnings("unchecked")
      List<Map<String, Object>> children = (List<Map<String, Object>>) node.get("children");
      if (hasRouteName(children, name)) {
        return true;
      }
    }
    return false;
  }

  private static boolean hasLeafWithPageComponent(List<Map<String, Object>> nodes) {
    if (nodes == null) {
      return false;
    }
    for (Map<String, Object> node : nodes) {
      Object component = node.get("component");
      if (component != null && !"Layout".equals(component) && !"ParentView".equals(component)) {
        return true;
      }
      @SuppressWarnings("unchecked")
      List<Map<String, Object>> children = (List<Map<String, Object>>) node.get("children");
      if (hasLeafWithPageComponent(children)) {
        return true;
      }
    }
    return false;
  }
}
