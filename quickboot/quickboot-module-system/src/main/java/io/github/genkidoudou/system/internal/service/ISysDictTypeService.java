package io.github.genkidoudou.system.internal.service;

import io.github.genkidoudou.common.api.PageInfo;
import io.github.genkidoudou.common.api.PageRequest;
import io.github.genkidoudou.common.excel.listener.ExcelResult;
import io.github.genkidoudou.system.internal.vo.SysDictTypeImportRow;
import io.github.genkidoudou.system.internal.vo.SysDictTypeVo;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

/**
 * 字典类型管理。
 */
public interface ISysDictTypeService {

  /**
   * 字典类型分页。
   *
   * @param pageRequest 分页与筛选
   * @return 分页结果
   */
  PageInfo<SysDictTypeVo> page(PageRequest<SysDictTypeVo> pageRequest);

  /**
   * 字典类型详情。
   *
   * @param dictId 字典类型主键
   * @return 详情 Vo
   */
  SysDictTypeVo getDetail(Long dictId);

  /**
   * 新增字典类型；dictType 编码不可重复。
   *
   * @param vo 可写字段
   * @return 新建主键 dictId
   */
  Long add(SysDictTypeVo vo);

  /**
   * 修改字典类型。
   *
   * @param vo 含 dictId 与可写字段
   * @return 是否成功
   */
  boolean update(SysDictTypeVo vo);

  /**
   * 删除字典类型；仍有关联字典项则拒绝。
   *
   * @param ids 主键集合
   */
  void remove(Collection<Long> ids);

  /**
   * 刷新全部字典缓存。
   */
  void refreshAll();

  /**
   * 刷新指定字典类型缓存。
   *
   * @param dictType 字典类型编码
   */
  void refresh(String dictType);

  /**
   * 同步导出。
   *
   * @param query 导出条件或 ids
   * @return 导出行
   */
  List<SysDictTypeVo> export(SysDictTypeVo query);

  /**
   * 同步导入（按 dictType 判重）。
   *
   * @param file          上传文件
   * @param updateSupport 是否更新已存在
   * @return 导入统计
   */
  ExcelResult<SysDictTypeImportRow> importExcel(MultipartFile file, boolean updateSupport) throws IOException;
}
