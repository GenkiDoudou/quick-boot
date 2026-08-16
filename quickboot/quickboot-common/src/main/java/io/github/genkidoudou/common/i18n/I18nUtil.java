package io.github.genkidoudou.common.i18n;

import cn.hutool.extra.spring.SpringUtil;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.context.i18n.LocaleContextHolder;

import java.util.Locale;


/**
 * 国际化文案解析工具：按业务码从 Spring {@link MessageSource} 取消息，支持占位参数与线程 Locale。
 */
public final class I18nUtil {

  private I18nUtil() {
  }

  /**
   * 使用当前线程 {@link LocaleContextHolder} 中的 Locale 解析文案。
   */
  public static String getMessage(Integer code) {
    return getMessage(code, null, null);
  }

  /**
   * 使用占位参数解析消息。
   * <p>
   * 未提供可变参数重载：Java 中 {@code Object...} 与 {@code Object[]} 擦除后签名冲突，调用方请传入数组。
   */
  public static String getMessage(Integer code, Object[] args) {
    return getMessage(code, args, null);
  }

  /**
   * 指定调用方兜底文案。
   *
   * @param defaultMessage 调用方兜底（可为 {@code null}）
   */
  public static String getMessage(Integer code, Object[] args, String defaultMessage) {
    if (defaultMessage != null && !defaultMessage.isBlank()) {
      return defaultMessage;
    }
    return resolveFromMessageSource(code, args);
  }

  /**
   * @return 当前线程 Locale（来自 {@link LocaleContextHolder}）
   */
  public static Locale getLocale() {
    return LocaleContextHolder.getLocale();
  }

  /**
   * 设置当前线程 Locale，供后续 {@link #getMessage(Integer, Object[], String)} 解析使用。
   *
   * @param locale 目标 Locale
   */
  public static void setLocale(Locale locale) {
    LocaleContextHolder.setLocale(locale);
  }

  private static MessageSource messageSource() {
    try {
      // 必须用 Bean 名 messageSource；否则可能拿到 ApplicationContext 内部空的 DelegatingMessageSource
      return SpringUtil.getBean("messageSource", MessageSource.class);
    } catch (Throwable ignored) {
      return null;
    }
  }

  private static String resolveFromMessageSource(Integer code, Object[] args) {
    MessageSource ms = messageSource();
    if (ms == null) {
      return null;
    }
    Locale locale = LocaleContextHolder.getLocale();
    try {
      return ms.getMessage(code + "", args, locale);
    } catch (NoSuchMessageException ignored) {
      return null;
    } catch (Exception ignored) {
      return null;
    }
  }
}
