package io.github.genkidoudou.common.i18n;

import cn.hutool.extra.spring.SpringUtil;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

import java.util.Locale;

/**
 * 国际化工具类
 * 可以根据code和参数获取国际化消息
 *
 * @author genkidoudou
 * @since 2026/03/05
 */
public class I18nUtil {
    /**
     * 根据code获取国际化消息
     *
     * @param code 消息code
     * @return 国际化消息
     * @since 2026/03/05
     */
    public static String getMessage(Integer code) {
        return getMessage(code + "", null);
    }

    /**
     * 根据code获取国际化消息
     *
     * @param code 消息code
     * @return 国际化消息
     * @since 2026/03/05
     */
    public static String getMessage(String code) {
        return getMessage(code, null);
    }

    /**
     * 根据code和参数获取国际化消息
     *
     * @param code 消息code
     * @param args 参数
     * @return 国际化消息
     * @since 2026/03/05
     */
    public static String getMessage(String code, Object[] args) {
        return getMessage(code, args, code);
    }

    /**
     * 根据code、参数和默认消息获取国际化消息
     *
     * @param code           消息code
     * @param args           参数
     * @param defaultMessage 默认消息
     * @return 国际化消息
     * @since 2026/03/05
     */
    public static String getMessage(String code, Object[] args, String defaultMessage) {
        return getMessage(code, args, defaultMessage, LocaleContextHolder.getLocale());
    }

    /**
     * 根据code、参数、默认消息和语言环境获取国际化消息
     *
     * @param code           消息code
     * @param args           参数
     * @param defaultMessage 默认消息
     * @param locale         语言环境
     * @return 国际化消息
     * @since 2026/03/05
     */
    public static String getMessage(String code, Object[] args, String defaultMessage, Locale locale) {
        try {
            // 通过 Hutool 的 SpringUtil 获取 MessageSource
            MessageSource messageSource = SpringUtil.getBean(MessageSource.class);
            if (messageSource == null) {
                return defaultMessage;
            }
            return messageSource.getMessage(code, args, defaultMessage, locale);
        } catch (Exception e) {
            return defaultMessage;
        }
    }

    /**
     * 获取当前语言环境
     *
     * @return 当前语言环境
     * @since 2026/03/05
     */
    public static Locale getCurrentLocale() {
        return LocaleContextHolder.getLocale();
    }

    /**
     * 设置当前语言环境
     *
     * @param locale 语言环境
     * @since 2026/03/05
     */
    public static void setCurrentLocale(Locale locale) {
        LocaleContextHolder.setLocale(locale);
    }
}
