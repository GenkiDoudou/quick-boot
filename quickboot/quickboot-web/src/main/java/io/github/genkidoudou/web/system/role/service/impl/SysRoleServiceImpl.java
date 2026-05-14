package io.github.genkidoudou.web.system.role.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.github.genkidoudou.common.api.PageInfo;
import io.github.genkidoudou.common.excel.ExcelUtils;
import io.github.genkidoudou.common.exception.ErrorCodes;
import io.github.genkidoudou.common.exception.WarningException;
import io.github.genkidoudou.web.system.menu.domain.SysRole;
import io.github.genkidoudou.web.system.menu.domain.SysRoleMenu;
import io.github.genkidoudou.web.system.menu.domain.SysUserRole;
import io.github.genkidoudou.web.system.menu.mapper.SysRoleMapper;
import io.github.genkidoudou.web.system.menu.mapper.SysRoleMenuMapper;
import io.github.genkidoudou.web.system.menu.mapper.SysUserRoleMapper;
import io.github.genkidoudou.web.system.role.domain.SysRoleDept;
import io.github.genkidoudou.web.system.role.dto.RoleCancelUserRequest;
import io.github.genkidoudou.web.system.role.dto.RoleChangeStatusRequest;
import io.github.genkidoudou.web.system.role.dto.RoleDataScopeRequest;
import io.github.genkidoudou.web.system.role.dto.RoleGrantUsersRequest;
import io.github.genkidoudou.web.system.role.dto.RoleMenuRequest;
import io.github.genkidoudou.web.system.role.dto.SysRoleAuthUserQueryBo;
import io.github.genkidoudou.web.system.role.dto.SysRoleBo;
import io.github.genkidoudou.web.system.role.dto.SysRoleExcelRow;
import io.github.genkidoudou.web.system.role.dto.SysRoleQueryBo;
import io.github.genkidoudou.web.system.role.dto.SysRoleUserVo;
import io.github.genkidoudou.web.system.role.dto.SysRoleVo;
import io.github.genkidoudou.web.system.role.mapper.SysRoleDeptMapper;
import io.github.genkidoudou.web.system.role.service.SysRoleService;
import io.github.genkidoudou.web.system.user.domain.SysUser;
import io.github.genkidoudou.web.system.user.mapper.SysUserMapper;
import io.github.genkidoudou.web.system.user.service.SysUserRoleBindService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * 角色管理实现。
 */
@Service
public class SysRoleServiceImpl implements SysRoleService {

    /** 内置超级管理员角色主键（与 Flyway 种子一致）。 */
    public static final Long ADMIN_ROLE_ID = 1L;

    private static final Set<String> DATA_SCOPES = Set.of("1", "2", "3", "4", "5");

    private final SysRoleMapper roleMapper;
    private final SysRoleMenuMapper roleMenuMapper;
    private final SysRoleDeptMapper roleDeptMapper;
    private final SysUserRoleMapper userRoleMapper;
    private final SysUserMapper userMapper;
    private final SysUserRoleBindService userRoleBindService;

    /**
     * @param roleMapper      角色表
     * @param roleMenuMapper  角色菜单关联
     * @param roleDeptMapper  角色部门（数据权限）
     * @param userRoleMapper  用户角色关联
     * @param userMapper      用户表
     * @param userRoleBindService 用户角色写入统一入口
     */
    public SysRoleServiceImpl(
            SysRoleMapper roleMapper,
            SysRoleMenuMapper roleMenuMapper,
            SysRoleDeptMapper roleDeptMapper,
            SysUserRoleMapper userRoleMapper,
            SysUserMapper userMapper,
            SysUserRoleBindService userRoleBindService) {
        this.roleMapper = roleMapper;
        this.roleMenuMapper = roleMenuMapper;
        this.roleDeptMapper = roleDeptMapper;
        this.userRoleMapper = userRoleMapper;
        this.userMapper = userMapper;
        this.userRoleBindService = userRoleBindService;
    }

    @Override
    public PageInfo<SysRoleVo> page(SysRoleQueryBo query) {
        int pageNum = normalizePageNum(query.getPageNum());
        int pageSize = normalizePageSize(query.getPageSize());
        LocalDateTime begin = parseBeginTime(query.getBeginTime());
        LocalDateTime end = parseEndTime(query.getEndTime());
        LambdaQueryWrapper<SysRole> w = buildRoleQueryWrapper(query, begin, end)
                .orderByAsc(SysRole::getRoleSort)
                .orderByAsc(SysRole::getRoleId);
        Page<SysRole> mp = roleMapper.selectPage(new Page<>(pageNum, pageSize), w);
        List<SysRoleVo> rows = new ArrayList<>(mp.getRecords().size());
        for (SysRole row : mp.getRecords()) {
            rows.add(toVoBrief(row));
        }
        Page<SysRoleVo> voPage = new Page<>(mp.getCurrent(), mp.getSize(), mp.getTotal());
        voPage.setRecords(rows);
        return PageInfo.from(voPage);
    }

