package io.github.genkidoudou.web.config.slowsql;

import com.alibaba.druid.filter.FilterAdapter;
import com.alibaba.druid.filter.FilterChain;
import com.alibaba.druid.proxy.jdbc.PreparedStatementProxy;
import com.alibaba.druid.proxy.jdbc.ResultSetProxy;
import com.alibaba.druid.proxy.jdbc.StatementProxy;
import io.github.genkidoudou.common.monitor.slowsql.SlowSqlCaptureSupport;
import io.github.genkidoudou.common.monitor.slowsql.SlowSqlMapperContext;

import java.sql.SQLException;

/**
 * Druid JDBC 过滤器：拦截 Statement 与 PreparedStatement（MyBatis / 积木均走后者）。
 */
public class SlowSqlDruidFilter extends FilterAdapter {

    private final SlowSqlCaptureSupport captureSupport;

    public SlowSqlDruidFilter(SlowSqlCaptureSupport captureSupport) {
        this.captureSupport = captureSupport;
    }

    @Override
    public ResultSetProxy statement_executeQuery(FilterChain chain, StatementProxy statement, String sql)
        throws SQLException {
        return timed(chain, () -> chain.statement_executeQuery(statement, sql), statement, sql);
    }

    @Override
    public int statement_executeUpdate(FilterChain chain, StatementProxy statement, String sql) throws SQLException {
        return timed(chain, () -> chain.statement_executeUpdate(statement, sql), statement, sql);
    }

    @Override
    public boolean statement_execute(FilterChain chain, StatementProxy statement, String sql) throws SQLException {
        return timed(chain, () -> chain.statement_execute(statement, sql), statement, sql);
    }

    @Override
    public int[] statement_executeBatch(FilterChain chain, StatementProxy statement) throws SQLException {
        return timed(chain, () -> chain.statement_executeBatch(statement), statement, statement.getLastExecuteSql());
    }

    @Override
    public boolean preparedStatement_execute(FilterChain chain, PreparedStatementProxy statement) throws SQLException {
        return timed(chain, () -> chain.preparedStatement_execute(statement), statement, statement.getSql());
    }

    @Override
    public ResultSetProxy preparedStatement_executeQuery(FilterChain chain, PreparedStatementProxy statement)
        throws SQLException {
        return timed(chain, () -> chain.preparedStatement_executeQuery(statement), statement, statement.getSql());
    }

    @Override
    public int preparedStatement_executeUpdate(FilterChain chain, PreparedStatementProxy statement)
        throws SQLException {
        return timed(chain, () -> chain.preparedStatement_executeUpdate(statement), statement, statement.getSql());
    }

    @FunctionalInterface
    private interface SqlCallable<T> {
        T call() throws SQLException;
    }

    private <T> T timed(FilterChain chain, SqlCallable<T> callable, StatementProxy statement, String sql)
        throws SQLException {
        long start = System.nanoTime();
        try {
            return callable.call();
        } finally {
            onAfter(statement, sql, System.nanoTime() - start);
        }
    }

    private void onAfter(StatementProxy statement, String sql, long nanos) {
        try {
            long costMs = nanos / 1_000_000L;
            String executeSql = SlowSqlExecutableSqlResolver.resolve(statement, sql);
            captureSupport.captureIfSlow(executeSql, costMs);
        } catch (Throwable ignored) {
            // 含 IDE 增量编译残留的 java.lang.Error，不得影响 JDBC / Flyway 启动
            SlowSqlMapperContext.clear();
        }
    }
}
