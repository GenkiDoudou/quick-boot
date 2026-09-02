package io.github.genkidoudou.system.internal.service;

import io.github.genkidoudou.common.api.PageInfo;
import io.github.genkidoudou.common.api.PageRequest;
import io.github.genkidoudou.common.excel.listener.ExcelResult;
import io.github.genkidoudou.system.internal.vo.SysOauthClientImportResult;
import io.github.genkidoudou.system.internal.vo.SysOauthClientImportRow;
import io.github.genkidoudou.system.internal.vo.SysOauthClientVo;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

/**
 * OAuth 客户端管理（公开 API 仅 Vo）。
 */
public interface ISysOauthClientService {

  /**
   * 按业务 clientId 查询（登录 Basic 鉴权；含 secret）。
   *
   * @param clientId 客户端业务标识
   * @return Vo（含 secret）；不存在则 null
   */
  SysOauthClientVo findByClientId(String clientId);

  /**
   * 分页（结果不含 secret）。
   *
   * @param pageRequest 分页参数
   * @return 分页结果
   */
  PageInfo<SysOauthClientVo> page(PageRequest<SysOauthClientVo> pageRequest);

  /**
   * 详情（含明文 secret，供管理端复制凭证）。
   *
   * @param id 主键
   * @return 含 secret 的 Vo
   */
  SysOauthClientVo getDetail(Long id);

  /**
   * 新增；服务端生成 secret。
   *
   * @param vo 可写字段（不含 clientSecret、id）
   * @return 新建记录主键 id
   */
  Long add(SysOauthClientVo vo);

  /**
   * 修改；不变更 clientSecret / clientId。
   *
   * @param vo 含 id 与可写字段
   * @return 是否成功
   */
  boolean update(SysOauthClientVo vo);

  /**
   * 按主键删除（逻辑删）。
   *
   * @param ids 主键列表
   */
  void remove(Collection<Long> ids);

  /**
   * 同步导出数据（不含 secret）。
   *
   * @param sysOauthClientVo 导出条件
   * @return 导出行
   */
  List<SysOauthClientVo> export(SysOauthClientVo sysOauthClientVo);

  /**
   * 同步导入 xlsx（无 secret 列；新增生成 secret；可选更新保留 secret）。
   *
   * @param file          上传文件
   * @param updateSupport 是否更新已存在 clientId
   * @return 导入统计（可含失败明细 base64）
   */
  ExcelResult<SysOauthClientImportRow> importExcel(MultipartFile file, boolean updateSupport)
    throws IOException;
}
