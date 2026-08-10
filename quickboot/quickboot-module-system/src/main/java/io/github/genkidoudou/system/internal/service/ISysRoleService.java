package io.github.genkidoudou.system.internal.service;

import io.github.genkidoudou.common.api.PageInfo;
import io.github.genkidoudou.common.api.PageRequest;
import io.github.genkidoudou.common.excel.listener.ExcelResult;
import io.github.genkidoudou.system.internal.vo.RoleMenuTreeVo;
import io.github.genkidoudou.system.internal.vo.SysRoleImportRow;
import io.github.genkidoudou.system.internal.vo.SysRoleUserVo;
import io.github.genkidoudou.system.internal.vo.SysRoleVo;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

/**
 * 角色管理。
 */
public interface ISysRoleService {

  /**
   * 角色分页。
   *
   * @param pageRequest 分页参数
   * @return 分页结果
   */
  PageInfo<SysRoleVo> page(PageRequest<SysRoleVo> pageRequest);

  /**
   * 角色详情。
   *
   * @param roleId 主键
   * @return Vo
   */
  SysRoleVo getDetail(Long roleId);

  /**
   * 新增角色。
   *
   * @param vo 可写字段（不含 roleId）
   * @return 新建主键 roleId
   */
  Long add(SysRoleVo vo);

  /**
   * 修改角色。
   *
   * @param vo 含 roleId 与可写字段
   * @return 是否成功
   */
  boolean update(SysRoleVo vo);

  /**
   * 按主键删除（逻辑删）；空集合静默返回。超管与已绑用户角色不可删。
   *
   * @param roleIds 主键集合
   */
  void remove(Collection<Long> roleIds);

  /**
   * 修改状态。
   *
   * @param roleId 主键
   * @param status {@code 0}/{@code 1}
   */
  void changeStatus(Long roleId, String status);

  /**
   * 全量保存角色菜单。
   *
   * @param roleId  主键
   * @param menuIds 菜单 id 列表
   */
  void saveMenus(Long roleId, List<Long> menuIds);

  /**
   * 角色菜单树与已勾选 keys。
   *
   * @param roleId 主键
   * @return 树与勾选
   */
  RoleMenuTreeVo menuTree(Long roleId);

  /**
   * 已分配用户分页。
   *
   * @param pageRequest 分页
   * @param roleId      角色 id
   * @return 分页
   */
  PageInfo<SysRoleUserVo> allocatedPage(PageRequest<SysRoleUserVo> pageRequest, Long roleId);

  /**
   * 未分配用户分页。
   *
   * @param pageRequest 分页
   * @param roleId      角色 id
   * @return 分页
   */
  PageInfo<SysRoleUserVo> unallocatedPage(PageRequest<SysRoleUserVo> pageRequest, Long roleId);

  /**
   * 授权用户。
   *
   * @param roleId  角色 id
   * @param userIds 用户 id
   */
  void grantUsers(Long roleId, List<String> userIds);

  /**
   * 取消用户授权。
   *
   * @param roleId  角色 id
   * @param userIds 用户 id
   */
  void cancelUsers(Long roleId, List<String> userIds);

  /**
   * 同步导出。有 ids 则仅导出勾选；否则按搜索条件。
   *
   * @param query 导出条件
   * @return 导出行
   */
  List<SysRoleVo> export(SysRoleVo query);

  /**
   * 同步导入（按 roleKey 判重）。
   *
   * @param file          上传文件
   * @param updateSupport 是否更新已存在
   * @return 导入统计
   */
  ExcelResult<SysRoleImportRow> importExcel(MultipartFile file, boolean updateSupport) throws IOException;
}
