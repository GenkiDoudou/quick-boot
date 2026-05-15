/**
 * XSS 脚本注入拦截模块
 * <p>
 * 检测并拦截请求参数和 body 中的 XSS 脚本，防止跨站脚本攻击。
 * <p>
 * 配置前缀：{@code qc.security.firewall.xss}
 * <p>
 * 核心类：
 * <ul>
 *   <li>{@link io.github.genkidoudou.common.firewall.xss.XssProperties} - 配置属性</li>
 *   <li>{@link io.github.genkidoudou.common.firewall.xss.XssFilter} - XSS 检测过滤器</li>
 *   <li>{@link io.github.genkidoudou.common.firewall.xss.XssUtils} - 检测工具类</li>
 *   <li>{@link io.github.genkidoudou.common.firewall.xss.XssConfiguration} - 自动配置</li>
 * </ul>
 *
 * @author genkidoudou
 * @since 2026/03/06
 */
package io.github.genkidoudou.common.firewall.xss;
