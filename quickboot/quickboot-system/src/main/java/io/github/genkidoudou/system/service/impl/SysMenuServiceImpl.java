package io.github.genkidoudou.system.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.github.genkidoudou.common.excel.ExcelUtils;
import io.github.genkidoudou.common.excel.exception.ExcelDataCheckException;
import io.github.genkidoudou.common.excel.listener.ExcelResult;
import io.github.genkidoudou.common.exception.ErrorCodes;
import io.github.genkidoudou.common.exception.WarningException;
import io.github.genkidoudou.common.mybatisplus.BaseServiceImpl;
import io.github.genkidoudou.system.entity.SysMenu;
import io.github.genkidoudou.system.entity.SysRoleMenu;
import io.github.genkidoudou.system.mapper.SysMenuMapper;
import io.github.genkidoudou.system.mapper.SysRoleMenuMapper;
import io.github.genkidoudou.system.service.ISysMenuService;
import io.github.genkidoudou.system.service.ISysPermissionService;
import io.github.genkidoudou.system.vo.MenuSortVo;
import io.github.genkidoudou.system.vo.MenuTreeSelectVo;
import io.github.genkidoudou.system.vo.SysMenuImportRow;
import io.github.genkidoudou.system.vo.SysMenuVo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 系统菜单管理实现。
 */
@Service
@RequiredArgsConstructor
public class SysMenuServiceImpl extends BaseServiceImpl<SysMenuMapper, SysMenu> implements ISysMenuService {

  private static final Set<String> MENU_TYPES = Set.of("M", "C", "F");

  public static final int IMPORT_MAX_ROWS = 5000;

  private final ISysPermissionService permissionService;
  private final SysRoleMenuMapper sysRoleMenuMapper;

  @Override
  public List<SysMenuVo> listTree(String menuName, String status) {
    LambdaQueryWrapper<SysMenu> q = new LambdaQueryWrapper<SysMenu>()
      .orderByAsc(SysMenu::getParentId)
      .orderByAsc(SysMenu::getOrderNum);
    if (StrUtil.isNotBlank(menuName)) {
      q.like(SysMenu::getMenuName, menuName.trim());
    }
    if (StrUtil.isNotBlank(status)) {
      q.eq(SysMenu::getStatus, status.trim());
    }
    List<SysMenu> all = this.list(q);
    // 名称过滤时返回扁平匹配节点及其祖先，便于树表展示
    if (StrUtil.isNotBlank(menuName)) {
      all = expandWithAncestors(all);
    }
    return buildVoTree(all, 0L);
  }

  @Override
  public List<MenuTreeSelectVo> treeselect(boolean excludeButton, boolean directoryOnly) {
    LambdaQueryWrapper<SysMenu> q = new LambdaQueryWrapper<SysMenu>()
      .eq(SysMenu::getStatus, "0")
      .orderByAsc(SysMenu::getParentId)
      .orderByAsc(SysMenu::getOrderNum);
    if (directoryOnly) {
      q.eq(SysMenu::getMenuType, "M");
    } else if (excludeButton) {
      q.in(SysMenu::getMenuType, List.of("M", "C"));
    }
    List<SysMenu> all = this.list(q);
    return buildSelectTree(all, 0L);
  }

  @Override
  public Map<String, Object> roleMenuTreeselect(Long roleId) {
    if (roleId == null) {
      throw new WarningException(ErrorCodes.Menu.INVALID_PARAM, "roleId");
    }
    List<SysMenu> all = permissionService.listAllEnabledMenus();
    List<MenuTreeSelectVo> menus = buildSelectTree(all, 0L);
    Map<String, Object> result = new LinkedHashMap<>();
    result.put("menus", menus);
    result.put("checkedKeys", permissionService.listMenuIdsByRoleId(roleId));
    return result;
  }

  @Override
  public SysMenuVo getDetail(Long menuId) {
    if (menuId == null) {
      throw new WarningException(ErrorCodes.Menu.INVALID_PARAM, "menuId");
    }
    requireMenu(menuId);
    return getVoById(menuId, SysMenuVo.class);
  }

  @Override
  public Long add(SysMenuVo vo) {
    normalizeAndValidate(vo, true);
    SysMenu entity = toEntity(vo);
    entity.setMenuId(null);
    applyDefaults(entity, vo);
    boolean save = this.save(entity);
    return save ? entity.getMenuId() : null;
  }

