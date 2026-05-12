package com.su60.quickboot.data.logger;

import cn.hutool.core.lang.Opt;
import com.su60.quickboot.common.exception.BaseException;
import com.su60.quickboot.common.exception.GlobalMsgCode;
import com.su60.quickboot.common.utils.ServletUtil;
import com.su60.quickboot.data.logger.annotation.IgnoreLogger;
import com.su60.quickboot.data.spring.SpringContextHolder;
import com.alibaba.fastjson.JSON;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.Signature;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * 日志解析解析类
 *
 * @author luyanan
 * @since 2023/05/21
 **/
public class AbstractLoggerParserHandler {


	/**
	 * 方法介绍分割线
	 *
	 * @author luyanan
	 * @since 2023/05/21
	 */
	protected static final String DESP_DIVIDING_LINE = "-";


	/**
	 * 解析
	 *
	 * @param loggerEventDto loggerEventDto
	 * @return com.github.rainsoil.fastapi.common.core.logger.core.LoggerInfo
	 * @since 2023/05/22
	 */
	public LoggerInfo parserToLoggerInfo(LoggerEventDto loggerEventDto) {

		if (null == loggerEventDto) {
			return null;
		}

		Signature signature = loggerEventDto.getSignature();
		Method method = null;
		IgnoreLogger ignoreLogger = null;
		String params = null;
		if (null != signature) {
			MethodSignature methodSignature = (MethodSignature) loggerEventDto.getSignature();
			method = methodSignature.getMethod();
			ignoreLogger = method.getAnnotation(IgnoreLogger.class);
			// 不添加注解 或者 注解的类型不为ALL 或者不等于parade，
			Map<String, Object> requestParams = getRequestParams(methodSignature, loggerEventDto.getArgs());
			params = JSON.toJSONString(requestParams);
		} else {
			params = JSON.toJSONString(loggerEventDto.getArgs());
		}
		// 时间
		Long timeConsuming = loggerEventDto.getEndTime() - loggerEventDto.getStartTime();
		if (null != loggerEventDto.getStartTime() && null != loggerEventDto.getEndTime()) {

			timeConsuming = loggerEventDto.getEndTime() - loggerEventDto.getStartTime();
		}
		HttpServletRequest request =
				SpringContextHolder.getRequest();
		String result = null;
		if (null != loggerEventDto.getResult()) {
			Object result1 = loggerEventDto.getResult();
			if (result1 instanceof String) {
				result = (String) result1;
			} else {
				result = JSON.toJSONString(result1);
			}
		}

		LoggerInfo loggerInfo = new LoggerInfo()
				.setMethodName(Opt
						.ofNullable(method)
						.map(me -> me.getDeclaringClass().getName() + "." + me.getName())
						.orElse(null))
				// 方法介绍
				.setDesp(getDesp(method))
				.setResult(result)
				.setStartTime(loggerEventDto.getStartTime())
				.setEndTime(loggerEventDto.getEndTime())
				.setTimeConsuming(timeConsuming).setTraceId(loggerEventDto.getTraceId())
				.setErrorMsg(Opt
						.ofNullable(loggerEventDto.getThrowable())
						.map(a -> a.getLocalizedMessage())
						.orElse(null))
				.setCode(Opt
						.ofNullable(loggerEventDto.getThrowable())
						.map(a -> {
							if (a instanceof BaseException) {
								BaseException baseException = (BaseException) a;
								return baseException.getCode();
							} else {
								return GlobalMsgCode.INTERNAL_SERVER_ERROR;
							}
						}).orElse(GlobalMsgCode.SUCCESS))
				.setSourceIp(Opt
						.ofNullable(request)
						.map(a -> ServletUtil.getClientIP(request))
						.orElse(null))
				.setUri(Opt
						.ofNullable(request)
						.map(a -> a.getRequestURI())
						.orElse(null))
				.setMethod(Opt.ofNullable(request)
						.map(a -> a.getMethod())
						.orElse(null))
				.setRequestParams(params);
		return loggerInfo;
	}


	/**
	 * 方法介绍
	 *
	 * @param method 方法
	 * @return java.lang.String
	 * @since 2023/05/22
	 */
	protected String getDesp(Method method) {
		if (null == method) {
			return null;
		}
		StringBuilder desp = new StringBuilder();
		if (method.getDeclaringClass().isAnnotationPresent(Api.class)) {
			String apiValue = method.getDeclaringClass().getAnnotation(Api.class).value();
			if (StringUtils.isEmpty(apiValue)) {
				//noinspection AliDeprecation
				apiValue = method.getDeclaringClass().getAnnotation(Api.class).value();
			}
			desp.append(apiValue).append(DESP_DIVIDING_LINE);
		}
		if (method.isAnnotationPresent(ApiOperation.class)) {
			desp.append(method.getAnnotation(ApiOperation.class).value());
		}
		if (desp.toString().endsWith(DESP_DIVIDING_LINE)) {
			desp.deleteCharAt(desp.length() - 1);
		}
		return desp.toString();
	}


	/**
	 * 获取请求参数
	 *
	 * @param methodSignature 方法签名
	 * @param args            参数
	 * @return java.util.Map<java.lang.String, java.lang.Object>
	 * @since 2023/05/21
	 */
	protected Map<String, Object> getRequestParams(MethodSignature methodSignature, Object[] args) {
		if (null == args || args.length == 0) {
			return new HashMap<>();
		}
		Map<String, Object> requestParams = new HashMap<>(16);

		// 参数名
		String[] paramNames = methodSignature.getParameterNames();
		if (null == paramNames || paramNames.length == 0) {
			return new HashMap<>(16);
		}

		for (int i = 0; i < paramNames.length; i++) {
			Object value = args[i];
			if (null == value) {
				continue;
			}

			// 如果是文件对象
			if (value instanceof MultipartFile) {
				MultipartFile file = (MultipartFile) value;
				// 获取文件名
				value = file.getOriginalFilename();
			} else if (value instanceof ServletResponse || value instanceof ServletRequest) {

				continue;
			} else if (value instanceof Model || value.getClass().getName().startsWith("org.springframework")) {
				continue;
			}

			requestParams.put(paramNames[i], value);
		}
		return requestParams;
	}
}
