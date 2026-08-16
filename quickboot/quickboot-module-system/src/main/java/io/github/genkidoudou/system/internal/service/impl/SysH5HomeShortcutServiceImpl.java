package io.github.genkidoudou.system.internal.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.github.genkidoudou.common.exception.ErrorCodes;
import io.github.genkidoudou.common.exception.WarningException;
import io.github.genkidoudou.system.internal.entity.SysUserH5HomeShortcut;
import io.github.genkidoudou.system.internal.mapper.SysUserH5HomeShortcutMapper;
import io.github.genkidoudou.system.internal.service.ISysH5HomeShortcutService;
import io.github.genkidoudou.system.internal.service.ISysPermissionService;
import io.github.genkidoudou.system.internal.vo.H5WorkbenchItemVo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * H5 首页快捷偏好：默认解析、候选交集与全量保存。
 */
@Service
@RequiredArgsConstructor
public class SysH5HomeShortcutServiceImpl implements ISysH5HomeShortcutService {

  /**
   * 系统默认快捷（有序）：用户 / 部门 / 角色 / 在线用户。
   * <p>对应 Flyway V29/V31 的 H5 C 菜单 id；无权限项在解析时自动跳过。</p>
   */
  private static final List<Long> DEFAULT_MENU_IDS = List.of(9002L, 9003L, 9004L, 9015L);

  private final SysUserH5HomeShortcutMapper shortcutMapper;
  private final ISysPermissionService permissionService;

  @Override
  public List<H5WorkbenchItemVo> listFinalShortcuts(String userId) {
    if (StrUtil.isBlank(userId)) {
      return List.of();
    }
    Map<String, H5WorkbenchItemVo> byId = candidatesById(userId);
    List<SysUserH5HomeShortcut> prefs = listPrefs(userId);
    List<Long> orderedIds;
    if (CollUtil.isEmpty(prefs)) {
      orderedIds = DEFAULT_MENU_IDS;
    }
    else {
      orderedIds = prefs.stream()
        .sorted(Comparator.comparingInt(p -> p.getOrderNum() == null ? 0 : p.getOrderNum()))
        .map(SysUserH5HomeShortcut::getMenuId)
        .filter(Objects::nonNull)
        .toList();
    }
    List<H5WorkbenchItemVo> out = new ArrayList<>();
    int order = 1;
    for (Long menuId : orderedIds) {
      if (out.size() >= MAX_SHORTCUTS) {
        break;
      }
      H5WorkbenchItemVo hit = byId.get(String.valueOf(menuId));
      if (hit == null) {
        continue;
      }
      H5WorkbenchItemVo copy = copyItem(hit);
      copy.setOrderNum(order++);
      out.add(copy);
    }
    return out;
  }

  @Override
  public List<H5WorkbenchItemVo> listCandidates(String userId) {
    if (StrUtil.isBlank(userId)) {
      return List.of();
    }
    return permissionService.listH5PageItems(userId);
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public void saveShortcuts(String userId, List<String> menuIds) {
    if (StrUtil.isBlank(userId)) {
      throw new WarningException(ErrorCodes.Menu.INVALID_PARAM, "userId");
    }
    List<String> ids = menuIds == null ? List.of() : menuIds.stream()
      .filter(StrUtil::isNotBlank)
      .map(String::trim)
      .toList();
    // 空数组：清除偏好 → 恢复默认
    if (ids.isEmpty()) {
      shortcutMapper.delete(new LambdaQueryWrapper<SysUserH5HomeShortcut>()
        .eq(SysUserH5HomeShortcut::getUserId, userId));
      return;
    }
    if (ids.size() > MAX_SHORTCUTS) {
      throw new WarningException(ErrorCodes.Menu.INVALID_PARAM, "menuIds最多" + MAX_SHORTCUTS + "个");
    }
    Set<String> allowed = candidatesById(userId).keySet();
    for (String id : ids) {
      if (!allowed.contains(id)) {
        throw new WarningException(ErrorCodes.Menu.INVALID_PARAM, "无权菜单:" + id);
      }
    }
    // 全量覆盖：先删后插
    shortcutMapper.delete(new LambdaQueryWrapper<SysUserH5HomeShortcut>()
      .eq(SysUserH5HomeShortcut::getUserId, userId));
    int order = 1;
    for (String id : ids) {
      SysUserH5HomeShortcut row = new SysUserH5HomeShortcut();
      row.setUserId(userId);
      row.setMenuId(Long.valueOf(id));
      row.setOrderNum(order++);
      shortcutMapper.insert(row);
    }
  }

  private List<SysUserH5HomeShortcut> listPrefs(String userId) {
    return shortcutMapper.selectList(new LambdaQueryWrapper<SysUserH5HomeShortcut>()
      .eq(SysUserH5HomeShortcut::getUserId, userId)
      .orderByAsc(SysUserH5HomeShortcut::getOrderNum));
  }

  private Map<String, H5WorkbenchItemVo> candidatesById(String userId) {
    return permissionService.listH5PageItems(userId).stream()
      .filter(i -> StrUtil.isNotBlank(i.getId()))
      .collect(Collectors.toMap(H5WorkbenchItemVo::getId, i -> i, (a, b) -> a, LinkedHashMap::new));
  }

  private static H5WorkbenchItemVo copyItem(H5WorkbenchItemVo src) {
    H5WorkbenchItemVo item = new H5WorkbenchItemVo();
    item.setId(src.getId());
    item.setLabel(src.getLabel());
    item.setPath(src.getPath());
    item.setIcon(src.getIcon());
    item.setOrderNum(src.getOrderNum());
    return item;
  }
}
