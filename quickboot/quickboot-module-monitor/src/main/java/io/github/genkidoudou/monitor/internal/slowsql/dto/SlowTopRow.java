package io.github.genkidoudou.monitor.internal.slowsql.dto;

import lombok.Data;

/**
 * 最慢 SQL 聚合行，用于监控概览 Top N 展示。
 */
@Data
public class SlowTopRow {
  /** 慢 SQL 主键 */
  private Long slowId;
  /** 来源：BUSINESS / JIMU / SYSTEM */
  private String sqlSource;
  /** SQL 操作类型 */
  private String sqlType;
  /** MyBatis Mapper 标识 */
  private String mapperId;
  /** SQL 文本摘要（截断） */
  private String sqlText;
  /** 耗时毫秒 */
  private Long costTime;
}
