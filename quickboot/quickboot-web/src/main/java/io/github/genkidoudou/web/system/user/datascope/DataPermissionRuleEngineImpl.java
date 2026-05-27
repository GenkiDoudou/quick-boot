package io.github.genkidoudou.web.system.user.datascope;

import cn.hutool.core.collection.CollUtil;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.expression.operators.relational.InExpression;
import net.sf.jsqlparser.expression.operators.relational.ParenthesedExpressionList;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 参照旧 quick-boot {@code DataPermissionRuleEngineImpl}：从 Session 取归并后的范围并生成条件。
 */
@Component
public class DataPermissionRuleEngineImpl implements DataPermissionRuleEngine {

    @Override
    public Expression build(Table table, DataPermission ann) {
        DataScopeSession s = DataScopeSessionStore.get();
        if (s == null) {
            return null;
        }
        if (s.denyAll()) {
            return new EqualsTo(new LongValue(1), new LongValue(0));
        }
        if (s.scopeType() == DataScopeType.ALL) {
            return null;
        }
        if (s.scopeType() == DataScopeType.DEPT) {
            List<Long> deptIds = s.visibleDeptIds();
            if (CollUtil.isEmpty(deptIds)) {
                return new EqualsTo(new LongValue(1), new LongValue(0));
            }
            return buildDeptIn(table, ann.deptField(), deptIds);
        }
        EqualsTo self = new EqualsTo(new Column(table, ann.userField()), new LongValue(s.userId()));
        List<Long> deptIds = s.visibleDeptIds();
        if (CollUtil.isNotEmpty(deptIds)) {
            Expression deptIn = buildDeptIn(table, ann.deptField(), deptIds);
            return new OrExpression(deptIn, self);
        }
        return self;
    }

    private static Expression buildDeptIn(Table table, String deptField, List<Long> deptIds) {
        if (deptIds.size() == 1) {
            return new EqualsTo(new Column(table, deptField), new LongValue(deptIds.get(0)));
        }
        List<Expression> values = new ArrayList<>(deptIds.size());
        for (Long deptId : deptIds) {
            values.add(new LongValue(deptId));
        }
        InExpression in = new InExpression();
        in.setLeftExpression(new Column(table, deptField));
        in.setRightExpression(new ParenthesedExpressionList<>(values));
        return in;
    }
}
