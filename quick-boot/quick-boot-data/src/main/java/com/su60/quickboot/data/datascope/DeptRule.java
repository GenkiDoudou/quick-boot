//package com.su60.quickboot.data.datascope;
//
//import net.sf.jsqlparser.expression.Expression;
//import net.sf.jsqlparser.expression.LongValue;
//import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
//import net.sf.jsqlparser.schema.Column;
//import net.sf.jsqlparser.schema.Table;
//
//public class DeptRule implements DataPermissionRule {
//	public boolean supports(DataScope s) {
//		return s == DataScope.DEPT;
//	}
//
//	public Expression buildExpression(Table t, DataPermission a, UserContext u) {
//		return new EqualsTo(
//				new Column(t, a.deptField()),
//				new LongValue(u.getDeptId())
//		);
//	}
//}
