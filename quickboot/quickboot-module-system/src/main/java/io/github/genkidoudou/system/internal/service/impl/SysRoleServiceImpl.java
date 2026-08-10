package io.github.genkidoudou.system.internal.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.github.genkidoudou.common.api.PageInfo;
import io.github.genkidoudou.common.api.PageRequest;
import io.github.genkidoudou.common.excel.ExcelUtils;
import io.github.genkidoudou.common.excel.exception.ExcelDataCheckException;
import io.github.genkidoudou.common.excel.listener.ExcelResult;
import io.github.genkidoudou.common.exception.ErrorCodes;
import io.github.genkidoudou.common.exception.WarningException;
import io.github.genkidoudou.common.mybatisplus.BaseServiceImpl;
import io.github.genkidoudou.system.internal.entity.SysMenu;
import io.github.genkidoudou.system.internal.entity.SysRole;
import io.github.genkidoudou.system.internal.entity.SysRoleMenu;
import io.github.genkidoudou.system.internal.entity.SysUser;
import io.github.genkidoudou.system.internal.entity.SysUserRole;
import io.github.genkidoudou.system.internal.mapper.SysRoleMapper;
import io.github.genkidoudou.system.internal.mapper.SysRoleMenuMapper;
import io.github.genkidoudou.system.internal.mapper.SysUserMapper;
import io.github.genkidoudou.system.internal.mapper.SysUserRoleMapper;
import io.github.genkidoudou.system.internal.security.SaPermissionCache;
import io.github.genkidoudou.system.internal.service.ISysPermissionService;
import io.github.genkidoudou.system.internal.service.ISysRoleService;
import io.github.genkidoudou.system.internal.vo.RoleMenuTreeVo;
import io.github.genkidoudou.system.internal.vo.SysMenuTreeVo;
import io.github.genkidoudou.system.internal.vo.SysRoleImportRow;
import io.github.genkidoudou.system.internal.vo.SysRoleUserVo;
import io.github.genkidoudou.system.internal.vo.SysRoleVo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 角色管理实现。
 */
@Service
@RequiredArgsConstructor
public class SysRoleServiceImpl extends BaseServiceImpl<SysRoleMapper, SysRole> implements ISysRoleService {

  private static final long SUPER_ROLE_ID = 1L;

  public static final int IMPORT_MAX_ROWS = 5000;

  private final SysRoleMenuMapper roleMenuMapper;
  private final SysUserRoleMapper userRoleMapper;
  private final SysUserMapper userMapper;
  private final ISysPermissionService permissionService;

  @Override
  public PageInfo<SysRoleVo> page(PageRequest<SysRoleVo> pageRequest) {
    SysRoleVo param = pageRequest != null ? pageRequest.getParam() : null;
    return this.page(pageRequest, q -> {
      if (param == null) {
        return;
      }
      if (StrUtil.isNotBlank(param.getRoleName())) {
        q.like(SysRole::getRoleName, param.getRoleName().trim());
      }
      if (StrUtil.isNotBlank(param.getRoleKey())) {
        q.like(SysRole::getRoleKey, param.getRoleKey().trim());
      }
      if (StrUtil.isNotBlank(param.getStatus())) {
        q.eq(SysRole::getStatus, param.getStatus().trim());
      }
    }, SysRoleVo.class);
  }

  @Override
  public SysRoleVo getDetail(Long roleId) {
    if (roleId == null) {
      throw new WarningException(ErrorCodes.Role.INVALID_PARAM, "roleId");
    }
    requireRole(roleId);
    return getVoById(roleId, SysRoleVo.class);
  }

