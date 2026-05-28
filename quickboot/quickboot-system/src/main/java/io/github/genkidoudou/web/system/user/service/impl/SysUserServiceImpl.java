package io.github.genkidoudou.web.system.user.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.github.genkidoudou.common.api.PageInfo;
import io.github.genkidoudou.common.excel.ExcelUtils;
import io.github.genkidoudou.common.exception.ErrorCodes;
import io.github.genkidoudou.common.exception.WarningException;
import io.github.genkidoudou.common.security.firewall.password.PasswordCodec;
import io.github.genkidoudou.web.system.dept.DeptSubtreeHelper;
import io.github.genkidoudou.web.system.dept.domain.SysDept;
import io.github.genkidoudou.web.system.dept.mapper.SysDeptMapper;
import io.github.genkidoudou.web.system.menu.domain.SysRole;
import io.github.genkidoudou.web.system.menu.domain.SysUserRole;
import io.github.genkidoudou.web.system.menu.mapper.SysRoleMapper;
import io.github.genkidoudou.web.system.menu.mapper.SysUserRoleMapper;
import io.github.genkidoudou.web.system.role.dto.SysRoleVo;
import io.github.genkidoudou.web.system.user.datascope.DataPermission;
import io.github.genkidoudou.web.system.user.domain.SysUser;
import io.github.genkidoudou.web.system.user.dto.SysUserCreateBo;
import io.github.genkidoudou.web.system.user.dto.SysUserDetailVo;
import io.github.genkidoudou.web.system.user.dto.SysUserExcelRow;
import io.github.genkidoudou.web.system.user.dto.SysUserImportExcelRow;
import io.github.genkidoudou.web.system.user.dto.SysUserImportFailRow;
import io.github.genkidoudou.web.system.user.dto.SysUserQueryBo;
import io.github.genkidoudou.web.system.user.dto.SysUserUpdateBo;
import io.github.genkidoudou.web.system.user.dto.SysUserVo;
import io.github.genkidoudou.web.system.user.dto.UserAuthRoleRequest;
import io.github.genkidoudou.web.system.user.dto.UserAuthRoleVo;
import io.github.genkidoudou.web.system.user.dto.UserChangeStatusRequest;
import io.github.genkidoudou.web.system.user.dto.UserImportResultVo;
import io.github.genkidoudou.web.system.user.dto.UserResetPwdRequest;
import io.github.genkidoudou.web.system.user.datascope.DataScopeSession;
import io.github.genkidoudou.web.system.user.datascope.DataScopeSessionStore;
import io.github.genkidoudou.web.system.user.mapper.SysUserMapper;
import io.github.genkidoudou.web.system.user.service.SysUserRoleBindService;
import io.github.genkidoudou.web.system.user.service.SysUserService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 系统用户管理实现。
 */
@Service
public class SysUserServiceImpl implements SysUserService {

    /** 内置超级管理员用户主键（与 Flyway 种子一致）。 */
    public static final long ADMIN_USER_ID = 1L;

    private static final long IMPORT_ERROR_TTL_MS = 15L * 60L * 1000L;

    private final SysUserMapper userMapper;
    private final SysDeptMapper deptMapper;
    private final SysUserRoleMapper userRoleMapper;
    private final SysRoleMapper roleMapper;
    private final PasswordCodec passwordCodec;
    private final SysUserRoleBindService userRoleBindService;

    private final ConcurrentHashMap<String, CachedExport> importErrorCache = new ConcurrentHashMap<>();

    private static final class CachedExport {
        private final byte[] bytes;
        private final long expireAtEpochMs;

        private CachedExport(byte[] bytes, long expireAtEpochMs) {
            this.bytes = bytes;
            this.expireAtEpochMs = expireAtEpochMs;
        }
    }

    public SysUserServiceImpl(
            SysUserMapper userMapper,
            SysDeptMapper deptMapper,
            SysUserRoleMapper userRoleMapper,
            SysRoleMapper roleMapper,
            PasswordCodec passwordCodec,
            SysUserRoleBindService userRoleBindService) {
        this.userMapper = userMapper;
        this.deptMapper = deptMapper;
        this.userRoleMapper = userRoleMapper;
        this.roleMapper = roleMapper;
        this.passwordCodec = passwordCodec;
        this.userRoleBindService = userRoleBindService;
    }

