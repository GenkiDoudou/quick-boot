package io.github.genkidoudou.web.system.user.datascope;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.schema.Table;

/**
 * 根据 {@link DataScopeSession} 为命中表构建 JSQLParser 条件（旧 quick-boot 规则引擎职责）。
 */
public interface DataPermissionRuleEngine {

    /**
     * @param table SQL 中的表对象
     * @param ann   当前数据权限注解
     * @return 追加到 WHERE 的表达式；无需限制时返回 {@code null}
     */
    Expression build(Table table, DataPermission ann);
}
