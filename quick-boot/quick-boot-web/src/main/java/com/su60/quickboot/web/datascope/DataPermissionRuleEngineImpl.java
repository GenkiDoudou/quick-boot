package com.su60.quickboot.web.datascope;

import cn.hutool.core.collection.CollectionUtil;
import com.su60.quickboot.core.security.LoginUser;
import com.su60.quickboot.core.security.LoginUserUtils;
import com.su60.quickboot.data.datascope.DataPermission;
import com.su60.quickboot.data.datascope.DataPermissionRuleEngine;
import com.su60.quickboot.data.datascope.DataScopeType;
import lombok.RequiredArgsConstructor;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.Parenthesis;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.expression.operators.relational.InExpression;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class DataPermissionRuleEngineImpl implements DataPermissionRuleEngine {

	@Override
	public Expression build(Table table, DataPermission ann) {
		// 权限这里
		// 1. 当包含所有权限的时候 则直接取最大的
		// 2. 其他的：

		// 感觉角色来判断
		LoginUser user = LoginUserUtils.getUser();
		if (null == user) {
			// 当不登陆的时候不处理
			return null;
		}
		List<Long> roleIds =
				user.getRoleIds();

		if (CollectionUtil.isEmpty(roleIds)) {
			// 没有任何权限
			return new EqualsTo(new LongValue(1), new LongValue(0));
		}
		DataScopeType dataScopeType = user.getDataScopeType();
		List<Long> dataScopeDeptIds = user.getDataScopeDeptIds();
		if (dataScopeType == DataScopeType.ALL) {
			// 给全部权限
			return null;
		} else if (dataScopeType == DataScopeType.DEPT) {
			return buildDeptIn(table, ann.deptField(), dataScopeDeptIds);
		} else {
			// 只能看自己的
			// 自己的
			EqualsTo self = new EqualsTo(
					new Column(table, ann.userField()),
					new LongValue(user.getId()));
			if (CollectionUtil.isNotEmpty(dataScopeDeptIds)) {
				Expression deptIn = buildDeptIn(table, ann.deptField(), dataScopeDeptIds);
				return new OrExpression(deptIn, self);

			} else {
				return self;
			}


		}


//		else if (dataScopeType == DataScopeType.DEPT) {
//			List<Long> dataScopeDeptIds = user.getDataScopeDeptIds();
//			if (CollectionUtil.isEmpty(dataScopeDeptIds)){
//
//			}
//			// 本部门
//			return new EqualsTo(
//					new Column(table, ann.deptField()),
//					new LongValue(user.getDeptId())
//			);
//
//		} else if (dataScopeType == DataScopeType.DEPT_AND_CHILD || dataScopeType == DataScopeType.CUSTOM) {
//			// 自定义
//			List<SysRoleDo> sysRoleDos = sysRoleService.listByUserId(user.getId());
//
//			List<Long> deptIds = new ArrayList<>();
//			return new InExpression(
//					new Column(table, ann.deptField()),
//					new ExpressionList(
//							deptIds
//					)
//			);
//		} else if (dataScopeType == DataScopeType.SELF) {
//			//  自己
//			return new EqualsTo(
//					new Column(table, ann.userField()),
//					new LongValue(user.getId())
//			);
//		}
//		return new EqualsTo(new LongValue(1), new LongValue(0));
	}

	private Expression buildDeptIn(
			Table table,
			String deptField,
			List<Long> deptIds) {

		// 单值直接 =
		if (deptIds.size() == 1) {
			return new EqualsTo(
					new Column(table, deptField),
					new LongValue(deptIds.get(0))
			);
		}

		List<Expression> values = new ArrayList<>();
		for (Long deptId : deptIds) {
			values.add(new LongValue(deptId));
		}

		ExpressionList expressionList = new ExpressionList();
		expressionList.setExpressions(values);

		// ⭐ 关键：强制加括号
		Parenthesis parenthesis = new Parenthesis(expressionList);

		InExpression in = new InExpression();
		in.setLeftExpression(new Column(table, deptField));
		in.setRightExpression(parenthesis);

		return in;
	}


}
