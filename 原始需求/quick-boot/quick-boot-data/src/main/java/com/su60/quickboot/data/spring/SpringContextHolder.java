package com.su60.quickboot.data.spring;

import cn.hutool.core.lang.Opt;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.framework.AopContext;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.springframework.web.util.WebUtils;


import java.io.IOException;
import java.io.PrintWriter;

/**
 * @author luyanan
 * @since 2023/11/30
 **/

@Slf4j
@Service
//@Lazy(false)
@Configuration
@Order(-1000000000)
public class SpringContextHolder extends WebUtils implements BeanFactoryPostProcessor, ApplicationContextAware, DisposableBean {
	/**
	 * Spring应用上下文环境
	 */
	private static ConfigurableListableBeanFactory beanFactory;

	/**
	 * 上下文
	 *
	 * @since 2023/11/30
	 */

	private static ApplicationContext applicationContext = null;

	/**
	 * 取得存储在静态变量中的ApplicationContext.
	 * @return   ApplicationContext
	 * @since 2023/11/30
	 */
	public static ApplicationContext getApplicationContext() {
		return applicationContext;
	}

	/**
	 * 实现ApplicationContextAware接口, 注入Context到静态变量中.
	 */
	@Override
	public void setApplicationContext(ApplicationContext applicationContext) {
		SpringContextHolder.applicationContext = applicationContext;
	}

	/**
	 * 从静态变量applicationContext中取得Bean, 自动转型为所赋值对象的类型.
	 *
	 * @param name bean 名称
	 * @param <T>  bean 泛型
	 * @return T  bean
	 * @since 2022/7/5
	 */
	@SuppressWarnings("unchecked")
	public static <T> T getBean(String name) {
		return Opt.ofNullable(beanFactory).map(a -> (T) a.getBean(name)).orElseGet(() -> null);
	}

	/**
	 * 从静态变量applicationContext中取得Bean, 自动转型为所赋值对象的类型.
	 *
	 * @param requiredType bean class
	 * @param <T>          bean 泛型
	 * @return T  bean
	 * @since 2022/7/5
	 */
	public static <T> T getBean(Class<T> requiredType) {
		return Opt.ofNullable(beanFactory).map(a -> (T) a.getBean(requiredType)).orElseGet(() -> null);
	}

	/**
	 * 清除SpringContextHolder中的ApplicationContext为Null.
	 */
	public static void clearHolder() {
		if (log.isDebugEnabled()) {
			log.debug("清除SpringContextHolder中的ApplicationContext:" + applicationContext);
		}
		applicationContext = null;
	}

	/**
	 * 发布事件
	 *
	 * @param event 事件
	 */
	public static void publishEvent(ApplicationEvent event) {
		if (applicationContext == null) {
			return;
		}
		applicationContext.publishEvent(event);
	}

	/**
	 * 实现DisposableBean接口, 在Context关闭时清理静态变量.
	 */
	@Override
	public void destroy() {
		SpringContextHolder.clearHolder();
	}

	/**
	 * 获取HttpServletRequest
	 *
	 * @return javax.servlet.http.HttpServletRequest
	 * @since 2021/1/23
	 */
	public static HttpServletRequest getRequest() {
		return Opt
				.ofNullable(RequestContextHolder.getRequestAttributes())
				.filter(a -> null != a)
				.map(a -> (ServletRequestAttributes) a)
				.filter(a -> null != a)
				.map(a -> a.getRequest())
				.orElse(null);
	}

	/**
	 * 获取HttpServletResponse
	 *
	 * @return javax.servlet.http.HttpServletResponse
	 * @since 2021/1/23
	 */
	public static HttpServletResponse getResponse() {
		return Opt.ofNullable(RequestContextHolder.getRequestAttributes()).map(r -> (ServletRequestAttributes) r).map(a -> a.getResponse()).orElse(null);
	}

