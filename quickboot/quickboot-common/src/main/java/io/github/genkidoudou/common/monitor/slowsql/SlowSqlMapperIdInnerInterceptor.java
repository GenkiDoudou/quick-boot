package io.github.genkidoudou.common.monitor.slowsql;

import com.baomidou.mybatisplus.extension.plugins.inner.InnerInterceptor;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;

/**
 * 仅在当前线程标记 MyBatis {@code mapperId}，由 Druid JDBC 过滤器统一落库。
 */
public class SlowSqlMapperIdInnerInterceptor implements InnerInterceptor {

    @Override
    @SuppressWarnings({"rawtypes", "deprecation"})
    public void beforeQuery(
            Executor executor,
            MappedStatement ms,
            Object parameter,
            RowBounds rowBounds,
            ResultHandler resultHandler,
            BoundSql boundSql) {
        SlowSqlMapperContext.set(ms.getId());
    }

    @Override
    public void beforeUpdate(Executor executor, MappedStatement ms, Object parameter) {
        SlowSqlMapperContext.set(ms.getId());
    }
}
