# security

## 用途

`quickboot-common` 能力包 `io.github.genkidoudou.common.security`。

源码目录：`quickboot/quickboot-common/src/main/java/io/github/genkidoudou/common/security/`

## 公开类型 / API

| 类型 | 相对路径 | 说明 |
| --- | --- | --- |
| FirewallCorsAutoConfiguration | security/firewall/cors/FirewallCorsAutoConfiguration.java | /** * &#123;@code qc.security.firewall.cors.enabled=true} 时注册 CORS Filter。 * &lt;p&gt;移植自 bak &#123;@code FirewallCorsAutoConfiguration} |
| FirewallCorsProperties | security/firewall/cors/FirewallCorsProperties.java | /** * 安全防火墙：CORS 跨域配置，绑定前缀 &#123;@code qc.security.firewall.cors}。 * &lt;p&gt;移植自 bak &#123;@code FirewallCorsProperties}。 */ |
| LoginUserService | security/service/LoginUserService.java | /** * 当前登录用户 SPI：由 sa-token 等安全模块提供实现。 */ |
| LoginUserUtils | security/utils/LoginUserUtils.java | /** * 登录用户上下文工具：从 Spring 容器解析 &#123;@link LoginUserService}。 */ |
| LoginUser | security/vo/LoginUser.java | /** * 当前登录用户（sa-token 会话解析结果）。 */ |


## 配置键

若存在 `*Properties`，以该类字段及 `application.yml` 中对应前缀为准（见上表类型）。

## 示例

业务模块通过依赖 `quickboot-common` 直接引用上表类型；具体用法见各业务 Service / Controller。
