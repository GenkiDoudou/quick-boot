package io.github.genkidoudou.monitor.internal.slowsql.support;

import io.github.genkidoudou.common.monitor.slowsql.SlowSqlCapturedEvent;
import io.github.genkidoudou.monitor.internal.slowsql.entity.SysSlowSql;
import io.github.genkidoudou.monitor.internal.slowsql.mapper.SysSlowSqlMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 慢 SQL 落库（同步/异步监听器复用）。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SlowSqlPersistSupport {

    private final SysSlowSqlMapper slowSqlMapper;
    private final SlowSqlAssembler slowSqlAssembler;

    public void persist(SlowSqlCapturedEvent event) {
        try {
            SysSlowSql row = slowSqlAssembler.assemble(event.getPayload());
            slowSqlMapper.insert(row);
        } catch (Exception ex) {
            log.error("persist slow sql failed", ex);
        }
    }
}