    private SysRoleVo toVoBrief(SysRole row) {
        SysRoleVo vo = BeanUtil.copyProperties(row, SysRoleVo.class);
        vo.setDeptIds(null);
        return vo;
    }

    @Override
    public SysRoleVo getById(Long roleId) {
        SysRole row = roleMapper.selectById(roleId);
        if (row == null) {
            return null;
        }
        SysRoleVo vo = BeanUtil.copyProperties(row, SysRoleVo.class);
        if ("2".equals(row.getDataScope())) {
            List<Long> deptIds = roleDeptMapper.selectList(Wrappers.<SysRoleDept>lambdaQuery()
                            .eq(SysRoleDept::getRoleId, roleId)).stream()
                    .map(SysRoleDept::getDeptId)
                    .toList();
            vo.setDeptIds(deptIds);
        } else {
            vo.setDeptIds(Collections.emptyList());
        }
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void add(SysRoleBo req) {
        checkRoleKeyUnique(req.getRoleKey(), null);
        SysRole entity = BeanUtil.copyProperties(req, SysRole.class);
        if (StrUtil.isBlank(entity.getStatus())) {
            entity.setStatus("0");
        }
        entity.setDataScope("1");
        entity.setDelFlag("0");
        LocalDateTime now = LocalDateTime.now();
        entity.setCreateTime(now);
        entity.setUpdateTime(now);
        entity.setCreateBy(currentOperator());
        entity.setUpdateBy(currentOperator());
        roleMapper.insert(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(SysRoleBo req) {
        SysRole old = roleMapper.selectById(req.getRoleId());
        if (old == null) {
            throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "角色不存在或已删除");
        }
        checkRoleKeyUnique(req.getRoleKey(), req.getRoleId());
        SysRole entity = BeanUtil.copyProperties(req, SysRole.class);
        entity.setDataScope(old.getDataScope());
        entity.setCreateTime(old.getCreateTime());
        entity.setCreateBy(old.getCreateBy());
        entity.setDelFlag(old.getDelFlag());
        entity.setUpdateTime(LocalDateTime.now());
        entity.setUpdateBy(currentOperator());
        roleMapper.updateById(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeBatch(List<Long> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) {
            throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "删除角色ID不能为空");
        }
        if (roleIds.stream().anyMatch(ADMIN_ROLE_ID::equals)) {
            throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "内置超级管理员角色不允许删除");
        }
        for (Long roleId : roleIds) {
            long cnt = userRoleMapper.selectCount(Wrappers.<SysUserRole>lambdaQuery()
                    .eq(SysUserRole::getRoleId, roleId));
            if (cnt > 0) {
                SysRole r = roleMapper.selectById(roleId);
                String name = r != null ? r.getRoleName() : String.valueOf(roleId);
                throw new WarningException(ErrorCodes.Common.INVALID_PARAM,
                        "角色「" + name + "」已分配用户，无法删除");
            }
        }
        List<SysRole> rows = roleMapper.selectBatchIds(roleIds);
        if (rows.size() != roleIds.size()) {
            throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "存在无效的角色ID");
        }
        roleMapper.deleteByIds(roleIds);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void changeStatus(RoleChangeStatusRequest req) {
        SysRole old = roleMapper.selectById(req.getRoleId());
        if (old == null) {
            throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "角色不存在或已删除");
        }
        SysRole patch = new SysRole();
        patch.setRoleId(req.getRoleId());
        patch.setStatus(req.getStatus());
        patch.setUpdateTime(LocalDateTime.now());
        patch.setUpdateBy(currentOperator());
        roleMapper.updateById(patch);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateDataScope(RoleDataScopeRequest req) {
        if (ADMIN_ROLE_ID.equals(req.getRoleId())) {
            throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "内置超级管理员角色的数据权限不允许修改");
        }
        if (!DATA_SCOPES.contains(req.getDataScope())) {
            throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "数据范围取值无效");
        }
        if ("2".equals(req.getDataScope())
                && (req.getDeptIds() == null || req.getDeptIds().isEmpty())) {
            throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "自定义数据权限时至少选择一个部门");
        }
        SysRole old = roleMapper.selectById(req.getRoleId());
        if (old == null) {
            throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "角色不存在或已删除");
        }
        SysRole patch = new SysRole();
        patch.setRoleId(req.getRoleId());
        patch.setDataScope(req.getDataScope());
        patch.setUpdateTime(LocalDateTime.now());
        patch.setUpdateBy(currentOperator());
        roleMapper.updateById(patch);

        roleDeptMapper.delete(Wrappers.<SysRoleDept>lambdaQuery().eq(SysRoleDept::getRoleId, req.getRoleId()));
        if ("2".equals(req.getDataScope())) {
            for (Long deptId : req.getDeptIds()) {
                if (deptId == null || deptId < 1) {
                    continue;
                }
                SysRoleDept rd = new SysRoleDept();
                rd.setRoleId(req.getRoleId());
                rd.setDeptId(deptId);
                roleDeptMapper.insert(rd);
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateMenus(RoleMenuRequest req) {
        if (ADMIN_ROLE_ID.equals(req.getRoleId())) {
            throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "内置超级管理员角色的菜单权限不允许修改");
        }
        SysRole old = roleMapper.selectById(req.getRoleId());
        if (old == null) {
            throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "角色不存在或已删除");
        }
        roleMenuMapper.delete(Wrappers.<SysRoleMenu>lambdaQuery().eq(SysRoleMenu::getRoleId, req.getRoleId()));
        List<Long> menuIds = req.getMenuIds() == null ? Collections.emptyList() : req.getMenuIds();
        for (Long menuId : menuIds) {
            if (menuId == null || menuId < 1) {
                continue;
            }
            SysRoleMenu rm = new SysRoleMenu();
            rm.setRoleId(req.getRoleId());
            rm.setMenuId(menuId);
            roleMenuMapper.insert(rm);
        }
    }

    @Override
    public PageInfo<SysRoleUserVo> pageAllocatedUsers(SysRoleAuthUserQueryBo query) {
        assertRoleAccessible(query.getRoleId());
        int pageNum = normalizePageNum(query.getPageNum());
        int pageSize = normalizePageSize(query.getPageSize());
        LambdaQueryWrapper<SysUser> w = Wrappers.<SysUser>lambdaQuery()
                .apply("user_id IN (SELECT user_id FROM sys_user_role WHERE role_id = {0})", query.getRoleId())
                .like(StrUtil.isNotBlank(query.getUserName()), SysUser::getUserName, query.getUserName())
                .like(StrUtil.isNotBlank(query.getNickName()), SysUser::getNickName, query.getNickName())
                .orderByAsc(SysUser::getUserId);
        Page<SysUser> mp = userMapper.selectPage(new Page<>(pageNum, pageSize), w);
        return mapUserPage(mp);
    }

    @Override
    public PageInfo<SysRoleUserVo> pageUnallocatedUsers(SysRoleAuthUserQueryBo query) {
        assertRoleAccessible(query.getRoleId());
        int pageNum = normalizePageNum(query.getPageNum());
        int pageSize = normalizePageSize(query.getPageSize());
        LambdaQueryWrapper<SysUser> w = Wrappers.<SysUser>lambdaQuery()
                .apply("user_id NOT IN (SELECT user_id FROM sys_user_role WHERE role_id = {0})", query.getRoleId())
                .like(StrUtil.isNotBlank(query.getUserName()), SysUser::getUserName, query.getUserName())
                .like(StrUtil.isNotBlank(query.getNickName()), SysUser::getNickName, query.getNickName())
                .orderByAsc(SysUser::getUserId);
        Page<SysUser> mp = userMapper.selectPage(new Page<>(pageNum, pageSize), w);
        return mapUserPage(mp);
    }

    private void assertRoleAccessible(Long roleId) {
        SysRole r = roleMapper.selectById(roleId);
        if (r == null) {
            throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "角色不存在或已删除");
        }
    }

    private PageInfo<SysRoleUserVo> mapUserPage(Page<SysUser> mp) {
        List<SysRoleUserVo> rows = new ArrayList<>(mp.getRecords().size());
        for (SysUser u : mp.getRecords()) {
            rows.add(BeanUtil.copyProperties(u, SysRoleUserVo.class));
        }
        Page<SysRoleUserVo> voPage = new Page<>(mp.getCurrent(), mp.getSize(), mp.getTotal());
        voPage.setRecords(rows);
        return PageInfo.from(voPage);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void grantUsers(RoleGrantUsersRequest req) {
        assertRoleAccessible(req.getRoleId());
        if (req.getUserIds() == null || req.getUserIds().isEmpty()) {
            throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "用户ID列表不能为空");
        }
        for (Long uid : req.getUserIds()) {
            if (uid == null || uid < 1) {
                continue;
            }
            SysUser u = userMapper.selectById(uid);
            if (u == null) {
                throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "用户不存在或已删除：id=" + uid);
            }
            userRoleBindService.ensureUserHasRole(uid, req.getRoleId());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelUser(RoleCancelUserRequest req) {
        userRoleMapper.delete(Wrappers.<SysUserRole>lambdaQuery()
                .eq(SysUserRole::getRoleId, req.getRoleId())
                .eq(SysUserRole::getUserId, req.getUserId()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelUsers(RoleGrantUsersRequest req) {
        if (req.getUserIds() == null || req.getUserIds().isEmpty()) {
            throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "用户ID列表不能为空");
        }
        for (Long uid : req.getUserIds()) {
            userRoleMapper.delete(Wrappers.<SysUserRole>lambdaQuery()
                    .eq(SysUserRole::getRoleId, req.getRoleId())
                    .eq(SysUserRole::getUserId, uid));
        }
    }

    @Override
    public void export(SysRoleQueryBo query, HttpServletResponse response) {
        LocalDateTime begin = parseBeginTime(query.getBeginTime());
        LocalDateTime end = parseEndTime(query.getEndTime());
        LambdaQueryWrapper<SysRole> w = buildRoleQueryWrapper(query, begin, end)
                .orderByAsc(SysRole::getRoleSort)
                .orderByAsc(SysRole::getRoleId);
        List<SysRole> rows = roleMapper.selectList(w);
        List<SysRoleExcelRow> out = new ArrayList<>(rows.size());
        for (SysRole row : rows) {
            SysRoleExcelRow er = BeanUtil.copyProperties(row, SysRoleExcelRow.class);
            er.setStatusLabel("0".equals(row.getStatus()) ? "正常" : "停用");
            er.setDataScopeLabel(dataScopeLabel(row.getDataScope()));
            out.add(er);
        }
        ExcelUtils.exportExcel(out, "sys-role", SysRoleExcelRow.class, response);
    }

    private static String dataScopeLabel(String ds) {
        if (ds == null) {
            return "";
        }
        return switch (ds) {
            case "1" -> "全部";
            case "2" -> "自定义";
            case "3" -> "本部门";
            case "4" -> "本部门及以下";
            case "5" -> "仅本人";
            default -> ds;
        };
    }

    private LambdaQueryWrapper<SysRole> buildRoleQueryWrapper(SysRoleQueryBo query,
                                                              LocalDateTime begin,
                                                              LocalDateTime end) {
        return Wrappers.<SysRole>lambdaQuery()
                .like(StrUtil.isNotBlank(query.getRoleName()), SysRole::getRoleName, query.getRoleName())
                .like(StrUtil.isNotBlank(query.getRoleKey()), SysRole::getRoleKey, query.getRoleKey())
                .eq(StrUtil.isNotBlank(query.getStatus()), SysRole::getStatus, query.getStatus())
                .ge(begin != null, SysRole::getCreateTime, begin)
                .le(end != null, SysRole::getCreateTime, end);
    }

    private void checkRoleKeyUnique(String roleKey, Long excludeId) {
        var q = Wrappers.<SysRole>lambdaQuery().eq(SysRole::getRoleKey, roleKey);
        if (excludeId != null) {
            q.ne(SysRole::getRoleId, excludeId);
        }
        if (roleMapper.selectCount(q) > 0) {
            throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "权限字符已存在");
        }
    }

    private static int normalizePageNum(Integer pageNum) {
        return pageNum == null || pageNum < 1 ? 1 : pageNum;
    }

    private static int normalizePageSize(Integer pageSize) {
        return pageSize == null || pageSize < 1 ? 10 : pageSize;
    }

    private static LocalDateTime parseBeginTime(String beginTime) {
        if (StrUtil.isBlank(beginTime)) {
            return null;
        }
        return LocalDateTime.parse(beginTime.trim() + "T00:00:00");
    }

    private static LocalDateTime parseEndTime(String endTime) {
        if (StrUtil.isBlank(endTime)) {
            return null;
        }
        return LocalDateTime.of(java.time.LocalDate.parse(endTime.trim()), LocalTime.MAX);
    }

    private static String currentOperator() {
        try {
            if (StpUtil.isLogin()) {
                return String.valueOf(StpUtil.getLoginId());
            }
        } catch (Exception ignored) {
            // 非 Web 线程或未登录
        }
        return "system";
    }
}
