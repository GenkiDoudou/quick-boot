package io.github.genkidoudou.web.system.user.datascope;

import com.baomidou.mybatisplus.core.toolkit.PluginUtils;
import com.baomidou.mybatisplus.extension.plugins.inner.InnerInterceptor;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.Join;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.WithItem;
import net.sf.jsqlparser.schema.Table;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.springframework.stereotype.Component;

/**
 * 参照旧 quick-boot {@code DataPermissionInterceptor}：仅在 {@link DataPermissionContext} 存在时
 * 对 SELECT 命中表追加数据权限条件（MyBatis-Plus {@link InnerInterceptor}）。
 */
@Component
public class DataPermissionInnerInterceptor implements InnerInterceptor {

    private final DataPermissionRuleEngine ruleEngine;

    public DataPermissionInnerInterceptor(DataPermissionRuleEngine ruleEngine) {
        this.ruleEngine = ruleEngine;
    }

    @Override
    @SuppressWarnings({"rawtypes", "deprecation"})
    public void beforeQuery(
            Executor executor,
            MappedStatement ms,
            Object parameter,
            RowBounds rowBounds,
            ResultHandler resultHandler,
            BoundSql boundSql) {
        if (ms.getSqlCommandType() != SqlCommandType.SELECT) {
            return;
        }
        DataPermission ann = DataPermissionContext.get();
        if (ann == null || ann.ignore()) {
            return;
        }
        PluginUtils.MPBoundSql mpBs = PluginUtils.mpBoundSql(boundSql);
        String originalSql = mpBs.sql();
        Statement stmt;
        try {
            stmt = CCJSqlParserUtil.parse(originalSql);
        } catch (Exception e) {
            return;
        }
        if (stmt instanceof Select select) {
            processSelect(select, ann);
            mpBs.sql(stmt.toString());
        }
    }

    private void processSelect(Select select, DataPermission ann) {
        if (select.getSelectBody() instanceof PlainSelect ps) {
            handlePlainSelect(ps, ann);
        }
        if (select.getWithItemsList() != null) {
            for (WithItem w : select.getWithItemsList()) {
                processSelect(w.getSelect(), ann);
            }
        }
    }

    private void handlePlainSelect(PlainSelect ps, DataPermission ann) {
        if (ps.getFromItem() instanceof Table table) {
            handleTable(ps, table, ann);
        }
        if (ps.getJoins() != null) {
            for (Join j : ps.getJoins()) {
                if (j.getRightItem() instanceof Table table) {
                    handleTable(ps, table, ann);
                }
            }
        }
    }

    private void handleTable(PlainSelect ps, Table table, DataPermission ann) {
        if (!TableMatchUtil.match(table.getName(), ann.tables())) {
            return;
        }
        Expression expr = ruleEngine.build(table, ann);
        if (expr == null) {
            return;
        }
        Expression where = ps.getWhere();
        if (where == null) {
            ps.setWhere(expr);
        } else if (!where.toString().contains(expr.toString())) {
            ps.setWhere(new AndExpression(where, expr));
        }
    }
}
