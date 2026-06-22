package io.github.genkidoudou.web.system.role.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.github.genkidoudou.common.api.PageInfo;
import io.github.genkidoudou.common.excel.ExcelImportResult;
import io.github.genkidoudou.common.excel.ExcelResult;
import io.github.genkidoudou.common.excel.ExcelUtils;
import io.github.genkidoudou.common.excel.exception.ExcelDataCheckException;
import io.github.genkidoudou.common.exception.ErrorCodes;
import io.github.genkidoudou.common.exception.WarningException;
import io.github.genkidoudou.common.validation.ValidatorUtils;
import io.github.genkidoudou.common.validation.group.AddGroup;
import io.github.genkidoudou.common.validation.group.UpdateGroup;
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
import io.github.genkidoudou.web.system.role.dto.SysRoleImportExcelRow;
import io.github.genkidoudou.web.system.role.dto.SysRoleQueryBo;
import io.github.genkidoudou.web.system.role.dto.SysRoleUserVo;
import io.github.genkidoudou.web.system.role.dto.SysRoleVo;
import io.github.genkidoudou.web.system.role.mapper.SysRoleDeptMapper;
import io.github.genkidoudou.web.system.role.service.SysRoleService;
import io.github.genkidoudou.web.system.user.domain.SysUser;
import io.github.genkidoudou.web.system.user.mapper.SysUserMapper;
import io.github.genkidoudou.web.system.user.service.SysUserRoleBindService;
import io.github.genkidoudou.web.system.user.authcache.UserAuthCacheService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.sql.Timestamp;
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
    private final UserAuthCacheService userAuthCacheService;

    private final JdbcTemplate jdbcTemplate;

    /**
     * @param roleMapper      角色表
     * @param roleMenuMapper  角色菜单关联
     * @param roleDeptMapper  角色部门（数据权限）
     * @param userRoleMapper  用户角色关联
     * @param userMapper      用户表
     * @param userRoleBindService 用户角色写入统一入口
     * @param jdbcTemplate    用于绕过 MP 逻辑删除 SQL 改写，释放已删除行占用的 {@code role_key} 唯一键
     */
    public SysRoleServiceImpl(
            SysRoleMapper roleMapper,
            SysRoleMenuMapper roleMenuMapper,
            SysRoleDeptMapper roleDeptMapper,
            SysUserRoleMapper userRoleMapper,
            SysUserMapper userMapper,
            SysUserRoleBindService userRoleBindService,
            UserAuthCacheService userAuthCacheService,
            JdbcTemplate jdbcTemplate) {
        this.roleMapper = roleMapper;
        this.roleMenuMapper = roleMenuMapper;
        this.roleDeptMapper = roleDeptMapper;
        this.userRoleMapper = userRoleMapper;
        this.userMapper = userMapper;
        this.userRoleBindService = userRoleBindService;
        this.userAuthCacheService = userAuthCacheService;
        this.jdbcTemplate = jdbcTemplate;
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
        if (StrUtil.isNotBlank(entity.getRoleKey())) {
            entity.setRoleKey(entity.getRoleKey().trim());
        }
        if (StrUtil.isBlank(entity.getStatus())) {
            entity.setStatus("0");
        }
        String scope = normalizeDataScope(req.getDataScope(), "1");
        if ("2".equals(scope)) {
            assertCustomDeptIdsNonEmpty(req.getDeptIds());
        }
        entity.setDataScope(scope);
        entity.setDelFlag("0");
        LocalDateTime now = LocalDateTime.now();
        entity.setCreateTime(now);
        entity.setUpdateTime(now);
        entity.setCreateBy(currentOperator());
        entity.setUpdateBy(currentOperator());
        roleMapper.insert(entity);
        replaceRoleDeptBindings(entity.getRoleId(), scope, req.getDeptIds());
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
        if (StrUtil.isNotBlank(entity.getRoleKey())) {
            entity.setRoleKey(entity.getRoleKey().trim());
        }
        String effectiveScope;
        if (ADMIN_ROLE_ID.equals(req.getRoleId())) {
            effectiveScope = old.getDataScope();
        } else {
            effectiveScope = normalizeDataScope(req.getDataScope(), old.getDataScope());
            if ("2".equals(effectiveScope)) {
                assertCustomDeptIdsNonEmpty(req.getDeptIds());
            }
        }
        entity.setDataScope(effectiveScope);
        entity.setCreateTime(old.getCreateTime());
        entity.setCreateBy(old.getCreateBy());
        entity.setDelFlag(old.getDelFlag());
        entity.setUpdateTime(LocalDateTime.now());
        entity.setUpdateBy(currentOperator());
        roleMapper.updateById(entity);
        if (!ADMIN_ROLE_ID.equals(req.getRoleId())) {
            replaceRoleDeptBindings(req.getRoleId(), effectiveScope, req.getDeptIds());
        }
        userAuthCacheService.evictUsersByRoleId(req.getRoleId());
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
        // 逻辑删除前改写 role_key，避免 uk_sys_role_key 仍占用已删除行导致无法新建同权限字符
        tombstoneRoleKeysBeforeLogicalRemove(rows);
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
        replaceRoleDeptBindings(req.getRoleId(), req.getDataScope(), req.getDeptIds());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateMenus(RoleMenuRequest req) {
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
        userAuthCacheService.evictUsersByRoleId(req.getRoleId());
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
        userAuthCacheService.evictUsersByRoleId(req.getRoleId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelUser(RoleCancelUserRequest req) {
        userRoleMapper.delete(Wrappers.<SysUserRole>lambdaQuery()
                .eq(SysUserRole::getRoleId, req.getRoleId())
                .eq(SysUserRole::getUserId, req.getUserId()));
        userAuthCacheService.evictUser(req.getUserId());
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
            userAuthCacheService.evictUser(uid);
        }
    }

    @Override
    public void export(SysRoleQueryBo query, HttpServletResponse response) {
        List<SysRoleExcelRow> out = loadRoleExcelRows(query, Integer.MAX_VALUE);
        ExcelUtils.exportExcel(out, "sys-role", SysRoleExcelRow.class, response);
    }

    @Override
    public long countExportRows(SysRoleQueryBo query) {
        LocalDateTime begin = parseBeginTime(query.getBeginTime());
        LocalDateTime end = parseEndTime(query.getEndTime());
        Long c = roleMapper.selectCount(buildRoleQueryWrapper(query, begin, end));
        return c == null ? 0L : c;
    }

    @Override
    public byte[] exportExcelBytes(SysRoleQueryBo query, int maxRows) {
        return ExcelUtils.writeBytes("sys-role", SysRoleExcelRow.class, loadRoleExcelRows(query, maxRows));
    }

    private List<SysRoleExcelRow> loadRoleExcelRows(SysRoleQueryBo query, int maxRows) {
        LocalDateTime begin = parseBeginTime(query.getBeginTime());
        LocalDateTime end = parseEndTime(query.getEndTime());
        LambdaQueryWrapper<SysRole> w = buildRoleQueryWrapper(query, begin, end)
            .orderByAsc(SysRole::getRoleSort)
            .orderByAsc(SysRole::getRoleId);
        int limit = Math.max(1, maxRows);
        List<SysRole> rows = roleMapper.selectList(w.last("LIMIT " + limit));
        List<SysRoleExcelRow> out = new ArrayList<>(rows.size());
        for (SysRole row : rows) {
            SysRoleExcelRow er = BeanUtil.copyProperties(row, SysRoleExcelRow.class);
            er.setStatusLabel("0".equals(row.getStatus()) ? "正常" : "停用");
            er.setDataScopeLabel(dataScopeLabel(row.getDataScope()));
            out.add(er);
        }
        return out;
    }

    @Override
    public ExcelImportResult importData(MultipartFile file, boolean updateSupport) throws IOException {
        ExcelResult<SysRoleImportExcelRow> readResult = ExcelUtils.importExcel(
                file.getInputStream(),
                SysRoleImportExcelRow.class,
                (row, context) -> importOneRoleRow(row, updateSupport),
                (rows, context) -> {
                });
        return ExcelImportResult.build(readResult);
    }

    @Override
    public void importRoleExcelRow(SysRoleImportExcelRow row, boolean updateSupport) {
        importOneRoleRow(row, updateSupport);
    }

    /**
     * 处理单行导入；失败抛出 {@link ExcelDataCheckException} 以便写入失败明细。
     */
    private void importOneRoleRow(SysRoleImportExcelRow row, boolean updateSupport) {
        if (isBlankImportRow(row)) {
            return;
        }
        String roleKey = StrUtil.trim(row.getRoleKey());
        String roleName = StrUtil.trim(row.getRoleName());
        if (StrUtil.isBlank(roleKey) || StrUtil.isBlank(roleName)) {
            throw new ExcelDataCheckException("角色名称、权限字符不能为空");
        }
        SysRole existed = roleMapper.selectOne(Wrappers.<SysRole>lambdaQuery().eq(SysRole::getRoleKey, roleKey), false);
        String resolvedScope = parseDataScopeLabel(row.getDataScope());
        List<Long> deptIds = Collections.emptyList();
        if ("2".equals(resolvedScope)) {
            if (existed == null) {
                throw new ExcelDataCheckException("新建角色不允许使用自定义数据范围");
            }
            if (!"2".equals(existed.getDataScope())) {
                throw new ExcelDataCheckException("不能通过导入将数据范围改为自定义，请在界面配置");
            }
            SysRoleVo vo = getById(existed.getRoleId());
            if (vo != null && vo.getDeptIds() != null && !vo.getDeptIds().isEmpty()) {
                deptIds = new ArrayList<>(vo.getDeptIds());
            }
            if (deptIds.isEmpty()) {
                throw new ExcelDataCheckException("该角色为自定义但未配置部门，请先在界面保存部门后再导入");
            }
        }

        SysRoleBo bo = new SysRoleBo();
        bo.setRoleName(roleName);
        bo.setRoleKey(roleKey);
        bo.setRoleSort(row.getRoleSort() != null ? row.getRoleSort() : 0);
        bo.setStatus(normalizeImportStatus(row.getStatus()));
        bo.setRemark(StrUtil.trim(StrUtil.nullToDefault(row.getRemark(), "")));

        try {
            if (existed == null) {
                bo.setDataScope(resolvedScope);
                bo.setDeptIds(Collections.emptyList());
                ValidatorUtils.validate(bo, AddGroup.class);
                add(bo);
            } else {
                if (!updateSupport) {
                    throw new ExcelDataCheckException("权限字符已存在：" + roleKey);
                }
                bo.setRoleId(existed.getRoleId());
                if (ADMIN_ROLE_ID.equals(existed.getRoleId())) {
                    bo.setRoleKey("admin");
                    bo.setDataScope(null);
                    bo.setDeptIds(null);
                } else {
                    bo.setDataScope(resolvedScope);
                    bo.setDeptIds(deptIds);
                }
                ValidatorUtils.validate(bo, UpdateGroup.class);
                update(bo);
            }
        } catch (WarningException e) {
            throw new ExcelDataCheckException(e.getMessage());
        }
    }

    private boolean isBlankImportRow(SysRoleImportExcelRow row) {
        if (row == null) {
            return true;
        }
        return StrUtil.isAllBlank(row.getRoleName(), row.getRoleKey(), row.getStatus(), row.getRemark(), row.getDataScope())
                && row.getRoleSort() == null;
    }

    private static String normalizeImportStatus(String status) {
        if (StrUtil.equalsAny(status, "0", "1")) {
            return status;
        }
        return "0";
    }

    private String parseDataScopeLabel(String raw) {
        if (StrUtil.isBlank(raw)) {
            return "1";
        }
        String s = raw.trim();
        if (DATA_SCOPES.contains(s)) {
            return s;
        }
        return switch (s) {
            case "全部" -> "1";
            case "自定义" -> "2";
            case "本部门" -> "3";
            case "本部门及以下" -> "4";
            case "仅本人" -> "5";
            default -> throw new ExcelDataCheckException("数据范围取值无效: " + s);
        };
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
        if (StrUtil.isBlank(roleKey)) {
            return;
        }
        String key = roleKey.trim();
        // 历史数据：已逻辑删除行仍占用 role_key，MP 默认查询看不到，但唯一索引仍会拦截 INSERT
        releaseRoleKeyHeldByDeletedRows(key);
        var q = Wrappers.<SysRole>lambdaQuery().eq(SysRole::getRoleKey, key);
        if (excludeId != null) {
            q.ne(SysRole::getRoleId, excludeId);
        }
        if (roleMapper.selectCount(q) > 0) {
            throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "权限字符已存在");
        }
    }

    /**
     * 将已删除且仍占用目标 role_key 的行改为墓碑值，避免与 uk_sys_role_key 冲突。
     */
    private void releaseRoleKeyHeldByDeletedRows(String normalizedKey) {
        // MP 逻辑删除会改写 SELECT/UPDATE，无法可靠处理 del_flag=1 的行；JdbcTemplate 走原生 SQL 且参与当前事务
        String selectSql = "SELECT role_id FROM sys_role WHERE del_flag = '1' AND TRIM(role_key) = ?";
        List<Long> ids = jdbcTemplate.query(selectSql, (rs, rowNum) -> rs.getLong(1), normalizedKey);
        if (ids == null || ids.isEmpty()) {
            return;
        }
        LocalDateTime now = LocalDateTime.now();
        Timestamp ts = Timestamp.valueOf(now);
        String updateSql = "UPDATE sys_role SET role_key = ?, update_time = ? WHERE role_id = ?";
        for (Long rid : ids) {
            if (rid == null) {
                continue;
            }
            String suffix = "_del_" + rid;
            String base = normalizedKey;
            int maxLen = 100;
            if (base.length() + suffix.length() > maxLen) {
                base = StrUtil.subPre(base, Math.max(1, maxLen - suffix.length()));
            }
            jdbcTemplate.update(updateSql, base + suffix, ts, rid);
        }
    }

    /**
     * 逻辑删除前为 role_key 追加墓碑后缀，使全局唯一索引可被新角色复用（与 {@link SysRole#getDelFlag} 逻辑删除配合）。
     */
    private void tombstoneRoleKeysBeforeLogicalRemove(List<SysRole> rows) {
        for (SysRole row : rows) {
            if (row == null || StrUtil.isBlank(row.getRoleKey())) {
                continue;
            }
            String suffix = "_del_" + row.getRoleId();
            String base = row.getRoleKey().trim();
            int maxLen = 100;
            if (base.length() + suffix.length() > maxLen) {
                base = StrUtil.subPre(base, Math.max(1, maxLen - suffix.length()));
            }
            String nextKey = base + suffix;
            SysRole patch = new SysRole();
            patch.setRoleId(row.getRoleId());
            patch.setRoleKey(nextKey);
            roleMapper.updateById(patch);
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

    /**
     * 解析数据范围：请求值合法则采用，否则采用 fallback（须合法），最终默认全部。
     */
    private String normalizeDataScope(String requested, String fallback) {
        if (StrUtil.isNotBlank(requested) && DATA_SCOPES.contains(requested)) {
            return requested;
        }
        if (StrUtil.isNotBlank(fallback) && DATA_SCOPES.contains(fallback)) {
            return fallback;
        }
        return "1";
    }

    private void assertCustomDeptIdsNonEmpty(List<Long> deptIds) {
        if (deptIds == null || deptIds.isEmpty()) {
            throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "自定义数据权限时至少选择一个部门");
        }
    }

    /**
     * 重写角色与部门的数据权限关联表（非自定义范围时清空）。
     */
    private void replaceRoleDeptBindings(Long roleId, String dataScope, List<Long> deptIds) {
        roleDeptMapper.delete(Wrappers.<SysRoleDept>lambdaQuery().eq(SysRoleDept::getRoleId, roleId));
        if (!"2".equals(dataScope) || deptIds == null) {
            return;
        }
        for (Long deptId : deptIds) {
            if (deptId == null || deptId < 1) {
                continue;
            }
            SysRoleDept rd = new SysRoleDept();
            rd.setRoleId(roleId);
            rd.setDeptId(deptId);
            roleDeptMapper.insert(rd);
        }
    }
}
