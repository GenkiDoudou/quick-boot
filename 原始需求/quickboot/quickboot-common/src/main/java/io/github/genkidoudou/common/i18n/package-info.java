/**
 * 国际化资源模块
 * <p>
 * 提供国际化支持,包括:
 * <ul>
 *     <li>I18nUtil - 国际化工具类,可以根据code和参数获取国际化消息</li>
 * </ul>
 * <p>
 * 使用示例:
 * <pre>
 * // 获取国际化消息
 * String message = I18nUtil.getMessage("error.user.not.found");
 * 
 * // 获取带参数的国际化消息
 * String message = I18nUtil.getMessage("error.user.age.invalid", new Object[]{18});
 * </pre>
 * <p>
 * 说明:
 * <ul>
 *     <li>本模块不覆盖 Spring Boot 的国际化配置</li>
 *     <li>通过 Hutool 的 SpringUtil 获取 MessageSource</li>
 *     <li>使用 Spring Boot 默认的国际化配置即可</li>
 * </ul>
 *
 * @author genkidoudou
 */
package io.github.genkidoudou.common.i18n;
