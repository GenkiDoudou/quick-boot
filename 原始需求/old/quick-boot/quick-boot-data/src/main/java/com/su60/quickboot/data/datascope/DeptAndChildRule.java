//package com.su60.quickboot.data.datascope;
//
//import net.sf.jsqlparser.expression.Expression;
//import net.sf.jsqlparser.parser.CCJSqlParserUtil;
//import net.sf.jsqlparser.schema.Table;
//
//public class DeptAndChildRule implements DataPermissionRule {
//
//	public boolean supports(DataScope s) {
//		return s == DataScope.DEPT_AND_CHILD;
//	}
//
//	public Expression buildExpression(Table t, DataPermission a, UserContext u) {
//		String alias = t.getAlias() != null ? t.getAlias().getName() : t.getName();
//		String sql = """
//				EXISTS (
//				  SELECT 1 FROM sys_dept d
//				  WHERE d.id = %s.%s
//				    AND FIND_IN_SET(%d, d.ancestors)
//				)
//				""".formatted(alias, a.deptField(), u.getDeptId());
//		try {
//			return CCJSqlParserUtil.parseCondExpression(sql);
//		} catch (Exception e) {
//			throw new RuntimeException(e);
//		}
//	}
//}
