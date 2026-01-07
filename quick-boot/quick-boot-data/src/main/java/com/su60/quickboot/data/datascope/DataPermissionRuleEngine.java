package com.su60.quickboot.data.datascope;


import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.schema.Table;

import java.util.List;

public interface DataPermissionRuleEngine {

	/**
	 * 构建
	 * @since 2026/1/2
	 * @param table  表
	 * @param ann 注解
	 * @return
	 */
	Expression build(
			Table table,
			DataPermission ann);
//	private static final List<DataPermissionRule> RULES = List.of(
//			new AllRule(),
//			new CustomRule(),
//			new DeptAndChildRule(),
//			new DeptRule(),
//			new SelfRule()
//	);
//
//	/**
//	 * 多角色合并，取最宽
//	 */
//	public static DataScope calcScope(List<Role> roles) {
//		if (roles.stream().anyMatch(r -> r.getDataScope() == DataScope.ALL)) return DataScope.ALL;
//		if (roles.stream().anyMatch(r -> r.getDataScope() == DataScope.CUSTOM)) return DataScope.CUSTOM;
//		if (roles.stream().anyMatch(r -> r.getDataScope() == DataScope.DEPT_AND_CHILD)) return DataScope.DEPT_AND_CHILD;
//		if (roles.stream().anyMatch(r -> r.getDataScope() == DataScope.DEPT)) return DataScope.DEPT;
//		return DataScope.SELF;
//	}
//
//	public static Expression build(
//			DataScope scope, Table table,
//			DataPermission ann, UserContext user) {
//
//		for (DataPermissionRule rule : RULES) {
//			if (rule.supports(scope)) {
//				return rule.buildExpression(table, ann, user);
//			}
//		}
//		return null;
//	}
}
