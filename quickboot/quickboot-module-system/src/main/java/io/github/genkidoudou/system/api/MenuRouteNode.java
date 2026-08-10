package io.github.genkidoudou.system.api;

/**
 * 跨模块菜单路由节点（仅目录/菜单，不含按钮；不含持久化实体）。
 *
 * @param menuId   菜单 ID
 * @param parentId 父菜单 ID；顶级为 {@code 0}
 * @param menuName 菜单名称
 * @param menuType {@code M}=目录 / {@code C}=菜单
 * @param path     路由 path 段
 * @param orderNum 显示顺序，可为 {@code null}
 */
public record MenuRouteNode(
  Long menuId,
  Long parentId,
  String menuName,
  String menuType,
  String path,
  Integer orderNum
) {
}
