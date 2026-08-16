package io.github.genkidoudou.system.internal.service;

import io.github.genkidoudou.common.api.PageInfo;
import io.github.genkidoudou.common.api.PageRequest;
import io.github.genkidoudou.common.excel.listener.ExcelResult;
import io.github.genkidoudou.system.internal.entity.SysUser;
import io.github.genkidoudou.system.internal.vo.SysUserAuthRoleVo;
import io.github.genkidoudou.system.internal.vo.SysUserImportRow;
import io.github.genkidoudou.system.internal.vo.SysUserVo;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

/**
 * 用户管理。
 */
public interface ISysUserService {

  /**
   * 按登录账号查询（认证、导入判重用）。
   *
   * @param username 用户账号
   * @return 用户实体；不存在或参数为空则 {@code null}
   */
  SysUser findByUserName(String username);

  /**
   * 按主键查询。
   *
   * @param userId 用户主键
   * @return 用户实体；不存在或参数为空则 {@code null}
   */
  SysUser findByUserId(Long userId);

  /**
   * 用户分页；结果不含密码，并填充部门名与角色信息。
   *
   * @param pageRequest 分页与筛选条件
   * @return 分页结果
   */
  PageInfo<SysUserVo> page(PageRequest<SysUserVo> pageRequest);

  /**
   * 用户详情（不含密码）。
   *
   * @param userId 用户主键
   * @return 详情 Vo
   */
  SysUserVo getDetail(Long userId);

  /**
   * 新增用户并绑定角色；未传密码时使用默认密码。
   *
   * @param vo 可写字段（不含 userId）
   * @return 新建主键 userId
   */
  Long add(SysUserVo vo);

  /**
   * 修改用户；可更新部门、角色与可选密码。
   *
   * @param vo 含 userId 与可写字段
   * @return 是否成功
   */
  boolean update(SysUserVo vo);

  /**
   * 按主键批量删除；空集合静默返回。超级管理员不可删，并清理权限缓存。
   *
   * @param userIds 主键集合
   */
  void remove(Collection<Long> userIds);

  /**
   * 修改账号状态。
   *
   * @param userId 用户主键
   * @param status {@code 0} 正常 / {@code 1} 停用
   */
  void changeStatus(Long userId, String status);

  /**
   * 重置登录密码。
   *
   * @param userId   用户主键
   * @param password 新密码明文
   */
  void resetPwd(Long userId, String password);

  /**
   * 查询用户授权角色页数据（全部角色 + 已勾选 roleIds）。
   *
   * @param userId 用户主键
   * @return 授权页 Vo
   */
  SysUserAuthRoleVo authRole(Long userId);

  /**
   * 全量保存用户角色绑定。
   *
   * @param userId  用户主键
   * @param roleIds 角色 id 列表
   */
  void saveAuthRole(Long userId, List<Long> roleIds);

  /**
   * 同步导出。有 ids 则仅导出勾选；否则按搜索条件。
   *
   * @param query 导出条件
   * @return 导出行
   */
  List<SysUserVo> export(SysUserVo query);

  /**
   * 同步导入（按 userName 判重；可按部门名称解析 deptId）。
   *
   * @param file          上传文件
   * @param updateSupport 是否更新已存在账号
   * @return 导入统计
   */
  ExcelResult<SysUserImportRow> importExcel(MultipartFile file, boolean updateSupport) throws IOException;
}