	/**
	 * 设置cookie
	 *
	 * @param response        返回对象
	 * @param name            cookie 名称
	 * @param value           cookie value
	 * @param maxAgeInSeconds 存活时间
	 * @param domain          cookie域
	 * @since 2021/1/23
	 */
	public static void setCookie(HttpServletResponse response, String name, String value, String domain,
								 int maxAgeInSeconds) {
		Cookie cookie = new Cookie(name, value);
		cookie.setPath("/");
		cookie.setMaxAge(maxAgeInSeconds);
		cookie.setHttpOnly(true);
		if (StrUtil.isNotBlank(domain)) {
			cookie.setDomain(domain);
		}
		response.addCookie(cookie);
	}


	/**
	 * 删除cookie
	 *
	 * @param response http 返回对象
	 * @param name     cookie name
	 * @param domain   cookie域
	 * @since 2021/1/23
	 */
	public static void removeCookie(HttpServletResponse response, String name, String domain) {
		setCookie(response, name, null, domain, 0);
	}


	/**
	 * 获取HandlerMethod
	 *
	 * @param request 请求体
	 * @return org.springframework.web.method.HandlerMethod
	 * @since 2021/6/6
	 */
	@SneakyThrows
	public static HandlerMethod getHandlerMethod(HttpServletRequest request) {
		return (HandlerMethod) Opt.ofNullable(applicationContext.getBean(RequestMappingHandlerMapping.class))
				.map(re -> {
					try {
						return re.getHandler(request).getHandler();
					} catch (Exception e) {
						e.printStackTrace();
					}
					return null;
				}).orElseGet(null);

	}

	/**
	 * 获取HandlerMethod
	 *
	 * @return org.springframework.web.method.HandlerMethod
	 * @since 2021/6/6
	 */
	@SneakyThrows
	public static HandlerMethod getHandlerMethod() {
		return getHandlerMethod(getRequest());
	}


	/**
	 * 读取cookie
	 *
	 * @param request HttpServletRequest
	 * @param name    cookie name
	 * @return cookie value
	 */
	public String getCookieVal(HttpServletRequest request, String name) {
		Cookie cookie = WebUtils.getCookie(request, name);
		return cookie != null ? cookie.getValue() : null;
	}

	/**
	 * 清除 某个指定的cookie
	 *
	 * @param response HttpServletResponse
	 * @param key      cookie key
	 */
	public void removeCookie(HttpServletResponse response, String key) {
		setCookie(response, key, null, 0);
	}

	/**
	 * 设置cookie
	 *
	 * @param response        HttpServletResponse
	 * @param name            cookie name
	 * @param value           cookie value
	 * @param maxAgeInSeconds maxage
	 */
	public void setCookie(HttpServletResponse response, String name, String value, int maxAgeInSeconds) {
		Cookie cookie = new Cookie(name, value);
		cookie.setPath("/");
		cookie.setMaxAge(maxAgeInSeconds);
		cookie.setHttpOnly(true);
		response.addCookie(cookie);
	}


	/**
	 * 返回json
	 *
	 * @param response HttpServletResponse
	 * @param result   结果对象
	 */
	public void renderJson(HttpServletResponse response, Object result) {
		renderJson(response, result, MediaType.APPLICATION_JSON_VALUE);
	}

	/**
	 * 返回json
	 *
	 * @param response    HttpServletResponse
	 * @param result      结果对象
	 * @param contentType contentType
	 */
	public void renderJson(HttpServletResponse response, Object result, String contentType) {
		response.setCharacterEncoding("UTF-8");
		response.setContentType(contentType);
		try (PrintWriter out = response.getWriter()) {
			out.append(JSONUtil.toJsonStr(result));
		} catch (IOException e) {
			log.error(e.getMessage(), e);
		}
	}


	/**
	 * 获取aop代理对象
	 *
	 * @param invoker 类
	 * @param <T>     泛型
	 * @return T  类实例
	 * @since 2023/11/30
	 */
	@SuppressWarnings("unchecked")
	public static <T> T getAopProxy(T invoker) {
		return (T) AopContext.currentProxy();
	}

	@Override
	public void postProcessBeanFactory(ConfigurableListableBeanFactory configurableListableBeanFactory) throws BeansException {
		SpringContextHolder.beanFactory = configurableListableBeanFactory;
	}

}
