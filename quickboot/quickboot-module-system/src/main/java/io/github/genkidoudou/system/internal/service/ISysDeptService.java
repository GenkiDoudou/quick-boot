package io.github.genkidoudou.system.internal.service;
import io.github.genkidoudou.common.excel.listener.ExcelResult;
import io.github.genkidoudou.system.internal.vo.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.*;

/**
 * 部门管理。
 */
public interface ISysDeptService {

  /**
   * 部门树列表（可按名称、状态过滤）。
   *
   * @param name   部门名称模糊，可空
   * @param status 状态，可空
   * @return 树形 Vo 列表
   */
  List<SysDeptVo> list(String name, String status);

  /**
   * 下拉树（仅启用部门）。
   *
   * @return 树形 Vo 列表
   */
  List<SysDeptVo> treeSelect();

  /**
   * 部门详情。
   *
   * @param id 部门主键
   * @return 详情 Vo
   */
  SysDeptVo getDetail(Long id);

  /**
   * 新增部门。
   *
   * @param vo 可写字段（不含 deptId）
   * @return 新建主键 deptId
   */
  Long add(SysDeptVo vo);

  /**
   * 修改部门。
   *
   * @param vo 含 deptId 与可写字段
   * @return 是否成功
   */
  boolean update(SysDeptVo vo);

  /**
   * 按主键删除；存在子部门或绑定用户则拒绝。
   *
   * @param ids 主键集合
   */
  void remove(Collection<Long> ids);

  /**
   * 同步导出。有 ids 则仅导出勾选；否则全量。
   *
   * @param query 导出条件
   * @return 导出行
   */
  List<SysDeptVo> export(SysDeptVo query);

  /**
   * 同步导入部门。
   *
   * @param file          上传文件
   * @param updateSupport 是否更新已存在（当前实现逐行新增）
   * @return 导入统计
   */
  ExcelResult<SysDeptImportRow> importExcel(MultipartFile file, boolean updateSupport) throws IOException;
}
