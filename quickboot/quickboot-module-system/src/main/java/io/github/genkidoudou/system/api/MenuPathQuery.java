package io.github.genkidoudou.system.api;

import java.util.List;

/**
 * 菜单路由只读查询（跨模块消费入口；不暴露持久化实体）。
 * <p>
 * 供监控等域将前端 {@code pagePath} 解析为菜单名/面包屑。
 */
public interface MenuPathQuery {

  /**
   * 列出状态正常的目录（M）与菜单（C）节点，按 {@code orderNum} 升序。
   *
   * @return 路由节点列表；无数据时为空列表
   */
  List<MenuRouteNode> listActiveDirAndMenuRoutes();
}
