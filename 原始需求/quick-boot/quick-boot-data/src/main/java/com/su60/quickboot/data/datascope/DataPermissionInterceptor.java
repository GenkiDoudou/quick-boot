package com.su60.quickboot.data.datascope;

import cn.hutool.core.util.ReflectUtil;
import com.baomidou.mybatisplus.core.toolkit.PluginUtils;
import lombok.RequiredArgsConstructor;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.Join;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.WithItem;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.sql.Connection;

@Intercepts({
		@Signature(
				type = StatementHandler.class,
				method = "prepare",
				args = {Connection.class, Integer.class}
		)
})
@RequiredArgsConstructor
@Component
public class DataPermissionInterceptor implements Interceptor {


	private final DataPermissionRuleEngine dataPermissionRuleEngine;

	@Override
	public Object intercept(Invocation invocation) throws Throwable {

		StatementHandler handler =
				(StatementHandler) PluginUtils.realTarget(invocation.getTarget());

		MetaObject metaObject = SystemMetaObject.forObject(handler);

		MappedStatement ms =
				(MappedStatement) metaObject.getValue("delegate.mappedStatement");
		;

		DataPermission ann = findAnnotation(ms);
		if (null == ann) {
			ann = DataPermissionContext.get();
		}

		if (ann == null || ann.ignore()) {
			return invocation.proceed();
		}

		BoundSql boundSql = handler.getBoundSql();
		String originalSql = boundSql.getSql();

		Statement stmt;
		try {
			stmt = CCJSqlParserUtil.parse(originalSql);
		} catch (Exception e) {
			// XML 复杂 SQL 解析失败 → 安全放行
			return invocation.proceed();
		}

		if (stmt instanceof Select select) {
			processSelect(select, ann);
			ReflectUtil.setFieldValue(boundSql, "sql", select.toString());
		}

		return invocation.proceed();
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

	private void handleTable(
			PlainSelect ps, Table table, DataPermission ann) {

		if (!TableMatchUtil.match(table.getName(), ann.tables())) {
			return;
		}

//		UserContext user = DataPermissionContext.get();
//		DataScope scope =
//				DataPermissionRuleEngine.calcScope(user.getRoles());

//		Expression expr =
//				DataPermissionRuleEngine.build(scope, table, ann, user);
		Expression expr = dataPermissionRuleEngine.build(table, ann);

		if (expr == null) return;

		if (ps.getWhere() == null) {
			ps.setWhere(expr);
		} else if (!ps.getWhere().toString().contains(expr.toString())) {
			ps.setWhere(new AndExpression(ps.getWhere(), expr));
		}
	}

	/**
	 * 查找 Service / Mapper 上的 @DataPermission
	 */
	private DataPermission findAnnotation(MappedStatement ms) {
		String id = ms.getId();
		String className = id.substring(0, id.lastIndexOf('.'));
		String methodName = id.substring(id.lastIndexOf('.') + 1);
		try {
			Class<?> clazz = Class.forName(className);
			for (Method m : clazz.getMethods()) {
				if (m.getName().equals(methodName)
						&& m.isAnnotationPresent(DataPermission.class)) {
					return m.getAnnotation(DataPermission.class);
				}
			}
		} catch (Exception ignored) {
		}
		return null;
	}
}
