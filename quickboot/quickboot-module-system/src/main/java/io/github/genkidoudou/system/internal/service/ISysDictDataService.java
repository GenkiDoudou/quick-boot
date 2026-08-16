package io.github.genkidoudou.system.internal.service;

import io.github.genkidoudou.common.api.PageInfo;
import io.github.genkidoudou.common.api.PageRequest;
import io.github.genkidoudou.common.excel.listener.ExcelResult;
import io.github.genkidoudou.system.internal.vo.SysDictDataImportRow;
import io.github.genkidoudou.system.internal.vo.SysDictDataVo;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

/**
 * 字典数据（字典项）管理。
 */
public interface ISysDictDataService {

  /**
   * 字典项分页。
   *
   * @param pageRequest 分页与筛选
   * @return 分页结果
   */
  PageInfo<SysDictDataVo> page(PageRequest<SysDictDataVo> pageRequest);

  /**
   * 字典项详情。
   *
   * @param dictCode 字典项主键
   * @return 详情 Vo
   */
  SysDictDataVo getDetail(Long dictCode);

  /**
   * 新增字典项；同一 dictType 下 dictValue 不可重复。
   *
   * @param vo 可写字段
   * @return 新建主键 dictCode
   */
  Long add(SysDictDataVo vo);

  /**
   * 修改字典项。
   *
   * @param vo 含 dictCode 与可写字段
   * @return 是否成功
   */
  boolean update(SysDictDataVo vo);

  /**
   * 批量删除字典项；空集合静默返回。
   *
   * @param ids 主键集合
   */
  void remove(Collection<Long> ids);

  /**
   * 按字典类型查询启用中的字典项（前端下拉/标签用）。
   *
   * @param dictType 字典类型编码
   * @return 字典项列表
   */
  List<SysDictDataVo> listByType(String dictType);

  /**
   * 同步导出。
   *
   * @param query 导出条件或 ids
   * @return 导出行
   */
  List<SysDictDataVo> export(SysDictDataVo query);

  /**
   * 同步导入（按 dictType + dictValue 判重）。
   *
   * @param file          上传文件
   * @param updateSupport 是否更新已存在
   * @return 导入统计
   */
  ExcelResult<SysDictDataImportRow> importExcel(MultipartFile file, boolean updateSupport) throws IOException;
}
