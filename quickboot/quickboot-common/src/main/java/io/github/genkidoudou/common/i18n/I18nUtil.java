package io.github.genkidoudou.common.i18n;

import cn.hutool.extra.spring.SpringUtil;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.context.i18n.LocaleContextHolder;

import java.util.Locale;

/**
 * 国际化文案工具：基于 Spring {@link MessageSource} 与 {@link LocaleContextHolder}。
 * <p>
 * 须在 Spring 容器就绪后调用（通过 Hutool {@link SpringUtil} 获取 {@link MessageSource}）。
 * MessageSource 不可用或词条未命中时，回退至 {@link BizMessages} 内置文案，避免 {@code msg} 仅为数字业务码。
 */
public final class I18nUtil {

    private I18nUtil() {
    }

    /**
     * 使用当前线程 {@link LocaleContextHolder} 中的 Locale 解析文案。
     */
    public static String getMessage(String code) {
        return getMessage(code, null, null);
    }

    /**
     * 使用占位参数解析消息。
     * <p>
     * 未提供可变参数重载：Java 中 {@code Object...} 与 {@code Object[]} 擦除后签名冲突，调用方请传入数组。
     */
    public static String getMessage(String code, Object[] args) {
        return getMessage(code, args, null);
    }

    /**
     * 指定调用方兜底文案。
     * <p>
     * 若 {@code defaultMessage} 非空则<b>优先使用</b>（Filter / 业务异常的具体说明），避免被 i18n 通用词条覆盖；
     * 否则走 MessageSource → {@link BizMessages} 内置词条。
     *
     * @param defaultMessage 调用方兜底（可为 {@code null}）
     */
    public static String getMessage(String code, Object[] args, String defaultMessage) {
        if (defaultMessage != null && !defaultMessage.isBlank()) {
            return defaultMessage;
        }
        String fromMs = resolveFromMessageSource(code, args);
        return BizMessages.resolve(code, args, fromMs, null);
    }

    public static Locale getLocale() {
        return LocaleContextHolder.getLocale();
    }

    public static void setLocale(Locale locale) {
        LocaleContextHolder.setLocale(locale);
    }

    private static MessageSource messageSource() {
        try {
            return SpringUtil.getBean(MessageSource.class);
        } catch (Throwable ignored) {
            return null;
        }
    }

    private static String resolveFromMessageSource(String code, Object[] args) {
        MessageSource ms = messageSource();
        if (ms == null) {
            return null;
        }
        Locale locale = LocaleContextHolder.getLocale();
        try {
            return ms.getMessage(code, args, locale);
        } catch (NoSuchMessageException ignored) {
            return null;
        } catch (Exception ignored) {
            return null;
        }
    }
}
