package io.github.genkidoudou.web.system.slowsql.support;

import cn.hutool.core.bean.BeanUtil;
import io.github.genkidoudou.common.monitor.slowsql.SlowSqlCapturePayload;
import io.github.genkidoudou.web.system.slowsql.domain.SysSlowSql;
import org.springframework.stereotype.Component;

/**
 * 将慢 SQL 采集载荷组装为 {@link SysSlowSql} 实体。
 */
@Component
public class SlowSqlAssembler {

    /**
     * @param payload JDBC 采集事件载荷
     * @return 待插入实体
     */
    public SysSlowSql assemble(SlowSqlCapturePayload payload) {
        SysSlowSql row = BeanUtil.copyProperties(payload, SysSlowSql.class);
        row.setCostTime(payload.getCostTimeMs());
        return row;
    }
}
