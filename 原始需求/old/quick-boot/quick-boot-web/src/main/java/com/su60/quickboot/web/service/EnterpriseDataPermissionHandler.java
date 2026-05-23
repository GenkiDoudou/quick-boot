//package com.su60.quickboot.web.service;
//
//import com.baomidou.mybatisplus.extension.plugins.handler.DataPermissionHandler;
//import com.su60.quickboot.core.security.LoginUser;
//import com.su60.quickboot.core.security.LoginUserUtils;
//import com.su60.quickboot.data.datascope.DataPermission;
//import com.su60.quickboot.data.datascope.DataScopeType;
//import com.su60.quickboot.system.service.ISysDeptService;
//import lombok.RequiredArgsConstructor;
//import net.sf.jsqlparser.expression.Expression;
//import net.sf.jsqlparser.expression.LongValue;
//import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
//import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
//import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
//import net.sf.jsqlparser.expression.operators.relational.InExpression;
//import net.sf.jsqlparser.schema.Column;
//import org.springframework.stereotype.Component;
//import org.springframework.util.StringUtils;
//
//import java.util.List;
//
///**
// * 企业级数据权限处理器
// *
// * ✔ 零 SQL 侵入
// * ✔ 不查数据库
// * ✔ 高并发安全
// */
//@RequiredArgsConstructor
//@Component
//public class EnterpriseDataPermissionHandler implements DataPermissionHandler {
//
//	private final ISysDeptService sysDeptService;
//
//	@Override
//	public Expression getSqlSegment(Expression where, String mappedStatementId) {
//
//		LoginUser user = LoginUserUtils.getUser();
//
//		// 1️⃣ 未登录或管理员，直接放行
//		if (user == null
//				|| user.getDataScopeType() == DataScopeType.ALL) {
//			return where;
//		}
//
//		// 2️⃣ 获取 @DataPermission 注解
//		DataPermission permission =
//				DataPermissionHelper.getPermission(mappedStatementId);
//
//		if (permission == null) {
//			return where;
//		}
//
//		// 3️⃣ 构建权限条件
//		Expression condition = buildCondition(user, permission);
//
//		if (condition == null) {
//			return where;
//		}
//
//		// 4️⃣ 拼接 where
//		return where == null
//				? condition
//				: new AndExpression(where, condition);
//	}
//
//	private Expression buildCondition(LoginUser user, DataPermission p) {
//
//		String alias = p.tableAlias();
//		String deptCol = column(alias, p.deptColumn());
//		String userCol = column(alias, p.userColumn());
//
//		return switch (user.getDataScopeType()) {
//			case SELF -> eq(userCol, user.getId());
//			case DEPT -> eq(deptCol, user.getDeptId());
//			case DEPT_AND_CHILD, CUSTOM -> in(deptCol, user);
//
//			default -> null;
//		};
//	}
//
//	private String column(String alias, String column) {
//		return StringUtils.hasText(alias)
//				? alias + "." + column
//				: column;
//	}
//
//	private Expression eq(String column, Long value) {
//		return new EqualsTo(
//				new Column(column),
//				new LongValue(value)
//		);
//	}
//
//	private Expression in(String column, LoginUser loginUser) {
//
//		// 查询本部门
//		List<Long> values = sysDeptService.listDataScope(loginUser);
//		// 防止空集合导致 SQL 错误
//		if (values == null || values.isEmpty()) {
//			return new EqualsTo(new LongValue(1), new LongValue(2));
//		}
//
//		return new InExpression(
//				new Column(column),
//				new ExpressionList(
//						values.stream()
//								.map(LongValue::new)
//								.toList()
//				)
//		);
//	}
//}