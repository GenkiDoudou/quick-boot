//package com.su60.quickboot.data.datascope;
//
//import net.sf.jsqlparser.expression.Expression;
//import net.sf.jsqlparser.expression.LongValue;
//import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
//import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
//import net.sf.jsqlparser.expression.operators.relational.InExpression;
//import net.sf.jsqlparser.schema.Column;
//import net.sf.jsqlparser.schema.Table;
//
//import java.util.Set;
//import java.util.stream.Collectors;
//
//public class CustomRule implements DataPermissionRule {
//
//	public boolean supports(DataScope s) {
//		return s == DataScope.CUSTOM;
//	}
//
//	public Expression buildExpression(Table t, DataPermission a, UserContext u) {
//		Set<Long> deptIds = u.getRoles().stream()
//				.filter(r -> r.getDataScope() == DataScope.CUSTOM)
//				.flatMap(r -> r.getDeptIds().stream())
//				.collect(Collectors.toSet());
//
//		if (deptIds.isEmpty()) {
//			return new EqualsTo(new LongValue(1), new LongValue(0));
//		}
//
//		return new InExpression(
//				new Column(t, a.deptField()),
//				new ExpressionList(
//						deptIds.stream().map(LongValue::new).toList()
//				)
//		);
//	}
//}
//
