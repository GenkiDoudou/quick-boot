/**
 * 安全头管理模块
 * <p>
 * 为 HTTP 响应添加安全相关头，防止点击劫持、XSS、MIME 嗅探等攻击。
 * <p>
 * 配置前缀：{@code qc.security.headers}
 * <p>
 * 核心类：
 * <ul>
 *   <li>{@link io.github.genkidoudou.common.firewall.headers.SecurityHeaderProperties} - 配置属性</li>
 *   <li>{@link io.github.genkidoudou.common.firewall.headers.SecurityHeaderFilter} - 安全头过滤器</li>
 *   <li>{@link io.github.genkidoudou.common.firewall.headers.SecurityHeaderConfiguration} - 自动配置</li>
 * </ul>
 *
 * @author genkidoudou
 * @since 2026/03/06
 */
package io.github.genkidoudou.common.firewall.headers;