    @DataPermission(tables = "sys_user")
    @Override
    public PageInfo<SysUserVo> page(SysUserQueryBo query) {
        int pageNum = normalizePageNum(query.getPageNum());
        int pageSize = normalizePageSize(query.getPageSize());
        LocalDateTime begin = parseBeginTime(query.getBeginTime());
        LocalDateTime end = parseEndTime(query.getEndTime());
        LambdaQueryWrapper<SysUser> w = buildUserQueryWrapper(query, begin, end)
                .orderByAsc(SysUser::getUserId);
        Page<SysUser> mp = userMapper.selectPage(new Page<>(pageNum, pageSize), w);
        List<SysUserVo> rows = toUserVos(mp.getRecords());
        Page<SysUserVo> voPage = new Page<>(mp.getCurrent(), mp.getSize(), mp.getTotal());
        voPage.setRecords(rows);
        return PageInfo.from(voPage);
    }

    private LambdaQueryWrapper<SysUser> buildUserQueryWrapper(SysUserQueryBo query,
                                                              LocalDateTime begin,
                                                              LocalDateTime end) {
        LambdaQueryWrapper<SysUser> w = Wrappers.<SysUser>lambdaQuery()
                .like(StrUtil.isNotBlank(query.getUserName()), SysUser::getUserName, query.getUserName())
                .like(StrUtil.isNotBlank(query.getNickName()), SysUser::getNickName, query.getNickName())
                .like(StrUtil.isNotBlank(query.getPhonenumber()), SysUser::getPhonenumber, query.getPhonenumber())
                .eq(StrUtil.isNotBlank(query.getStatus()), SysUser::getStatus, query.getStatus())
                .ge(begin != null, SysUser::getCreateTime, begin)
                .le(end != null, SysUser::getCreateTime, end);
        if (query.getDeptId() != null) {
            Set<Long> deptIds = DeptSubtreeHelper.collectDeptSubtreeIds(loadAllDepts(), query.getDeptId());
            if (deptIds.isEmpty()) {
                w.apply("1 = 0");
            } else {
                w.in(SysUser::getDeptId, deptIds);
            }
        }
        return w;
    }

    private List<SysDept> loadAllDepts() {
        return deptMapper.selectList(new LambdaQueryWrapper<SysDept>().orderByAsc(SysDept::getOrderNum));
    }

