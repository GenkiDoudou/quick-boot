package io.github.genkidoudou.system.internal.service;

import io.github.genkidoudou.common.api.PageInfo;
import io.github.genkidoudou.common.api.PageRequest;
import io.github.genkidoudou.common.excel.listener.ExcelResult;
import io.github.genkidoudou.system.internal.vo.SysConfigImportRow;
import io.github.genkidoudou.system.internal.vo.SysConfigVo;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

/**
 * 系统参数配置管理。
 */
public interface ISysConfigService {

  /**
   * 参数分页。
   *
   * @param pageRequest 分页与筛选
   * @return 分页结果
   */
  PageInfo<SysConfigVo> page(PageRequest<SysConfigVo> pageRequest);

  /**
   * 参数详情。
   *
   * @param configId 参数主键
   * @return 详情 Vo
   */
  SysConfigVo getDetail(Long configId);

  /**
   * 新增参数；configKey 须唯一且符合命名规则。
   *
   * @param vo 可写字段
   * @return 新建主键 configId
   */
  Long add(SysConfigVo vo);

  /**
   * 修改参数；系统内置参数不可变更键名与类型。
   *
   * @param vo 含 configId 与可写字段
   * @return 是否成功
   */
  boolean update(SysConfigVo vo);

  /**
   * 批量删除；系统内置参数不可删。
   *
   * @param ids 主键集合
   */
  void remove(Collection<Long> ids);

  /**
   * 按 configKey 读取参数值（带缓存）。
   *
   * @param configKey 参数键名
   * @return 参数值；不存在则 {@code null}
   */
  String getConfigValueByKey(String configKey);

  /**
   * 刷新全部参数缓存。
   */
  void refreshCache();

  /**
   * 同步导出。
   *
   * @param request 导出条件或 ids
   * @return 导出行
   */
  List<SysConfigVo> export(SysConfigVo request);

  /**
   * 同步导入（按 configKey 判重）。
   *
   * @param file          上传文件
   * @param updateSupport 是否更新已存在
   * @return 导入统计
   */
  ExcelResult<SysConfigImportRow> importExcel(MultipartFile file, boolean updateSupport) throws IOException;
}
