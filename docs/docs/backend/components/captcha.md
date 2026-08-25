# captcha

## 用途

`quickboot-common` 能力包 `io.github.genkidoudou.common.captcha`。

源码目录：`quickboot/quickboot-common/src/main/java/io/github/genkidoudou/common/captcha/`

## 公开类型 / API

| 类型 | 相对路径 | 说明 |
| --- | --- | --- |
| CaptchaConfigController | captcha/CaptchaConfigController.java | /** * 登录页验证码开关（不受 &#123;@code qc.captcha.enabled} 条件装配影响，关闭时也返回 false）。 */ |
| CaptchaController | captcha/CaptchaController.java | /** * 天爱行为验证码：生成与校验（与前端 TAC SDK 默认契约一致）。 * * @author genkidoudou * @since 1.0.0 */ |
| CaptchaProperties | captcha/CaptchaProperties.java | /** * 业务侧验证码开关与类型；其余项见 Spring Boot 下 &#123;@code captcha.*}（tianai 官方前缀）。 * * @author genkidoudou * @see &lt;a href="https://doc |
| CaptchaPropertiesAutoConfiguration | captcha/CaptchaPropertiesAutoConfiguration.java | /** * 无论验证码开关如何，都绑定 &#123;@link CaptchaProperties}，并暴露 &#123;@code /api/captcha/config}。 */ |
| TianaiCacheStoreAutoConfiguration | captcha/TianaiCacheStoreAutoConfiguration.java | /** * 在无 Redis 或显式指定 &#123;@code qc.captcha.store=local} 时使用内存 &#123;@link CacheStore}，避免与 tianai 自带 Redis 装配冲突。 * &lt;p&gt; * 本类必须在 &#123;@l |


## 配置键

若存在 `*Properties`，以该类字段及 `application.yml` 中对应前缀为准（见上表类型）。

## 示例

业务模块通过依赖 `quickboot-common` 直接引用上表类型；具体用法见各业务 Service / Controller。
