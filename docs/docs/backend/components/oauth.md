# oauth

## 用途

`quickboot-common` 能力包 `io.github.genkidoudou.common.oauth`。

源码目录：`quickboot/quickboot-common/src/main/java/io/github/genkidoudou/common/oauth/`

## 公开类型 / API

| 类型 | 相对路径 | 说明 |
| --- | --- | --- |
| ClientBasicAuthenticationFilter | oauth/ClientBasicAuthenticationFilter.java | /** * 客户端校验(在未登录的状态下校验) * * @author luyanan * @since 2026/7/27 */ |
| ClientBasicPasswordCodes | oauth/ClientBasicPasswordCodes.java | /** * OAuth Client Basic 凭证编解码：Base64 URL + XOR 混淆，用于 Authorization 头传输 &#123;@code clientId:clientSecret}。 */ |
| OauthClientProperties | oauth/config/OauthClientProperties.java | /** * OAuth 客户端模块配置，前缀 &#123;@code qc.oauth}。 */ |
| OauthClientAutoConfiguration | oauth/OauthClientAutoConfiguration.java | /** * OAuth 客户端自动配置：注册 &#123;@link ClientBasicAuthenticationFilter} 与 &#123;@code clientBasic} 编解码器。 */ |
| OauthClientVo | oauth/OauthClientVo.java | /** * 客户端 * * @author luyanan * @since 2026/7/27 */ |
| OauthServiceSupport | oauth/OauthServiceSupport.java | /** * OAuth 客户端数据访问 SPI：由 system 模块基于 &#123;@code sys_oauth_client} 实现。 */ |
| OauthClientUtils | oauth/utils/OauthClientUtils.java | /** * OAuth 客户端工具：读取当前请求客户端、生成 Basic 头、判断是否需验证码。 */ |


## 配置键

若存在 `*Properties`，以该类字段及 `application.yml` 中对应前缀为准（见上表类型）。

## 示例

业务模块通过依赖 `quickboot-common` 直接引用上表类型；具体用法见各业务 Service / Controller。
