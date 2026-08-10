package io.github.genkidoudou.system.internal.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import io.github.genkidoudou.common.api.PageInfo;
import io.github.genkidoudou.common.api.PageRequest;
import io.github.genkidoudou.common.crypto.PasswordCodec;
import io.github.genkidoudou.common.excel.ExcelUtils;
import io.github.genkidoudou.common.excel.exception.ExcelDataCheckException;
import io.github.genkidoudou.common.excel.listener.ExcelResult;
import io.github.genkidoudou.common.exception.ErrorCodes;
import io.github.genkidoudou.common.exception.WarningException;
import io.github.genkidoudou.common.mybatisplus.BaseServiceImpl;
import io.github.genkidoudou.system.internal.entity.SysDept;
import io.github.genkidoudou.system.internal.entity.SysRole;
import io.github.genkidoudou.system.internal.entity.SysUser;
import io.github.genkidoudou.system.internal.entity.SysUserRole;
import io.github.genkidoudou.system.internal.mapper.SysDeptMapper;
import io.github.genkidoudou.system.internal.mapper.SysRoleMapper;
import io.github.genkidoudou.system.internal.mapper.SysUserMapper;
import io.github.genkidoudou.system.internal.mapper.SysUserRoleMapper;
import io.github.genkidoudou.system.internal.security.SaPermissionCache;
import io.github.genkidoudou.system.internal.service.ISysUserService;
import io.github.genkidoudou.system.internal.support.DeptIdExpand;
import io.github.genkidoudou.system.internal.vo.SysRoleVo;
import io.github.genkidoudou.system.internal.vo.SysUserAuthRoleVo;
import io.github.genkidoudou.system.internal.vo.SysUserImportRow;
import io.github.genkidoudou.system.internal.vo.SysUserVo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SysUserServiceImpl extends BaseServiceImpl<SysUserMapper, SysUser> implements ISysUserService {

  public static final String DEFAULT_PASSWORD = "admin123";
  public static final int IMPORT_MAX_ROWS = 5000;
  private static final long SUPER_USER_ID = 1L;

  private final PasswordCodec passwordCodec;
  private final SysUserRoleMapper userRoleMapper;
  private final SysRoleMapper roleMapper;
  private final SysDeptMapper deptMapper;

  @Override
  public SysUser findByUserName(String username) {
    if (StrUtil.isBlank(username)) {
      return null;
    }
    return this.getOne(new LambdaQueryWrapper<SysUser>().eq(SysUser::getUserName, username), false);
  }

  @Override
  public SysUser findByUserId(Long userId) {
    if (userId == null) {
      return null;
    }
    return this.getById(userId);
  }

  @Override
  public PageInfo<SysUserVo> page(PageRequest<SysUserVo> pageRequest) {
    SysUserVo param = pageRequest != null ? pageRequest.getParam() : null;
    return this.page(pageRequest, q -> {
      if (param == null) {
        return;
      }
      if (StrUtil.isNotBlank(param.getUserName())) {
        q.like(SysUser::getUserName, param.getUserName().trim());
      }
      if (StrUtil.isNotBlank(param.getNickName())) {
        q.like(SysUser::getNickName, param.getNickName().trim());
      }
      if (StrUtil.isNotBlank(param.getPhonenumber())) {
        q.like(SysUser::getPhonenumber, param.getPhonenumber().trim());
      }
      if (StrUtil.isNotBlank(param.getStatus())) {
        q.eq(SysUser::getStatus, param.getStatus().trim());
      }
      applyDeptIdFilter(q, param.getDeptId());
    }, SysUserVo.class, (entities, vos) -> {
      for (SysUserVo vo : vos) {
        vo.setPassword(null);
        fillDeptAndRoles(vo);
      }
      return vos;
    });
  }

  @Override
  public SysUserVo getDetail(Long userId) {
    SysUser user = requireUser(userId);
    SysUserVo vo = toVo(user, SysUserVo.class);
    vo.setPassword(null);
    fillDeptAndRoles(vo);
    return vo;
  }

  @Transactional(rollbackFor = Exception.class)
  @Override
  public Long add(SysUserVo vo) {
    String userName = vo.getUserName().trim();
    if (this.count(new LambdaQueryWrapper<SysUser>().eq(SysUser::getUserName, userName)) > 0) {
      throw WarningException.literal(ErrorCodes.Common.INVALID_PARAM, "用户账号已存在");
    }
    SysUser entity = toEntity(vo);
    entity.setUserId(null);
    entity.setUserName(userName);
    entity.setNickName(StrUtil.blankToDefault(vo.getNickName(), userName));
    entity.setDeptId(vo.getDeptId());
    entity.setEmail(vo.getEmail());
    entity.setPhonenumber(vo.getPhonenumber());
    entity.setSex(vo.getSex());
    entity.setStatus(StrUtil.blankToDefault(vo.getStatus(), "0"));
    String raw = StrUtil.blankToDefault(vo.getPassword(), DEFAULT_PASSWORD);
    entity.setPassword(passwordCodec.encrypt(raw));
    this.save(entity);
    saveRoles(entity.getUserId(), vo.getRoleIds());
    return entity.getUserId();
  }

  @Transactional(rollbackFor = Exception.class)
  @Override
  public boolean update(SysUserVo vo) {
    SysUser existing = requireUser(vo.getUserId());
    SysUser entity = toEntity(vo);
    entity.setUserId(existing.getUserId());
    entity.setUserName(existing.getUserName());
    entity.setNickName(StrUtil.blankToDefault(vo.getNickName(), existing.getNickName()));
    entity.setEmail(vo.getEmail());
    entity.setPhonenumber(vo.getPhonenumber());
    entity.setSex(vo.getSex());
    entity.setStatus(StrUtil.blankToDefault(vo.getStatus(), existing.getStatus()));
    if (StrUtil.isNotBlank(vo.getPassword())) {
      entity.setPassword(passwordCodec.encrypt(vo.getPassword()));
    } else {
      entity.setPassword(existing.getPassword());
    }
    // deptId 允许清空：全局 update-strategy=not_null 会跳过 null，需显式 set
    entity.setDeptId(null);
    boolean ok = this.updateById(entity);
    ok = this.update(new LambdaUpdateWrapper<SysUser>()
      .eq(SysUser::getUserId, existing.getUserId())
      .set(SysUser::getDeptId, vo.getDeptId())) && ok;
    if (vo.getRoleIds() != null) {
      saveRoles(existing.getUserId(), vo.getRoleIds());
    }
    return ok;
  }

  @Transactional(rollbackFor = Exception.class)
  @Override
  public void remove(Collection<Long> userIds) {
    if (CollectionUtil.isEmpty(userIds)) {
      return;
    }
    for (Long id : userIds) {
      if (id == null) {
        continue;
      }
      SysUser user = requireUser(id);
      assertNotSuperDestroy(user);
      userRoleMapper.delete(new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, String.valueOf(id)));
      this.removeById(id);
      SaPermissionCache.clearByUserIds(List.of(String.valueOf(id)));
    }
  }

  @Override
  public void changeStatus(Long userId, String status) {
    if (!"0".equals(status) && !"1".equals(status)) {
      throw WarningException.literal(ErrorCodes.Common.INVALID_PARAM, "状态无效");
    }
    SysUser user = requireUser(userId);
    if (isSuper(user) && "1".equals(status)) {
      throw WarningException.literal(ErrorCodes.Common.INVALID_PARAM, "不允许停用超级管理员");
    }
    user.setStatus(status);
    this.updateById(user);
    SaPermissionCache.clearByUserIds(List.of(String.valueOf(userId)));
  }

  @Override
  public void resetPwd(Long userId, String password) {
    if (StrUtil.isBlank(password)) {
      throw WarningException.literal(ErrorCodes.Common.INVALID_PARAM, "密码不能为空");
    }
    SysUser user = requireUser(userId);
    user.setPassword(passwordCodec.encrypt(password.trim()));
    this.updateById(user);
  }

  @Override
  public SysUserAuthRoleVo authRole(Long userId) {
    SysUser user = requireUser(userId);
    SysUserAuthRoleVo vo = new SysUserAuthRoleVo();
    vo.setUserId(user.getUserId());
    vo.setUserName(user.getUserName());
    vo.setNickName(user.getNickName());
    List<Long> roleIds = userRoleMapper.selectList(new LambdaQueryWrapper<SysUserRole>()
        .eq(SysUserRole::getUserId, String.valueOf(userId)))
      .stream().map(SysUserRole::getRoleId).toList();
    vo.setRoleIds(roleIds);
    List<SysRole> roles = roleMapper.selectList(new LambdaQueryWrapper<SysRole>().orderByAsc(SysRole::getRoleSort));
    vo.setRoles(roles.stream().map(r -> {
      SysRoleVo rv = new SysRoleVo();
      rv.setRoleId(r.getRoleId());
      rv.setRoleName(r.getRoleName());
      rv.setRoleKey(r.getRoleKey());
      rv.setStatus(r.getStatus());
      return rv;
    }).collect(Collectors.toList()));
    return vo;
  }

  @Transactional(rollbackFor = Exception.class)
  @Override
  public void saveAuthRole(Long userId, List<Long> roleIds) {
    requireUser(userId);
    saveRoles(userId, roleIds);
  }

  @Override
  public List<SysUserVo> export(SysUserVo query) {
    List<SysUser> list = listForExport(query == null ? new SysUserVo() : query);
    if (list.size() > IMPORT_MAX_ROWS) {
      throw WarningException.literal(ErrorCodes.Common.INVALID_PARAM, "导出条数超过上限 " + IMPORT_MAX_ROWS);
    }
    return list.stream().map(u -> {
      SysUserVo vo = toVo(u, SysUserVo.class);
      vo.setPassword(null);
      fillDeptAndRoles(vo);
      return vo;
    }).collect(Collectors.toList());
  }

  @Transactional(rollbackFor = Exception.class)
  @Override
  public ExcelResult<SysUserImportRow> importExcel(MultipartFile file, boolean updateSupport) throws IOException {
    if (file == null || file.isEmpty()) {
      throw WarningException.literal(ErrorCodes.Common.INVALID_PARAM, "请上传 Excel 文件");
    }
    List<SysUser> batch = new ArrayList<>();
    ExcelResult<SysUserImportRow> result = ExcelUtils.importExcel(
      file.getInputStream(),
      SysUserImportRow.class,
      (row, ctx) -> {
        if (row == null || StrUtil.isBlank(row.getUserName())) {
          throw new ExcelDataCheckException("用户账号不能为空");
        }
        String userName = row.getUserName().trim();
        SysUser existing = findByUserName(userName);
        SysUser entity = new SysUser();
        entity.setUserName(userName);
        entity.setNickName(StrUtil.blankToDefault(row.getNickName(), userName));
        entity.setDeptId(resolveDeptIdByName(row.getDeptName()));
        entity.setEmail(row.getEmail());
        entity.setPhonenumber(row.getPhonenumber());
        entity.setSex(row.getSex());
        entity.setStatus(StrUtil.blankToDefault(row.getStatus(), "0"));
        entity.setRemark(row.getRemark());
        if (existing == null) {
          entity.setPassword(passwordCodec.encrypt(DEFAULT_PASSWORD));
          batch.add(entity);
        } else if (updateSupport) {
          entity.setUserId(existing.getUserId());
          entity.setPassword(existing.getPassword());
          batch.add(entity);
        } else {
          throw new ExcelDataCheckException("用户账号已存在");
        }
      },
      (list, ctx) -> {
      });
    if (result.getTotal() != null && result.getTotal() > IMPORT_MAX_ROWS) {
      throw WarningException.literal(ErrorCodes.Common.INVALID_PARAM, "导入条数超过上限 " + IMPORT_MAX_ROWS);
    }
    if (CollectionUtil.isNotEmpty(batch)) {
      this.saveOrUpdateBatch(batch);
    }
    result.writeErrorFile();
    return result;
  }

  private Long resolveDeptIdByName(String deptName) {
    if (StrUtil.isBlank(deptName)) {
      return null;
    }
    String name = deptName.trim();
    List<SysDept> matched = deptMapper.selectList(new LambdaQueryWrapper<SysDept>()
      .eq(SysDept::getDeptName, name));
    if (matched.isEmpty()) {
      throw new ExcelDataCheckException("部门不存在: " + name);
    }
    if (matched.size() > 1) {
      throw new ExcelDataCheckException("部门名称不唯一: " + name);
    }
    return matched.get(0).getDeptId();
  }

  private void saveRoles(Long userId, List<Long> roleIds) {
    userRoleMapper.delete(new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, String.valueOf(userId)));
    if (CollUtil.isNotEmpty(roleIds)) {
      for (Long roleId : roleIds.stream().filter(Objects::nonNull).distinct().toList()) {
        SysUserRole ur = new SysUserRole();
        ur.setUserId(String.valueOf(userId));
        ur.setRoleId(roleId);
        userRoleMapper.insert(ur);
      }
    }
    SaPermissionCache.clearByUserIds(List.of(String.valueOf(userId)));
  }

  private void fillDeptAndRoles(SysUserVo vo) {
    if (vo.getDeptId() != null) {
      SysDept dept = deptMapper.selectById(vo.getDeptId());
      if (dept != null) {
        vo.setDeptName(dept.getDeptName());
      }
    }
    if (vo.getUserId() == null) {
      return;
    }
    List<Long> roleIds = userRoleMapper.selectList(new LambdaQueryWrapper<SysUserRole>()
        .eq(SysUserRole::getUserId, String.valueOf(vo.getUserId())))
      .stream().map(SysUserRole::getRoleId).toList();
    vo.setRoleIds(roleIds);
    if (CollUtil.isNotEmpty(roleIds)) {
      String names = roleMapper.selectBatchIds(roleIds).stream()
        .map(SysRole::getRoleName).filter(StrUtil::isNotBlank).collect(Collectors.joining(","));
      vo.setRoleNames(names);
    }
  }

  private List<SysUser> listForExport(SysUserVo query) {
    List<Long> ids = query.getIds() == null ? Collections.emptyList() : query.getIds().stream()
      .filter(Objects::nonNull).distinct().collect(Collectors.toList());
    if (!ids.isEmpty()) {
      return this.listByIds(ids);
    }
    LambdaQueryWrapper<SysUser> q = new LambdaQueryWrapper<>();
    if (StrUtil.isNotBlank(query.getUserName())) {
      q.like(SysUser::getUserName, query.getUserName().trim());
    }
    if (StrUtil.isNotBlank(query.getNickName())) {
      q.like(SysUser::getNickName, query.getNickName().trim());
    }
    if (StrUtil.isNotBlank(query.getStatus())) {
      q.eq(SysUser::getStatus, query.getStatus().trim());
    }
    applyDeptIdFilter(q, query.getDeptId());
    return this.list(q);
  }

  private void applyDeptIdFilter(LambdaQueryWrapper<SysUser> q, Long deptId) {
    if (deptId == null) {
      return;
    }
    q.in(SysUser::getDeptId, resolveDeptIdsIncludingChildren(deptId));
  }

  private List<Long> resolveDeptIdsIncludingChildren(Long deptId) {
    List<SysDept> all = deptMapper.selectList(new LambdaQueryWrapper<SysDept>()
      .select(SysDept::getDeptId, SysDept::getParentId));
    return DeptIdExpand.includingChildren(deptId, all);
  }

  private SysUser requireUser(Long userId) {
    if (userId == null) {
      throw WarningException.literal(ErrorCodes.Common.INVALID_PARAM, "userId");
    }
    SysUser user = this.getById(userId);
    if (user == null) {
      throw WarningException.literal(ErrorCodes.Common.INVALID_PARAM, "用户不存在");
    }
    return user;
  }

  private boolean isSuper(SysUser user) {
    return Objects.equals(user.getUserId(), SUPER_USER_ID) || "admin".equalsIgnoreCase(user.getUserName());
  }

  private void assertNotSuperDestroy(SysUser user) {
    if (isSuper(user)) {
      throw WarningException.literal(ErrorCodes.Common.INVALID_PARAM, "不允许删除超级管理员");
    }
  }
}
