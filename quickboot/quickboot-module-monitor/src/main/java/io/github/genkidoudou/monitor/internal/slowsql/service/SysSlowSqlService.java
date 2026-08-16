package io.github.genkidoudou.monitor.internal.slowsql.service;

import io.github.genkidoudou.common.api.PageInfo;
import io.github.genkidoudou.monitor.internal.slowsql.dto.SysSlowSqlQueryBo;
import io.github.genkidoudou.monitor.internal.slowsql.dto.SysSlowSqlVo;
import jakarta.servlet.http.HttpServletResponse;

import java.util.List;

/**
 * 慢 SQL 查询与维护服务。
 */
public interface SysSlowSqlService {

  /**
   * 分页查询慢 SQL 记录。
   *
   * @param query 筛选与分页条件
   * @return 慢 SQL 分页列表
   */
  PageInfo<SysSlowSqlVo> page(SysSlowSqlQueryBo query);

  /**
   * 按主键查询慢 SQL 详情。
   *
   * @param slowId 主键
   * @return 慢 SQL VO；不存在时抛业务异常
   */
  SysSlowSqlVo getById(Long slowId);

  /**
   * 按条件导出慢 SQL 为 Excel 文件流。
   *
   * @param query    筛选条件
   * @param response HTTP 响应
   */
  void export(SysSlowSqlQueryBo query, HttpServletResponse response);

  /**
   * 批量删除慢 SQL 记录。
   *
   * @param slowIds 主键列表；空列表时抛业务异常
   */
  void removeBatch(List<Long> slowIds);

  /** 清空全部慢 SQL 记录。 */
  void cleanAll();
}
