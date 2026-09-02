package io.github.genkidoudou.common.idempotency;

import cn.dev33.satoken.stp.StpUtil;
import io.github.genkidoudou.common.exception.ErrorCodes;
import io.github.genkidoudou.common.exception.WarningException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.expression.MethodBasedEvaluationContext;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

/**
 * 解析 {@link Idempotent#key()} SpEL 表达式，生成防重键片段。
 * <p>
 * 变量约定对齐若依 {@code @RepeatSubmit} 习惯：{@code #参数名}、{@code #body.xxx}、{@code #userId}、{@code #request}。
 */
final class IdempotencySpelEvaluator {

  private static final SpelExpressionParser PARSER = new SpelExpressionParser();
  private static final ParameterNameDiscoverer PARAM_NAMES = new DefaultParameterNameDiscoverer();

  private IdempotencySpelEvaluator() {
  }

  /**
   * @param spel    注解上的 SpEL 表达式
   * @param method  目标方法
   * @param args    实参
   * @param request 当前请求（可为 {@code null}）
   * @return 非空字符串键片段
   */
  static String evaluate(String spel, Method method, Object[] args, HttpServletRequest request) {
    if (!StringUtils.hasText(spel)) {
      return null;
    }
    EvaluationContext context = buildContext(method, args, request);
    Expression expression = PARSER.parseExpression(spel.trim());
    Object value;
    try {
      value = expression.getValue(context);
    } catch (Exception ex) {
      throw WarningException.literal(ErrorCodes.Common.INVALID_PARAM,
          "幂等 SpEL 表达式解析失败: " + ex.getMessage());
    }
    if (value == null || !StringUtils.hasText(String.valueOf(value))) {
      throw WarningException.literal(ErrorCodes.Common.INVALID_PARAM, "幂等 SpEL 表达式结果为空");
    }
    return String.valueOf(value).trim();
  }

  private static EvaluationContext buildContext(Method method, Object[] args, HttpServletRequest request) {
    MethodBasedEvaluationContext context =
        new MethodBasedEvaluationContext(null, method, args, PARAM_NAMES);
    context.setVariable("request", request);
    context.setVariable("userId", resolveLoginId());
    Object body = findRequestBody(method, args);
    if (body != null) {
      context.setVariable("body", body);
    }
    return context;
  }

  /** 取第一个 {@link RequestBody} 标注的参数作为 {@code #body}。 */
  private static Object findRequestBody(Method method, Object[] args) {
    Parameter[] parameters = method.getParameters();
    if (parameters == null || args == null) {
      return null;
    }
    int len = Math.min(parameters.length, args.length);
    for (int i = 0; i < len; i++) {
      if (parameters[i].isAnnotationPresent(RequestBody.class)) {
        return args[i];
      }
    }
    return null;
  }

  private static String resolveLoginId() {
    try {
      if (StpUtil.isLogin()) {
        return StpUtil.getLoginIdAsString();
      }
    } catch (Throwable ignored) {
      // 测试或未装配 sa-token
    }
    return "anon";
  }
}