  @Override
  public boolean update(SysMenuVo vo) {
    SysMenu existing = requireMenu(vo.getMenuId());
    normalizeAndValidate(vo, false);
    if (Objects.equals(vo.getParentId(), vo.getMenuId())) {
      throw new WarningException(ErrorCodes.Menu.PARENT_SELF);
    }
    SysMenu entity = toEntity(vo);
    entity.setMenuId(existing.getMenuId());
    applyDefaults(entity, vo);
    return super.updateById(entity);
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public void updateSort(MenuSortVo sort) {
    if (sort == null || CollUtil.isEmpty(sort.getMenuIds()) || CollUtil.isEmpty(sort.getOrderNums())) {
      throw new WarningException(ErrorCodes.Menu.INVALID_PARAM, "menuIds/orderNums");
    }
    if (sort.getMenuIds().size() != sort.getOrderNums().size()) {
      throw new WarningException(ErrorCodes.Menu.INVALID_PARAM, "menuIds与orderNums长度不一致");
    }
    for (int i = 0; i < sort.getMenuIds().size(); i++) {
      Long id = sort.getMenuIds().get(i);
      Integer order = sort.getOrderNums().get(i);
      if (id == null) {
        continue;
      }
      SysMenu patch = new SysMenu();
      patch.setMenuId(id);
      patch.setOrderNum(order == null ? 0 : order);
      this.updateById(patch);
    }
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public void remove(Collection<Long> menuIds) {
    if (CollectionUtil.isEmpty(menuIds)) {
      return;
    }
    List<Long> idList = menuIds.stream()
      .filter(Objects::nonNull)
      .distinct()
      .toList();
    if (idList.isEmpty()) {
      return;
    }
    for (Long menuId : idList) {
      requireMenu(menuId);
      long children = this.count(new LambdaQueryWrapper<SysMenu>().eq(SysMenu::getParentId, menuId));
      if (children > 0) {
        throw new WarningException(ErrorCodes.Menu.HAS_CHILDREN);
      }
      this.removeById(menuId);
    }
  }

  @Override
  public List<SysMenuVo> listByRoles(List<Long> roleIds) {
    if (CollUtil.isEmpty(roleIds)) {
      return new ArrayList<>();
    }
    List<SysRoleMenu> sysRoleMenus = sysRoleMenuMapper.selectList(new LambdaQueryWrapper<SysRoleMenu>()
      .in(SysRoleMenu::getRoleId, roleIds));
    if (CollUtil.isEmpty(sysRoleMenus)) {
      return new ArrayList<>();
    }
    Set<Long> menuIds = sysRoleMenus.stream().map(SysRoleMenu::getMenuId).collect(Collectors.toSet());
    return this.listByIds(menuIds).stream()
      .map(menu -> toVo(menu, SysMenuVo.class))
      .collect(Collectors.toList());
  }

  @Override
  public List<SysMenuVo> export(SysMenuVo query) {
    List<SysMenu> entities = listForExport(query == null ? new SysMenuVo() : query);
    if (entities.size() > IMPORT_MAX_ROWS) {
      throw WarningException.literal(ErrorCodes.Common.INVALID_PARAM, "导出条数超过上限 " + IMPORT_MAX_ROWS);
    }
    return entities.stream()
      .map(m -> toVo(m, SysMenuVo.class))
      .collect(Collectors.toList());
  }

  private List<SysMenu> listForExport(SysMenuVo query) {
    List<Long> ids = query.getIds() == null ? Collections.emptyList() : query.getIds().stream()
      .filter(Objects::nonNull)
      .distinct()
      .collect(Collectors.toList());
    if (!ids.isEmpty()) {
      return this.listByIds(ids);
    }
    LambdaQueryWrapper<SysMenu> q = new LambdaQueryWrapper<SysMenu>()
      .orderByAsc(SysMenu::getParentId)
      .orderByAsc(SysMenu::getOrderNum);
    if (StrUtil.isNotBlank(query.getMenuName())) {
      q.like(SysMenu::getMenuName, query.getMenuName().trim());
    }
    if (StrUtil.isNotBlank(query.getStatus())) {
      q.eq(SysMenu::getStatus, query.getStatus().trim());
    }
    return this.list(q);
  }

  private void normalizeAndValidate(SysMenuVo vo, boolean isAdd) {
    if (vo == null) {
      throw new WarningException(ErrorCodes.Menu.INVALID_PARAM, "body");
    }
    String type = vo.getMenuType() == null ? null : vo.getMenuType().trim();
    if (!MENU_TYPES.contains(type)) {
      throw new WarningException(ErrorCodes.Menu.TYPE_INVALID);
    }
    vo.setMenuType(type);
    if (vo.getParentId() == null) {
      vo.setParentId(0L);
    }
    if (vo.getParentId() != 0L) {
      requireMenu(vo.getParentId());
    }
    if (StrUtil.isNotBlank(vo.getMenuName())) {
      vo.setMenuName(vo.getMenuName().trim());
    }
    vo.setPerms(normalizePerms(vo.getPerms()));
    if (isAdd && StrUtil.isBlank(vo.getStatus())) {
      vo.setStatus("0");
    }
  }

  /**
   * 权限字符规范化：中文逗号转英文、去空白、去重、逗号拼接。
   *
   * @param raw 原始串
   * @return 规范化结果；空则 null
   */
  private static String normalizePerms(String raw) {
    if (StrUtil.isBlank(raw)) {
      return null;
    }
    String[] parts = raw.replace('，', ',').split(",");
    LinkedHashSet<String> uniq = new LinkedHashSet<>();
    for (String p : parts) {
      if (StrUtil.isNotBlank(p)) {
        uniq.add(p.trim());
      }
    }
    if (uniq.isEmpty()) {
      return null;
    }
    return String.join(",", uniq);
  }

  private void applyDefaults(SysMenu entity, SysMenuVo vo) {
    entity.setMenuName(vo.getMenuName().trim());
    entity.setMenuType(vo.getMenuType());
    entity.setParentId(vo.getParentId() == null ? 0L : vo.getParentId());
    entity.setOrderNum(vo.getOrderNum() == null ? 0 : vo.getOrderNum());
    entity.setIsFrame(StrUtil.blankToDefault(vo.getIsFrame(), "0"));
    entity.setIsCache(StrUtil.blankToDefault(vo.getIsCache(), "0"));
    entity.setVisible(StrUtil.blankToDefault(vo.getVisible(), "0"));
    entity.setStatus(StrUtil.blankToDefault(vo.getStatus(), "0"));
    if ("F".equals(vo.getMenuType())) {
      entity.setPath(null);
      entity.setComponent(null);
      entity.setRouteName(null);
      entity.setIcon(null);
      entity.setQuery(null);
      entity.setIsFrame("0");
      entity.setIsCache("0");
      entity.setVisible("0");
    }
  }

  private SysMenu requireMenu(Long menuId) {
    SysMenu menu = this.getById(menuId);
    if (menu == null) {
      throw new WarningException(ErrorCodes.Menu.NOT_FOUND, menuId);
    }
    return menu;
  }

  private List<SysMenu> expandWithAncestors(List<SysMenu> matched) {
    if (CollUtil.isEmpty(matched)) {
      return List.of();
    }
    Map<Long, SysMenu> allMap = this.list().stream()
      .collect(Collectors.toMap(SysMenu::getMenuId, m -> m, (a, b) -> a, LinkedHashMap::new));
    Map<Long, SysMenu> result = new LinkedHashMap<>();
    for (SysMenu m : matched) {
      SysMenu cur = m;
      while (cur != null && !result.containsKey(cur.getMenuId())) {
        result.put(cur.getMenuId(), cur);
        Long pid = cur.getParentId();
        if (pid == null || Objects.equals(pid, 0L)) {
          break;
        }
        cur = allMap.get(pid);
      }
    }
    return new ArrayList<>(result.values());
  }

  private List<SysMenuVo> buildVoTree(List<SysMenu> all, Long parentId) {
    List<SysMenuVo> list = new ArrayList<>();
    for (SysMenu m : all) {
      Long pid = m.getParentId() == null ? 0L : m.getParentId();
      if (!Objects.equals(pid, parentId)) {
        continue;
      }
      SysMenuVo vo = BeanUtil.copyProperties(m, SysMenuVo.class);
      vo.setChildren(buildVoTree(all, m.getMenuId()));
      list.add(vo);
    }
    list.sort((a, b) -> {
      int oa = a.getOrderNum() == null ? 0 : a.getOrderNum();
      int ob = b.getOrderNum() == null ? 0 : b.getOrderNum();
      return Integer.compare(oa, ob);
    });
    return list;
  }

  private List<MenuTreeSelectVo> buildSelectTree(List<SysMenu> all, Long parentId) {
    List<MenuTreeSelectVo> list = new ArrayList<>();
    for (SysMenu m : all) {
      Long pid = m.getParentId() == null ? 0L : m.getParentId();
      if (!Objects.equals(pid, parentId)) {
        continue;
      }
      MenuTreeSelectVo node = new MenuTreeSelectVo();
      node.setId(m.getMenuId());
      node.setLabel(m.getMenuName());
      node.setChildren(buildSelectTree(all, m.getMenuId()));
      list.add(node);
    }
    return list;
  }
}