  @Override
  public Long add(SysRoleVo vo) {
    String roleKey = vo.getRoleKey().trim();
    assertRoleKeyUnique(roleKey, null);
    SysRole entity = toEntity(vo);
    entity.setRoleId(null);
    entity.setRoleName(vo.getRoleName().trim());
    entity.setRoleKey(roleKey);
    entity.setRoleSort(vo.getRoleSort() == null ? 0 : vo.getRoleSort());
    entity.setDataScope(StrUtil.blankToDefault(vo.getDataScope(), "1"));
    entity.setStatus(StrUtil.blankToDefault(vo.getStatus(), "0"));
    boolean save = this.save(entity);
    if (!save) {
      return null;
    }
    return entity.getRoleId();
  }

  @Override
  public boolean update(SysRoleVo vo) {
    SysRole existing = requireRole(vo.getRoleId());
    String roleKey = vo.getRoleKey().trim();
    assertRoleKeyUnique(roleKey, vo.getRoleId());
    SysRole entity = toEntity(vo);
    entity.setRoleId(existing.getRoleId());
    entity.setRoleName(vo.getRoleName().trim());
    entity.setRoleKey(roleKey);
    if (entity.getRoleSort() == null) {
      entity.setRoleSort(existing.getRoleSort());
    }
    if (StrUtil.isBlank(entity.getDataScope())) {
      entity.setDataScope(existing.getDataScope());
    }
    if (StrUtil.isBlank(entity.getStatus())) {
      entity.setStatus(existing.getStatus());
    }
    return super.updateById(entity);
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public void remove(Collection<Long> roleIds) {
    if (CollectionUtil.isEmpty(roleIds)) {
      return;
    }
    List<Long> idList = roleIds.stream()
      .filter(Objects::nonNull)
      .distinct()
      .collect(Collectors.toList());
    if (idList.isEmpty()) {
      return;
    }
    for (Long roleId : idList) {
      if (Objects.equals(roleId, SUPER_ROLE_ID)) {
        throw new WarningException(ErrorCodes.Role.SUPER_ROLE_FORBIDDEN);
      }
      long bindCount = userRoleMapper.selectCount(new LambdaQueryWrapper<SysUserRole>()
        .eq(SysUserRole::getRoleId, roleId));
      if (bindCount > 0) {
        throw new WarningException(ErrorCodes.Role.HAS_USERS, roleId);
      }
      roleMenuMapper.delete(new LambdaQueryWrapper<SysRoleMenu>().eq(SysRoleMenu::getRoleId, roleId));
      this.removeById(roleId);
    }
  }

  @Override
  public void changeStatus(Long roleId, String status) {
    if (!"0".equals(status) && !"1".equals(status)) {
      throw new WarningException(ErrorCodes.Role.STATUS_INVALID);
    }
    SysRole role = requireRole(roleId);
    role.setStatus(status);
    this.updateById(role);
    clearPermissionCacheByRoleId(roleId);
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public void saveMenus(Long roleId, List<Long> menuIds) {
    requireRole(roleId);
    roleMenuMapper.delete(new LambdaQueryWrapper<SysRoleMenu>().eq(SysRoleMenu::getRoleId, roleId));
    if (CollUtil.isNotEmpty(menuIds)) {
      Set<Long> uniq = new HashSet<>();
      for (Long menuId : menuIds) {
        if (menuId != null) {
          uniq.add(menuId);
        }
      }
      // 勾选叶子时补齐祖先目录，避免中间 M 未写入导致侧栏断枝
      uniq.addAll(expandWithAncestorMenuIds(uniq));
      for (Long menuId : uniq) {
        SysRoleMenu rm = new SysRoleMenu();
        rm.setRoleId(roleId);
        rm.setMenuId(menuId);
        roleMenuMapper.insert(rm);
      }
    }
    clearPermissionCacheByRoleId(roleId);
  }

  private Set<Long> expandWithAncestorMenuIds(Set<Long> menuIds) {
    if (CollUtil.isEmpty(menuIds)) {
      return Set.of();
    }
    List<SysMenu> all = permissionService.listAllEnabledMenus();
    Map<Long, SysMenu> byId = all.stream()
      .collect(Collectors.toMap(SysMenu::getMenuId, m -> m, (a, b) -> a));
    Set<Long> extra = new HashSet<>();
    for (Long menuId : menuIds) {
      SysMenu cur = byId.get(menuId);
      if (cur == null) {
        continue;
      }
      Long pid = cur.getParentId();
      while (pid != null && pid != 0L && pid != -1L) {
        if (!extra.add(pid)) {
          break;
        }
        SysMenu parent = byId.get(pid);
        if (parent == null) {
          break;
        }
        pid = parent.getParentId();
      }
    }
    return extra;
  }

  @Override
  public RoleMenuTreeVo menuTree(Long roleId) {
    requireRole(roleId);
    List<SysMenu> all = permissionService.listAllEnabledMenus();
    RoleMenuTreeVo vo = new RoleMenuTreeVo();
    vo.setMenus(buildMenuTree(all, 0L));
    vo.setCheckedKeys(permissionService.listMenuIdsByRoleId(roleId));
    return vo;
  }

  @Override
  public PageInfo<SysRoleUserVo> allocatedPage(PageRequest<SysRoleUserVo> pageRequest, Long roleId) {
    requireRole(roleId);
    List<String> userIds = userRoleMapper.selectList(new LambdaQueryWrapper<SysUserRole>()
        .eq(SysUserRole::getRoleId, roleId))
      .stream()
      .map(SysUserRole::getUserId)
      .toList();
    return pageUsersByIds(pageRequest, userIds, true);
  }

  @Override
  public PageInfo<SysRoleUserVo> unallocatedPage(PageRequest<SysRoleUserVo> pageRequest, Long roleId) {
    requireRole(roleId);
    List<String> allocated = userRoleMapper.selectList(new LambdaQueryWrapper<SysUserRole>()
        .eq(SysUserRole::getRoleId, roleId))
      .stream()
      .map(SysUserRole::getUserId)
      .toList();
    return pageUsersByIds(pageRequest, allocated, false);
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public void grantUsers(Long roleId, List<String> userIds) {
    requireRole(roleId);
    if (CollUtil.isEmpty(userIds)) {
      return;
    }
    Set<String> existing = userRoleMapper.selectList(new LambdaQueryWrapper<SysUserRole>()
        .eq(SysUserRole::getRoleId, roleId))
      .stream()
      .map(SysUserRole::getUserId)
      .collect(Collectors.toSet());
    List<String> granted = new ArrayList<>();
    for (String uid : userIds) {
      if (StrUtil.isBlank(uid) || existing.contains(uid.trim())) {
        continue;
      }
      String trimmed = uid.trim();
      SysUserRole ur = new SysUserRole();
      ur.setRoleId(roleId);
      ur.setUserId(trimmed);
      userRoleMapper.insert(ur);
      granted.add(trimmed);
    }
    SaPermissionCache.clearByUserIds(granted);
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public void cancelUsers(Long roleId, List<String> userIds) {
    requireRole(roleId);
    if (CollUtil.isEmpty(userIds)) {
      return;
    }
    List<String> trimmedIds = userIds.stream().filter(StrUtil::isNotBlank).map(String::trim).toList();
    if (trimmedIds.isEmpty()) {
      return;
    }
    userRoleMapper.delete(new LambdaQueryWrapper<SysUserRole>()
      .eq(SysUserRole::getRoleId, roleId)
      .in(SysUserRole::getUserId, trimmedIds));
    SaPermissionCache.clearByUserIds(trimmedIds);
  }

  /**
   * 角色权限相关数据变更后，清理该角色下已登录用户的 Session 权限缓存。
   *
   * @param roleId 角色主键
   */
  private void clearPermissionCacheByRoleId(Long roleId) {
    if (roleId == null) {
      return;
    }
    List<String> userIds = userRoleMapper.selectList(new LambdaQueryWrapper<SysUserRole>()
        .eq(SysUserRole::getRoleId, roleId))
      .stream()
      .map(SysUserRole::getUserId)
      .toList();
    SaPermissionCache.clearByUserIds(userIds);
  }


  private PageInfo<SysRoleUserVo> pageUsersByIds(PageRequest<SysRoleUserVo> pageRequest, List<String> boundIds,
                                                 boolean allocated) {
    long current = pageRequest != null ? pageRequest.getCurrent() : 1L;
    long size = pageRequest != null ? pageRequest.getSize() : 10L;
    SysRoleUserVo param = pageRequest != null ? pageRequest.getParam() : null;

    LambdaQueryWrapper<SysUser> q = new LambdaQueryWrapper<>();
    List<Long> longIds = CollUtil.isEmpty(boundIds)
      ? List.of()
      : boundIds.stream().map(this::parseUserIdLong).filter(Objects::nonNull).toList();
    if (allocated) {
      if (longIds.isEmpty()) {
        return emptyPage(current, size);
      }
      q.in(SysUser::getUserId, longIds);
    } else if (!longIds.isEmpty()) {
      q.notIn(SysUser::getUserId, longIds);
    }
    if (param != null) {
      if (StrUtil.isNotBlank(param.getUserName())) {
        q.like(SysUser::getUserName, param.getUserName().trim());
      }
      if (StrUtil.isNotBlank(param.getNickName())) {
        q.like(SysUser::getNickName, param.getNickName().trim());
      }
      if (StrUtil.isNotBlank(param.getStatus())) {
        q.eq(SysUser::getStatus, param.getStatus().trim());
      }
    }
    Page<SysUser> page = userMapper.selectPage(new Page<>(current, size), q);
    List<SysRoleUserVo> rows = page.getRecords().stream().map(u -> {
      SysRoleUserVo vo = new SysRoleUserVo();
      vo.setUserId(u.getUserId() == null ? null : String.valueOf(u.getUserId()));
      vo.setUserName(u.getUserName());
      vo.setNickName(u.getNickName());
      vo.setStatus(u.getStatus());
      return vo;
    }).toList();
    PageInfo<SysRoleUserVo> info = new PageInfo<>();
    info.setCurrent(page.getCurrent());
    info.setSize(page.getSize());
    info.setTotal(page.getTotal());
    info.setRecords(rows);
    return info;
  }

  private PageInfo<SysRoleUserVo> emptyPage(long current, long size) {
    PageInfo<SysRoleUserVo> info = new PageInfo<>();
    info.setCurrent(current);
    info.setSize(size);
    info.setTotal(0);
    info.setRecords(List.of());
    return info;
  }

  private Long parseUserIdLong(String id) {
    try {
      return Long.parseLong(id.trim());
    } catch (Exception e) {
      return null;
    }
  }

  private List<SysMenuTreeVo> buildMenuTree(List<SysMenu> all, Long parentId) {
    List<SysMenuTreeVo> list = new ArrayList<>();
    long expected = normalizeTreeParentId(parentId);
    for (SysMenu m : all) {
      long pid = normalizeTreeParentId(m.getParentId());
      if (pid != expected) {
        continue;
      }
      SysMenuTreeVo node = new SysMenuTreeVo();
      node.setMenuId(m.getMenuId());
      node.setParentId(m.getParentId() == null ? 0L : m.getParentId());
      node.setMenuName(m.getMenuName());
      node.setMenuType(m.getMenuType());
      node.setChildren(buildMenuTree(all, m.getMenuId()));
      list.add(node);
    }
    return list;
  }

  private static long normalizeTreeParentId(Long parentId) {
    if (parentId == null || parentId == 0L || parentId == -1L) {
      return 0L;
    }
    return parentId;
  }

  private SysRole requireRole(Long roleId) {
    if (roleId == null) {
      throw new WarningException(ErrorCodes.Role.INVALID_PARAM, "roleId");
    }
    SysRole role = this.getById(roleId);
    if (role == null) {
      throw new WarningException(ErrorCodes.Role.NOT_FOUND, roleId);
    }
    return role;
  }

  private void assertRoleKeyUnique(String roleKey, Long excludeRoleId) {
    LambdaQueryWrapper<SysRole> q = new LambdaQueryWrapper<SysRole>().eq(SysRole::getRoleKey, roleKey);
    if (excludeRoleId != null) {
      q.ne(SysRole::getRoleId, excludeRoleId);
    }
    if (this.count(q) > 0) {
      throw new WarningException(ErrorCodes.Role.ROLE_KEY_EXISTS, roleKey);
    }
  }

  @Override
  public List<SysRoleVo> export(SysRoleVo query) {
    List<SysRole> entities = listForExport(query == null ? new SysRoleVo() : query);
    if (entities.size() > IMPORT_MAX_ROWS) {
      throw WarningException.literal(ErrorCodes.Common.INVALID_PARAM, "导出条数超过上限 " + IMPORT_MAX_ROWS);
    }
    return entities.stream()
      .map(r -> toVo(r, SysRoleVo.class))
      .collect(Collectors.toList());
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public ExcelResult<SysRoleImportRow> importExcel(MultipartFile file, boolean updateSupport) throws IOException {
    if (file == null || file.isEmpty()) {
      throw WarningException.literal(ErrorCodes.Common.INVALID_PARAM, "请上传 Excel 文件");
    }
    List<SysRole> lists = new ArrayList<>();

    ExcelResult<SysRoleImportRow> excelResult = ExcelUtils.importExcel(
      file.getInputStream(),
      SysRoleImportRow.class,
      (row, ctx) -> {
        String roleKey = row.getRoleKey().trim();
        SysRole existing = this.getOne(new LambdaQueryWrapper<SysRole>()
          .eq(SysRole::getRoleKey, roleKey)
          .last("LIMIT 1"));
        SysRole entity = BeanUtil.copyProperties(row, SysRole.class);
        entity.setRoleName(row.getRoleName().trim());
        entity.setRoleKey(roleKey);
        entity.setRoleSort(row.getRoleSort() == null ? 0 : row.getRoleSort());
        entity.setDataScope(StrUtil.blankToDefault(row.getDataScope(), "1"));
        entity.setStatus(StrUtil.blankToDefault(row.getStatus(), "0"));
        if (existing == null) {
          entity.setRoleId(null);
          lists.add(entity);
        } else if (updateSupport) {
          entity.setRoleId(existing.getRoleId());
          lists.add(entity);
        } else {
          throw new ExcelDataCheckException("已存在相同权限字符");
        }
      },
      (batch, ctx) -> {
        // 行级已入队
      });

    if (CollectionUtil.isNotEmpty(lists)) {
      this.saveOrUpdateBatch(lists);
    }
    excelResult.writeErrorFile();
    return excelResult;
  }

  private List<SysRole> listForExport(SysRoleVo query) {
    List<Long> ids = query.getIds() == null ? Collections.emptyList() : query.getIds().stream()
      .filter(Objects::nonNull)
      .distinct()
      .collect(Collectors.toList());
    if (!ids.isEmpty()) {
      return this.listByIds(ids);
    }
    LambdaQueryWrapper<SysRole> q = new LambdaQueryWrapper<>();
    if (StrUtil.isNotBlank(query.getRoleName())) {
      q.like(SysRole::getRoleName, query.getRoleName().trim());
    }
    if (StrUtil.isNotBlank(query.getRoleKey())) {
      q.like(SysRole::getRoleKey, query.getRoleKey().trim());
    }
    if (StrUtil.isNotBlank(query.getStatus())) {
      q.eq(SysRole::getStatus, query.getStatus().trim());
    }
    q.orderByAsc(SysRole::getRoleSort).orderByDesc(SysRole::getRoleId);
    return this.list(q);
  }
}
