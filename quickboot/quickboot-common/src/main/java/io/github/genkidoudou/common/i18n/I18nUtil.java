package io.github.genkidoudou.common.i18n;

import cn.hutool.extra.spring.SpringUtil;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.context.i18n.LocaleContextHolder;

import java.util.Locale;


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

  public static Locale getLocale() {
    return LocaleContextHolder.getLocale();
  }

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
