package io.github.genkidoudou.common.i18n;

import cn.hutool.extra.spring.SpringUtil;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

import java.util.Locale;

/**
 * 国际化文案工具：基于 Spring {@link MessageSource} 与 {@link LocaleContextHolder}。
 * <p>
 * 须在 Spring 容器就绪后调用（通过 Hutool {@link SpringUtil} 获取 {@link MessageSource}）。
 * 不负责词条维护；Web 语言切换由 {@code LocaleResolver} 等与 {@link LocaleContextHolder} 协作完成。
 */
public final class I18nUtil {

    private I18nUtil() {
    }

    /**
     * 使用当前线程 {@link LocaleContextHolder} 中的 Locale；词条缺失时返回 {@code code}。
     */
    public static String getMessage(String code) {
        return resolve(code, null, code);
    }

    /**
     * 使用占位参数解析消息；词条缺失时返回 {@code code}。
     * <p>
     * 未提供可变参数重载：Java 中 {@code Object...} 与 {@code Object[]} 擦除后签名冲突，调用方请传入数组。
     */
    public static String getMessage(String code, Object[] args) {
        return resolve(code, args, code);
    }

    /**
     * 指定默认兜底文案；{@code defaultMessage == null} 时等价于使用 {@code code} 作为兜底。
     */
    public static String getMessage(String code, Object[] args, String defaultMessage) {
        String fallback = defaultMessage != null ? defaultMessage : code;
        return resolve(code, args, fallback);
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

    private static String resolve(String code, Object[] args, String fallbackWhenMissing) {
        MessageSource ms = messageSource();
        Locale locale = LocaleContextHolder.getLocale();
        if (ms == null) {
            return fallbackWhenMissing;
        }
        try {
            String msg = ms.getMessage(code, args, fallbackWhenMissing, locale);
            return msg != null ? msg : fallbackWhenMissing;
        } catch (Exception ignored) {
            return fallbackWhenMissing;
        }
    }
}
