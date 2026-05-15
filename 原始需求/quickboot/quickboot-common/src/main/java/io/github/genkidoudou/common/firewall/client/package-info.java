/**
 * 客户端管理模块
 * <p>
 * 提供客户端认证和管理功能，支持配置文件和数据库两种实现方式
 * </p>
 *
 * <h2>主要功能</h2>
 * <ul>
 *     <li>客户端认证：通过 clientId 和 clientSecret 进行认证</li>
 *     <li>密钥管理：支持 SM2 公私钥和签名密钥管理</li>
 *     <li>多数据源：支持配置文件（默认）和数据库两种实现</li>
 *     <li>灵活配置：支持排除URL、自定义Header名称等</li>
 * </ul>
 *
 * <h2>使用方式</h2>
 *
 * <h3>1. 配置文件方式（默认）</h3>
 * <pre>
 * qc:
 *   security:
 *     firewall:
 *       client:
 *         enabled: true
 *         source: config
 *         clients:
 *           - client-id: app001
 *             client-secret: secret001
 *             public-key: xxx
 *             sign-key: xxx
 *             enabled: true
 * </pre>
 *
 * <h3>2. 数据库方式</h3>
 * <pre>
 * qc:
 *   security:
 *     firewall:
 *       client:
 *         enabled: true
 *         source: database
 * </pre>
 * <p>
 * 注意：数据库实现需要在业务模块中自行实现 {@link io.github.genkidoudou.common.firewall.client.ClientService} 接口
 * </p>
 *
 * <h2>核心类</h2>
 * <ul>
 *     <li>{@link io.github.genkidoudou.common.firewall.client.ClientService} - 客户端服务接口</li>
 *     <li>{@link io.github.genkidoudou.common.firewall.client.OauthClient} - 客户端实体</li>
 *     <li>{@link io.github.genkidoudou.common.firewall.client.ClientAuthFilter} - 客户端认证过滤器</li>
 *     <li>{@link io.github.genkidoudou.common.firewall.client.impl.ConfigClientServiceImpl} - 配置文件实现</li>
 * </ul>
 *
 * @author luyanan
 * @since 2026-03-04
 */
package io.github.genkidoudou.common.firewall.client;