    private void assertLoginUserHasDeptForMutatingUser() {
        try {
            if (!StpUtil.isLogin()) {
                return;
            }
        } catch (Exception ignored) {
            return;
        }
        DataScopeSession s = DataScopeSessionStore.get();
        if (s == null) {
            return;
        }
        if (s.loginDeptId() == null) {
            throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "当前用户未分配部门，不允许执行该操作");
        }
    }
    private List<SysUserVo> toUserVos(List<SysUser> users) {
        if (users.isEmpty()) {
            return List.of();
        }
        Set<Long> deptIds = users.stream().map(SysUser::getDeptId).filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Long, String> deptNameMap = new HashMap<>();
        if (!deptIds.isEmpty()) {
            List<SysDept> depts = deptMapper.selectList(Wrappers.<SysDept>lambdaQuery().in(SysDept::getDeptId, deptIds));
            for (SysDept d : depts) {
                deptNameMap.put(d.getDeptId(), d.getDeptName());
            }
        }
        List<Long> userIds = users.stream().map(SysUser::getUserId).toList();
        Map<Long, String> roleNamesMap = loadRoleNamesByUserIds(userIds);
        List<SysUserVo> out = new ArrayList<>(users.size());
        for (SysUser u : users) {
            SysUserVo vo = BeanUtil.copyProperties(u, SysUserVo.class);
            vo.setUserId(u.getUserId());
            if (u.getDeptId() != null) {
                vo.setDeptName(deptNameMap.getOrDefault(u.getDeptId(), ""));
            } else {
                vo.setDeptName("");
            }
            vo.setRoleNames(roleNamesMap.getOrDefault(u.getUserId(), ""));
            out.add(vo);
        }
        return out;
    }

    private Map<Long, String> loadRoleNamesByUserIds(List<Long> userIds) {
        if (userIds.isEmpty()) {
            return Map.of();
        }
        List<SysUserRole> urs = userRoleMapper.selectList(Wrappers.<SysUserRole>lambdaQuery()
                .in(SysUserRole::getUserId, userIds));
        if (urs.isEmpty()) {
            return Map.of();
        }
        Set<Long> roleIds = urs.stream().map(SysUserRole::getRoleId).collect(Collectors.toSet());
        List<SysRole> roles = roleMapper.selectList(Wrappers.<SysRole>lambdaQuery().in(SysRole::getRoleId, roleIds));
        Map<Long, String> roleIdToName = roles.stream().collect(Collectors.toMap(SysRole::getRoleId, SysRole::getRoleName, (a, b) -> a));
        Map<Long, List<String>> tmp = new HashMap<>();
        for (SysUserRole ur : urs) {
            String name = roleIdToName.getOrDefault(ur.getRoleId(), "");
            tmp.computeIfAbsent(ur.getUserId(), k -> new ArrayList<>()).add(name);
        }
        Map<Long, String> out = new HashMap<>();
        for (Map.Entry<Long, List<String>> e : tmp.entrySet()) {
            out.put(e.getKey(), String.join(",", e.getValue()));
        }
        return out;
    }

    @Override
    public SysUserDetailVo get(Long userId) {
        SysUser u = userMapper.selectById(userId);
        if (u == null) {
            return null;
        }
        SysUserDetailVo vo = BeanUtil.copyProperties(u, SysUserDetailVo.class);
        vo.setUserId(u.getUserId());
        List<Long> roleIds = userRoleMapper.selectList(Wrappers.<SysUserRole>lambdaQuery()
                        .eq(SysUserRole::getUserId, userId)).stream()
                .map(SysUserRole::getRoleId)
                .toList();
        vo.setRoleIds(roleIds);
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void create(SysUserCreateBo bo) {
        assertLoginUserHasDeptForMutatingUser();
        assertUserNameUnique(bo.getUserName(), null);
        assertDeptExists(bo.getDeptId());
        assertRolesValid(bo.getRoleIds());
        assertEmailPhone(bo.getEmail(), bo.getPhonenumber());
        SysUser u = new SysUser();
        u.setDeptId(bo.getDeptId());
        u.setUserName(bo.getUserName());
        u.setNickName(bo.getNickName());
        u.setEmail(StrUtil.blankToDefault(bo.getEmail(), null));
        u.setPhonenumber(StrUtil.blankToDefault(bo.getPhonenumber(), null));
        u.setSex(StrUtil.blankToDefault(bo.getSex(), "0"));
        u.setStatus(StrUtil.blankToDefault(bo.getStatus(), "0"));
        u.setRemark(StrUtil.blankToDefault(bo.getRemark(), null));
        u.setUserType("00");
        u.setPassword(passwordCodec.encrypt(bo.getPassword(), "bcrypt"));
        userMapper.insert(u);
        userRoleBindService.replaceAllRolesForUser(u.getUserId(), bo.getRoleIds());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(SysUserUpdateBo bo) {
        assertLoginUserHasDeptForMutatingUser();
        SysUser existing = userMapper.selectById(bo.getUserId());
        if (existing == null) {
            throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "用户不存在或已删除");
        }
        assertDeptExists(bo.getDeptId());
        assertRolesValid(bo.getRoleIds());
        assertEmailPhone(bo.getEmail(), bo.getPhonenumber());
        if (existing.getUserId() != null && existing.getUserId() == ADMIN_USER_ID && "1".equals(bo.getStatus())) {
            throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "不允许停用内置超级管理员用户");
        }
        existing.setNickName(bo.getNickName());
        existing.setDeptId(bo.getDeptId());
        existing.setEmail(StrUtil.blankToDefault(bo.getEmail(), null));
        existing.setPhonenumber(StrUtil.blankToDefault(bo.getPhonenumber(), null));
        existing.setSex(StrUtil.blankToDefault(bo.getSex(), "0"));
        existing.setStatus(bo.getStatus());
        existing.setRemark(StrUtil.blankToDefault(bo.getRemark(), null));
        if (StrUtil.isNotBlank(bo.getPassword())) {
            existing.setPassword(passwordCodec.encrypt(bo.getPassword(), "bcrypt"));
        }
        userMapper.updateById(existing);
        userRoleBindService.replaceAllRolesForUser(bo.getUserId(), bo.getRoleIds());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void remove(List<Long> userIds) {
        assertLoginUserHasDeptForMutatingUser();
        if (userIds == null || userIds.isEmpty()) {
            throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "用户ID列表不能为空");
        }
        if (userIds.stream().anyMatch(id -> id != null && id.longValue() == ADMIN_USER_ID)) {
            throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "不允许删除内置超级管理员用户");
        }
        for (Long id : userIds) {
            if (id == null || id < 1) {
                continue;
            }
            userMapper.deleteById(id);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void changeStatus(UserChangeStatusRequest req) {
        assertLoginUserHasDeptForMutatingUser();
        SysUser u = userMapper.selectById(req.getUserId());
        if (u == null) {
            throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "用户不存在或已删除");
        }
        if (u.getUserId() != null && u.getUserId() == ADMIN_USER_ID && "1".equals(req.getStatus())) {
            throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "不允许停用内置超级管理员用户");
        }
        u.setStatus(req.getStatus());
        userMapper.updateById(u);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resetPwd(UserResetPwdRequest req) {
        assertLoginUserHasDeptForMutatingUser();
        SysUser u = userMapper.selectById(req.getUserId());
        if (u == null) {
            throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "用户不存在或已删除");
        }
        u.setPassword(passwordCodec.encrypt(req.getNewPassword(), "bcrypt"));
        userMapper.updateById(u);
    }

    @Override
    public UserAuthRoleVo authRoleInfo(Long userId) {
        SysUser u = userMapper.selectById(userId);
        if (u == null) {
            throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "用户不存在或已删除");
        }
        List<SysRole> roles = roleMapper.selectList(Wrappers.<SysRole>lambdaQuery()
                .eq(SysRole::getStatus, "0")
                .orderByAsc(SysRole::getRoleSort)
                .orderByAsc(SysRole::getRoleId));
        List<SysRoleVo> roleVos = new ArrayList<>(roles.size());
        for (SysRole r : roles) {
            roleVos.add(BeanUtil.copyProperties(r, SysRoleVo.class));
        }
        List<Long> roleIds = userRoleMapper.selectList(Wrappers.<SysUserRole>lambdaQuery()
                        .eq(SysUserRole::getUserId, userId)).stream()
                .map(SysUserRole::getRoleId)
                .toList();
        UserAuthRoleVo vo = new UserAuthRoleVo();
        vo.setUserId(u.getUserId());
        vo.setUserName(u.getUserName());
        vo.setNickName(u.getNickName());
        vo.setRoles(roleVos);
        vo.setRoleIds(roleIds);
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveAuthRole(UserAuthRoleRequest req) {
        assertLoginUserHasDeptForMutatingUser();
        SysUser u = userMapper.selectById(req.getUserId());
        if (u == null) {
            throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "用户不存在或已删除");
        }
        assertRolesValid(req.getRoleIds());
        userRoleBindService.replaceAllRolesForUser(req.getUserId(), req.getRoleIds());
    }

    @Override
    public UserImportResultVo importData(MultipartFile file, boolean updateSupport) {
        assertLoginUserHasDeptForMutatingUser();
        if (file == null || file.isEmpty()) {
            throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "导入文件不能为空");
        }
        List<SysUserImportExcelRow> rows;
        try {
            rows = ExcelUtils.importExcel(file.getInputStream(), SysUserImportExcelRow.class);
        } catch (Exception e) {
            throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "解析 Excel 失败：" + e.getMessage());
        }
        UserImportResultVo vo = new UserImportResultVo();
        vo.setTotal(rows.size());
        int ok = 0;
        List<SysUserImportFailRow> failRows = new ArrayList<>();
        List<String> failMsgs = new ArrayList<>();
        Map<String, SysRole> roleByKey = loadActiveRolesByKey();
        for (int i = 0; i < rows.size(); i++) {
            SysUserImportExcelRow row = rows.get(i);
            int lineNo = i + 2;
            try {
                processImportRow(row, updateSupport, roleByKey);
                ok++;
            } catch (WarningException ex) {
                failRows.add(new SysUserImportFailRow(StrUtil.nullToEmpty(row.getUserName()), ex.getMsg()));
                if (failMsgs.size() < 30) {
                    failMsgs.add("第" + lineNo + "行：" + ex.getMsg());
                }
            } catch (RuntimeException ex) {
                failRows.add(new SysUserImportFailRow(StrUtil.nullToEmpty(row.getUserName()), ex.getMessage()));
                if (failMsgs.size() < 30) {
                    failMsgs.add("第" + lineNo + "行：" + ex.getMessage());
                }
            }
        }
        vo.setSuccess(ok);
        vo.setFailure(failRows.size());
        vo.setFailureMessages(failMsgs);
        if (!failRows.isEmpty()) {
            byte[] bytes = ExcelUtils.writeBytes("失败明细", SysUserImportFailRow.class, failRows);
            String key = IdUtil.fastSimpleUUID();
            importErrorCache.put(key, new CachedExport(bytes, System.currentTimeMillis() + IMPORT_ERROR_TTL_MS));
            vo.setErrorKey(key);
        }
        return vo;
    }

    private void processImportRow(SysUserImportExcelRow row, boolean updateSupport, Map<String, SysRole> roleByKey) {
        if (StrUtil.isBlank(row.getUserName())) {
            throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "登录名称不能为空");
        }
        if (StrUtil.isBlank(row.getNickName())) {
            throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "用户昵称不能为空");
        }
        List<Long> roleIds = resolveRoleIdsByKeys(row.getRoleKeys(), roleByKey);
        if (roleIds.isEmpty()) {
            throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "角色权限字符无效或为空");
        }
        assertDeptExists(row.getDeptId());
        assertEmailPhone(row.getEmail(), row.getPhonenumber());
        SysUser existing = userMapper.selectOne(Wrappers.<SysUser>lambdaQuery()
                .eq(SysUser::getUserName, row.getUserName().trim())
                .last("LIMIT 1"));
        if (existing == null) {
            if (StrUtil.isBlank(row.getPassword())) {
                throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "新增用户时密码不能为空");
            }
            SysUserCreateBo bo = new SysUserCreateBo();
            bo.setUserName(row.getUserName().trim());
            bo.setNickName(row.getNickName().trim());
            bo.setPassword(row.getPassword());
            bo.setDeptId(row.getDeptId());
            bo.setEmail(row.getEmail());
            bo.setPhonenumber(row.getPhonenumber());
            bo.setSex(StrUtil.blankToDefault(row.getSex(), "0"));
            bo.setStatus(StrUtil.blankToDefault(row.getStatus(), "0"));
            bo.setRemark(null);
            bo.setRoleIds(roleIds);
            create(bo);
            return;
        }
        if (!updateSupport) {
            throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "用户已存在且未开启更新支持");
        }
        if (existing.getUserId() != null && existing.getUserId() == ADMIN_USER_ID) {
            throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "内置超级管理员不可通过导入更新");
        }
        SysUserUpdateBo ub = new SysUserUpdateBo();
        ub.setUserId(existing.getUserId());
        ub.setNickName(row.getNickName().trim());
        ub.setDeptId(row.getDeptId());
        ub.setEmail(row.getEmail());
        ub.setPhonenumber(row.getPhonenumber());
        ub.setSex(StrUtil.blankToDefault(row.getSex(), "0"));
        ub.setStatus(StrUtil.blankToDefault(row.getStatus(), "0"));
        ub.setRemark(null);
        ub.setRoleIds(roleIds);
        if (StrUtil.isNotBlank(row.getPassword())) {
            ub.setPassword(row.getPassword());
        }
        update(ub);
    }

    private Map<String, SysRole> loadActiveRolesByKey() {
        List<SysRole> roles = roleMapper.selectList(Wrappers.<SysRole>lambdaQuery().eq(SysRole::getStatus, "0"));
        Map<String, SysRole> map = new HashMap<>();
        for (SysRole r : roles) {
            if (StrUtil.isNotBlank(r.getRoleKey())) {
                map.put(r.getRoleKey().trim(), r);
            }
        }
        return map;
    }

    private List<Long> resolveRoleIdsByKeys(String roleKeys, Map<String, SysRole> roleByKey) {
        if (StrUtil.isBlank(roleKeys)) {
            return List.of();
        }
        String[] parts = roleKeys.split(",");
        List<Long> ids = new ArrayList<>();
        for (String p : parts) {
            String k = p.trim();
            if (k.isEmpty()) {
                continue;
            }
            SysRole r = roleByKey.get(k);
            if (r != null) {
                ids.add(r.getRoleId());
            }
        }
        return ids;
    }

    @Override
    public void importTemplate(HttpServletResponse response) {
        ExcelUtils.exportExcel(Collections.emptyList(), "用户导入模板", SysUserImportExcelRow.class, response);
    }

    @Override
    public void importError(String errorKey, HttpServletResponse response) {
        if (StrUtil.isBlank(errorKey)) {
            throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "errorKey 不能为空");
        }
        CachedExport cached = importErrorCache.remove(errorKey);
        if (cached == null || System.currentTimeMillis() > cached.expireAtEpochMs) {
            throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "失败明细已过期或不存在");
        }
        try {
            ExcelUtils.setAttachmentResponseHeader(response, "user-import-error.xlsx");
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=UTF-8");
            response.getOutputStream().write(cached.bytes);
        } catch (Exception e) {
            throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "输出失败明细异常：" + e.getMessage());
        }
    }

    @Override
    public void export(SysUserQueryBo query, HttpServletResponse response) {
        LocalDateTime begin = parseBeginTime(query.getBeginTime());
        LocalDateTime end = parseEndTime(query.getEndTime());
        LambdaQueryWrapper<SysUser> w = buildUserQueryWrapper(query, begin, end).orderByAsc(SysUser::getUserId);
        List<SysUser> users = userMapper.selectList(w);
        Map<Long, String> roleNamesMap = loadRoleNamesByUserIds(users.stream().map(SysUser::getUserId).toList());
        Set<Long> deptIds = users.stream().map(SysUser::getDeptId).filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Long, String> deptNameMap = new HashMap<>();
        if (!deptIds.isEmpty()) {
            List<SysDept> depts = deptMapper.selectList(Wrappers.<SysDept>lambdaQuery().in(SysDept::getDeptId, deptIds));
            for (SysDept d : depts) {
                deptNameMap.put(d.getDeptId(), d.getDeptName());
            }
        }
        List<SysUserExcelRow> out = new ArrayList<>(users.size());
        for (SysUser u : users) {
            SysUserExcelRow er = new SysUserExcelRow();
            er.setUserId(u.getUserId());
            er.setUserName(u.getUserName());
            er.setNickName(u.getNickName());
            if (u.getDeptId() != null) {
                er.setDeptName(deptNameMap.getOrDefault(u.getDeptId(), ""));
            } else {
                er.setDeptName("");
            }
            er.setPhonenumber(u.getPhonenumber());
            er.setStatusLabel("0".equals(u.getStatus()) ? "正常" : "停用");
            er.setRoleNames(roleNamesMap.getOrDefault(u.getUserId(), ""));
            er.setCreateTime(u.getCreateTime());
            out.add(er);
        }
        ExcelUtils.exportExcel(out, "sys-user", SysUserExcelRow.class, response);
    }

    private void assertUserNameUnique(String userName, Long excludeUserId) {
        var q = Wrappers.<SysUser>lambdaQuery().eq(SysUser::getUserName, userName.trim());
        if (excludeUserId != null) {
            q.ne(SysUser::getUserId, excludeUserId);
        }
        if (userMapper.selectCount(q) > 0) {
            throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "登录账号已存在");
        }
    }

    private void assertDeptExists(Long deptId) {
        if (deptId == null) {
            throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "部门不能为空");
        }
        SysDept d = deptMapper.selectById(deptId);
        if (d == null) {
            throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "部门不存在或已删除");
        }
    }

    private void assertRolesValid(List<Long> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) {
            throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "至少选择一个角色");
        }
        for (Long id : roleIds) {
            if (id == null || id < 1) {
                continue;
            }
            SysRole r = roleMapper.selectById(id);
            if (r == null) {
                throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "角色不存在或已删除：id=" + id);
            }
        }
    }

    private void assertEmailPhone(String email, String phone) {
        if (StrUtil.isNotBlank(email) && !email.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "邮箱格式不正确");
        }
        if (StrUtil.isNotBlank(phone) && !phone.matches("^1\\d{10}$")) {
            throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "手机号须为11位且以1开头");
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
}
