package io.github.genkidoudou.web.system.user.service;

import io.github.genkidoudou.common.api.PageInfo;
import io.github.genkidoudou.web.system.user.dto.SysUserCreateBo;
import io.github.genkidoudou.web.system.user.dto.SysUserDetailVo;
import io.github.genkidoudou.web.system.user.dto.SysUserQueryBo;
import io.github.genkidoudou.web.system.user.dto.SysUserUpdateBo;
import io.github.genkidoudou.web.system.user.dto.SysUserVo;
import io.github.genkidoudou.web.system.user.dto.UserAuthRoleRequest;
import io.github.genkidoudou.web.system.user.dto.UserAuthRoleVo;
import io.github.genkidoudou.web.system.user.dto.UserChangeStatusRequest;
import io.github.genkidoudou.web.system.user.dto.UserImportResultVo;
import io.github.genkidoudou.web.system.user.dto.UserResetPwdRequest;
import io.github.genkidoudou.web.system.user.datascope.DataPermission;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.multipart.MultipartFile;

/**
 * 系统用户管理：分页、详情、增删改、状态、密码、角色分配、导入导出。
 */
public interface SysUserService {

    /**
     * 分页查询用户列表。
     *
     * @param query 筛选与分页参数
     * @return 分页结果
     */
    @DataPermission(tables = {"sys_user"})
    PageInfo<SysUserVo> page(SysUserQueryBo query);

    /**
     * 用户详情（含角色 id）。
     *
     * @param userId 用户主键
     * @return 详情；不存在时返回 {@code null}
     */
    @DataPermission(tables = {"sys_user"})
    SysUserDetailVo get(Long userId);

    /**
     * 新增用户。
     *
     * @param bo 入参
     */
    @DataPermission(tables = {"sys_user"})
    void create(SysUserCreateBo bo);

    /**
     * 修改用户。
     *
     * @param bo 入参
     */
    @DataPermission(tables = {"sys_user"})
    void update(SysUserUpdateBo bo);

    /**
     * 批量删除用户（逻辑删除）。
     *
     * @param userIds 用户主键列表
     */
    void remove(java.util.List<Long> userIds);

    /**
     * 修改用户状态。
     *
     * @param req 请求体
     */
    @DataPermission(tables = {"sys_user"})
    void changeStatus(UserChangeStatusRequest req);

    /**
     * 重置用户密码。
     *
     * @param req 请求体
     */
    @DataPermission(tables = {"sys_user"})
    void resetPwd(UserResetPwdRequest req);

    /**
     * 分配角色页数据。
     *
     * @param userId 用户主键
     * @return 可选角色与已勾选
     */
    @DataPermission(tables = {"sys_user"})
    UserAuthRoleVo authRoleInfo(Long userId);

    /**
     * 保存用户角色分配。
     *
     * @param req 请求体
     */
    @DataPermission(tables = {"sys_user"})
    void saveAuthRole(UserAuthRoleRequest req);

    /**
     * 导入用户 Excel。
     *
     * @param file          上传文件
     * @param updateSupport 是否按登录名更新已存在用户
     * @return 导入统计与可选错误下载键
     */
    @DataPermission(tables = {"sys_user"})
    UserImportResultVo importData(MultipartFile file, boolean updateSupport);

    /**
     * 下载导入模板。
     *
     * @param response HTTP 响应
     */
    void importTemplate(HttpServletResponse response);

    /**
     * 下载导入失败明细。
     *
     * @param errorKey 导入结果返回的短时键
     * @param response HTTP 响应
     */
    void importError(String errorKey, HttpServletResponse response);

    /**
     * 按筛选条件导出用户 xlsx。
     *
     * @param query    与列表一致的筛选
     * @param response HTTP 响应
     */
    @DataPermission(tables = {"sys_user"})
    void export(SysUserQueryBo query, HttpServletResponse response);
}
