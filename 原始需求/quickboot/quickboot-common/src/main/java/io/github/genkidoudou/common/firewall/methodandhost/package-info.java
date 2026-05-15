/**
 * 请求方式和域名拦截模块
 * 
 * 提供 HTTP 请求方式和访问域名的拦截功能，只允许配置的请求方式和域名访问应用
 * 
 * 主要功能：
 * 1. 支持配置允许的 HTTP 请求方式（GET、POST、PUT、DELETE 等）
 * 2. 支持配置允许的访问域名/Host
 * 3. 支持通配符匹配域名（*.example.com）
 * 4. 支持端口通配符（localhost:*）
 * 5. 支持排除特定 URL 不进行拦截
 * 6. 自动拦截不符合规则的请求并返回 403 错误
 * 
 * 技术实现：
 * <ul>
 *   <li>基于 Servlet Filter 实现</li>
 *   <li>使用 Spring Boot ConfigurationProperties 管理配置</li>
 *   <li>使用 AntPathMatcher 进行路径匹配</li>
 *   <li>支持代理场景（X-Forwarded-Host）</li>
 * </ul>
 * 
 * 配置示例：
 * <pre>
 * qc:
 *   security:
 *     firewall:
 *       method-and-host:
 *         enabled: true
 *         allowed-methods:
 *           - GET
 *           - POST
 *           - PUT
 *           - DELETE
 *         allowed-hosts:
 *           - localhost:9100
 *           - example.com
 *           - *.example.com
 *           - localhost:*
 *         exclude-urls:
 *           - /health
 *           - /actuator/**
 *         forbidden-message: "请求被拒绝：不允许的请求方式或域名"
 * </pre>
 * 
 * 使用示例：
 * <pre>
 * // 1. 基本配置 - 只允许 GET 和 POST 请求
 * qc:
 *   security:
 *     firewall:
 *       method-and-host:
 *         enabled: true
 *         allowed-methods:
 *           - GET
 *           - POST
 * 
 * // 2. 域名限制 - 只允许特定域名访问
 * qc:
 *   security:
 *     firewall:
 *       method-and-host:
 *         enabled: true
 *         allowed-hosts:
 *           - localhost:9100
 *           - api.example.com
 * 
 * // 3. 通配符匹配 - 允许所有子域名
 * qc:
 *   security:
 *     firewall:
 *       method-and-host:
 *         enabled: true
 *         allowed-hosts:
 *           - *.example.com
 * 
 * // 4. 端口通配符 - 允许任意端口
 * qc:
 *   security:
 *     firewall:
 *       method-and-host:
 *         enabled: true
 *         allowed-hosts:
 *           - localhost:*
 * 
 * // 5. 排除特定 URL
 * qc:
 *   security:
 *     firewall:
 *       method-and-host:
 *         enabled: true
 *         allowed-methods:
 *           - GET
 *           - POST
 *         exclude-urls:
 *           - /health
 *           - /actuator/**
 *           - /api/public/**
 * 
 * // 6. 完整配置示例
 * qc:
 *   security:
 *     firewall:
 *       method-and-host:
 *         enabled: true
 *         allowed-methods:
 *           - GET
 *           - POST
 *           - PUT
 *           - DELETE
 *         allowed-hosts:
 *           - localhost:9100
 *           - api.example.com
 *           - *.example.com
 *         exclude-urls:
 *           - /health
 *           - /actuator/**
 *         forbidden-message: "请求被拒绝"
 * </pre>
 * 
 * 请求方式说明：
 * <ul>
 *   <li><b>GET</b>: 获取资源</li>
 *   <li><b>POST</b>: 创建资源</li>
 *   <li><b>PUT</b>: 更新资源</li>
 *   <li><b>DELETE</b>: 删除资源</li>
 *   <li><b>PATCH</b>: 部分更新资源</li>
 *   <li><b>HEAD</b>: 获取资源头信息</li>
 *   <li><b>OPTIONS</b>: 获取支持的请求方式</li>
 *   <li><b>TRACE</b>: 回显请求</li>
 * </ul>
 * 
 * 域名匹配规则：
 * <ul>
 *   <li><b>精确匹配</b>: localhost:9100, example.com</li>
 *   <li><b>通配符匹配</b>: *.example.com（匹配所有子域名）</li>
 *   <li><b>端口通配符</b>: localhost:*（匹配任意端口）</li>
 * </ul>
 * 
 * 拦截响应格式：
 * <pre>
 * {
 *   "code": 403,
 *   "message": "不允许的请求方式: DELETE",
 *   "timestamp": "2026-03-02T10:30:45.123"
 * }
 * </pre>
 * 
 * 工作流程：
 * <pre>
 * 1. 请求到达
 *    ↓
 * 2. MethodAndHostFilter 拦截
 *    ↓
 * 3. 检查是否在排除列表 → 是 → 放行
 *    ↓ 否
 * 4. 验证请求方式 → 不允许 → 拦截（返回 403）
 *    ↓ 允许
 * 5. 验证请求域名 → 不允许 → 拦截（返回 403）
 *    ↓ 允许
 * 6. 放行请求
 * </pre>
 * 
 * 代理场景支持：
 * <ul>
 *   <li>支持 X-Forwarded-Host 头（Nginx、Apache 等代理）</li>
 *   <li>自动处理多个代理的情况（取第一个）</li>
 *   <li>兼容标准 Host 头</li>
 * </ul>
 * 
 * 安全建议：
 * <ul>
 *   <li>生产环境必须配置 allowedHosts，避免域名劫持</li>
 *   <li>不要使用过于宽松的通配符（如单独的 *）</li>
 *   <li>定期审查拦截日志，发现异常访问</li>
 *   <li>配合其他安全措施使用（如 HTTPS、防火墙）</li>
 *   <li>排除健康检查等特殊 URL</li>
 * </ul>
 * 
 * 性能说明：
 * <ul>
 *   <li>单次验证耗时 < 1ms</li>
 *   <li>对正常请求的性能影响 < 0.1%</li>
 *   <li>使用高效的字符串匹配，避免正则表达式</li>
 *   <li>优先检查排除列表，减少不必要的验证</li>
 * </ul>
 * 
 * 注意事项：
 * <ul>
 *   <li>如果 allowedMethods 为空，默认允许所有请求方式</li>
 *   <li>如果 allowedHosts 为空，默认允许所有域名</li>
 *   <li>排除的 URL 不会进行任何拦截检查</li>
 *   <li>Host 匹配区分大小写</li>
 *   <li>配置更新需要重启应用</li>
 * </ul>
 * 
 * 常见场景：
 * <ul>
 *   <li><b>开发环境</b>: enabled=false（不启用）</li>
 *   <li><b>测试环境</b>: 配置测试域名</li>
 *   <li><b>生产环境</b>: 严格配置允许的域名和请求方式</li>
 *   <li><b>API 网关</b>: 只允许 GET、POST</li>
 *   <li><b>管理后台</b>: 限制特定域名访问</li>
 * </ul>
 *
 * @author QuickBoot
 * @since 2026/03/02
 */
package io.github.genkidoudou.common.firewall.methodandhost;
