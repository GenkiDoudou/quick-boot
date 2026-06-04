package io.github.genkidoudou.web.system.role.service;

import io.github.genkidoudou.common.api.PageInfo;
import io.github.genkidoudou.common.excel.ExcelImportResult;
import io.github.genkidoudou.web.system.role.dto.RoleCancelUserRequest;
import io.github.genkidoudou.web.system.role.dto.RoleChangeStatusRequest;
import io.github.genkidoudou.web.system.role.dto.RoleDataScopeRequest;
import io.github.genkidoudou.web.system.role.dto.RoleGrantUsersRequest;
import io.github.genkidoudou.web.system.role.dto.RoleMenuRequest;
import io.github.genkidoudou.web.system.role.dto.SysRoleAuthUserQueryBo;
import io.github.genkidoudou.web.system.role.dto.SysRoleBo;
import io.github.genkidoudou.web.system.role.dto.SysRoleQueryBo;
import io.github.genkidoudou.web.system.role.dto.SysRoleUserVo;
import io.github.genkidoudou.web.system.role.dto.SysRoleVo;
import io.github.genkidoudou.web.system.user.datascope.DataPermission;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/**
 * 角色管理：CRUD、数据权限、菜单、用户分配与导出。
 */
public interface SysRoleService {

    /**
     * 分页查询角色。
     *
     * @param query 筛选与分页条件
     * @return 分页结果
     */
    PageInfo<SysRoleVo> page(SysRoleQueryBo query);

    /**
     * 按主键查询角色详情（含自定义部门 id 列表）。
     *
     * @param roleId 角色主键
     * @return 视图对象；不存在或已删除时返回 {@code null}
     */
    SysRoleVo getById(Long roleId);

    /**
     * 新增角色。
     *
     * @param req 入参
     */
    void add(SysRoleBo req);

    /**
     * 修改角色基础信息。
     *
     * @param req 入参
     */
    void update(SysRoleBo req);

    /**
     * 批量逻辑删除角色。
     *
     * @param roleIds 角色主键列表
     */
    void removeBatch(List<Long> roleIds);

    /**
     * 修改角色状态。
     *
     * @param req 入参
     */
    void changeStatus(RoleChangeStatusRequest req);

    /**
     * 保存数据权限及自定义部门。
     *
     * @param req 入参
     */
    void updateDataScope(RoleDataScopeRequest req);

    /**
     * 全量保存角色菜单关联。
     *
     * @param req 入参
     */
    void updateMenus(RoleMenuRequest req);

    /**
     * 分页查询已分配该角色的用户。
     *
     * @param query 条件
     * @return 分页结果
     */
    @DataPermission(tables = {"sys_user"})
    PageInfo<SysRoleUserVo> pageAllocatedUsers(SysRoleAuthUserQueryBo query);

    /**
     * 分页查询未分配该角色的用户。
     *
     * @param query 条件
     * @return 分页结果
     */
    @DataPermission(tables = {"sys_user"})
    PageInfo<SysRoleUserVo> pageUnallocatedUsers(SysRoleAuthUserQueryBo query);

    /**
     * 批量授权用户到角色。
     *
     * @param req 入参
     */
    @DataPermission(tables = {"sys_user"})
    void grantUsers(RoleGrantUsersRequest req);

    /**
     * 取消单个用户的角色。
     *
     * @param req 入参
     */
    void cancelUser(RoleCancelUserRequest req);

    /**
     * 批量取消用户的角色。
     *
     * @param req 入参
     */
    void cancelUsers(RoleGrantUsersRequest req);

    /**
     * 按筛选条件导出角色 Excel。
     *
     * @param query    与列表一致的筛选（忽略分页）
     * @param response HTTP 响应
     */
    void export(SysRoleQueryBo query, HttpServletResponse response);

    /** 按筛选条件统计可导出行数。 */
    @DataPermission(tables = {"sys_role"})
    long countExportRows(SysRoleQueryBo query);

    /** 生成导出 Excel 字节（最多 {@code maxRows} 行）。 */
    @DataPermission(tables = {"sys_role"})
    byte[] exportExcelBytes(SysRoleQueryBo query, int maxRows);

    /**
     * 从 Excel 批量导入角色。
     *
     * @param file          上传文件
     * @param updateSupport 是否按权限字符更新已存在数据
     * @return 导入统计与失败明细
     */
    ExcelImportResult importData(MultipartFile file, boolean updateSupport) throws IOException;

    /**
     * 导入单行角色（供平台导入编排调用）。
     *
     * @throws io.github.genkidoudou.common.excel.exception.ExcelDataCheckException 业务校验失败
     */
    void importRoleExcelRow(io.github.genkidoudou.web.system.role.dto.SysRoleImportExcelRow row, boolean updateSupport);
}
