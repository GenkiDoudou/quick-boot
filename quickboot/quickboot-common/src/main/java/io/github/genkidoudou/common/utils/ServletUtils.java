package io.github.genkidoudou.common.utils;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.extra.servlet.JakartaServletUtil;
import cn.hutool.extra.spring.SpringUtil;
import io.github.genkidoudou.common.api.R;
import io.github.genkidoudou.common.i18n.I18nUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.util.UrlPathHelper;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Servlet 工具（基于 Hutool {@link JakartaServletUtil}，适配 Spring Boot 3+/Jakarta）。
 */
public class ServletUtils extends JakartaServletUtil {
  /** JSON 响应 Content-Type（UTF-8）。 */
  public static final String CONTENT_TYPE_JSON_UTF8 = "application/json;charset=UTF-8";
  private final static AntPathMatcher antPathMatcher = new AntPathMatcher();
  private final static UrlPathHelper urlPathHelper = new UrlPathHelper();

  /**
   * 向响应写入统一 JSON 错误体 {@link R#error(int, String)}；HTTP 状态码固定 200。
   *
   * @param response HTTP 响应
   * @param code     业务错误码
   * @param args     i18n 占位参数
   * @throws IOException 写入失败
   */
  public static void writeResponse(HttpServletResponse response, Integer code, Object... args) throws IOException {
    String message = I18nUtil.getMessage(code, args);
    response.setStatus(HttpServletResponse.SC_OK);
    response.setCharacterEncoding(StandardCharsets.UTF_8.name());
    response.setContentType(CONTENT_TYPE_JSON_UTF8);

    ObjectMapper mapper = resolveObjectMapper();
    mapper.writeValue(response.getWriter(), R.error(code, message));
    response.getWriter().flush();
  }

  /**
   * 解析 Spring 容器中的 {@link ObjectMapper}；不可用时返回默认实例。
   *
   * @return Jackson ObjectMapper
   */
  public static ObjectMapper resolveObjectMapper() {
    try {
      return SpringUtil.getBean(ObjectMapper.class);
    } catch (Throwable ignored) {
      return new ObjectMapper();
    }
  }


  /**
   * 判断路径是否匹配任一 Ant 风格模式。
   *
   * @param path     待匹配路径
   * @param patterns 模式列表
   * @return 任一匹配则为 {@code true}
   */
  public static boolean matchesAny(String path, List<String> patterns) {
    if (path == null || patterns == null || patterns.isEmpty()) {
      return false;
    }
    for (String p : patterns) {
      if (p == null || p.isEmpty()) {
        continue;
      }
      if (antPathMatcher.match(p, path)) {
        return true;
      }
    }
    return false;
  }

  /**
   * 从当前请求解析应用内路径，再匹配 Ant 模式列表。
   *
   * @param request  HTTP 请求
   * @param patterns 模式列表
   * @return 任一匹配则为 {@code true}
   */
  public static boolean matchesAny(HttpServletRequest request, List<String> patterns) {
    if (CollectionUtil.isEmpty(patterns)) {
      return false;
    }
    String pathWithinApp = urlPathHelper.getPathWithinApplication(request);
    return matchesAny(pathWithinApp, patterns);
  }

  /**
   * 从 {@link RequestContextHolder} 获取当前 HTTP 请求；非 Web 线程返回 {@code null}。
   *
   * @return 当前请求或 {@code null}
   */
  public static HttpServletRequest currentRequest() {
    var attrs = RequestContextHolder.getRequestAttributes();
    if (attrs instanceof ServletRequestAttributes servletAttrs) {
      return servletAttrs.getRequest();
    }
    return null;
  }

}
