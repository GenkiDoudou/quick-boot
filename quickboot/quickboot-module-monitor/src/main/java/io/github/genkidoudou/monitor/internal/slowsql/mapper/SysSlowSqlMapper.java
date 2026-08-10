package io.github.genkidoudou.monitor.internal.slowsql.mapper;

import io.github.genkidoudou.common.mybatisplus.BaseBaseMapper;
import io.github.genkidoudou.monitor.internal.slowsql.entity.SysSlowSql;
import org.apache.ibatis.annotations.Mapper;

/**
 * 慢 SQL 记录 Mapper。
 */
@Mapper
public interface SysSlowSqlMapper extends BaseBaseMapper<SysSlowSql> {
}
