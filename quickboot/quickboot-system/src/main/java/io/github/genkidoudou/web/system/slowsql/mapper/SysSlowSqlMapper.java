package io.github.genkidoudou.web.system.slowsql.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.github.genkidoudou.web.system.slowsql.domain.SysSlowSql;
import org.apache.ibatis.annotations.Mapper;

/**
 * 慢 SQL 记录 Mapper。
 */
@Mapper
public interface SysSlowSqlMapper extends BaseMapper<SysSlowSql> {
}
