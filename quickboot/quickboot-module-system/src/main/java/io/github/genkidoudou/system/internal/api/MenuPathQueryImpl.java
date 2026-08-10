package io.github.genkidoudou.system.internal.api;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import io.github.genkidoudou.system.api.MenuPathQuery;
import io.github.genkidoudou.system.api.MenuRouteNode;
import io.github.genkidoudou.system.internal.entity.SysMenu;
import io.github.genkidoudou.system.internal.mapper.SysMenuMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * {@link MenuPathQuery} 实现：查询启用中的目录/菜单路由节点。
 */
@Service
@RequiredArgsConstructor
public class MenuPathQueryImpl implements MenuPathQuery {

  private static final String TYPE_DIR = "M";
  private static final String TYPE_MENU = "C";
  private static final String STATUS_NORMAL = "0";

  private final SysMenuMapper sysMenuMapper;

  @Override
  public List<MenuRouteNode> listActiveDirAndMenuRoutes() {
    List<SysMenu> rows = sysMenuMapper.selectList(
      Wrappers.<SysMenu>lambdaQuery()
        .in(SysMenu::getMenuType, TYPE_DIR, TYPE_MENU)
        .eq(SysMenu::getStatus, STATUS_NORMAL)
        .orderByAsc(SysMenu::getOrderNum));
    return rows.stream()
      .map(m -> new MenuRouteNode(
        m.getMenuId(),
        m.getParentId() == null ? 0L : m.getParentId(),
        m.getMenuName(),
        m.getMenuType(),
        m.getPath(),
        m.getOrderNum()))
      .toList();
  }
}
