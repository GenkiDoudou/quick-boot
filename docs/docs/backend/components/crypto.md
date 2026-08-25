# crypto

## 用途

`quickboot-common` 能力包 `io.github.genkidoudou.common.crypto`。

源码目录：`quickboot/quickboot-common/src/main/java/io/github/genkidoudou/common/crypto/`

## 公开类型 / API

| 类型 | 相对路径 | 说明 |
| --- | --- | --- |
| BCryptPasswordCodec | crypto/BCryptPasswordCodec.java | /** * BCrypt 密码编解码器：带盐哈希，适用于用户口令存储。 */ |
| DelegatingPasswordCodec | crypto/DelegatingPasswordCodec.java | /** * 多算法委托编解码器：密文形如 &#123;@code {id}encoded}，加密使用默认 id，校验按前缀路由到子实现。 * &lt;p&gt; * 无前缀的历史存根可通过 &#123;@link #setDefaultPasswordEncoderFor |
| PasswordCodec | crypto/PasswordCodec.java | /** * 加密算法 * * @author luyanan * @since 2026/7/26 */ |
| PasswordCodecAutoConfiguration | crypto/PasswordCodecAutoConfiguration.java | /** * 密码编解码器 Spring 装配：注册默认 &#123;@link DelegatingPasswordCodec}（bcrypt + sm3）。 */ |
| PasswordCodecFactories | crypto/PasswordCodecFactories.java | /** * 密码编码器工厂 * * @author luyanan * @since 2026/7/29 */ |
| Sm3PasswordCodec | crypto/Sm3PasswordCodec.java | /** * 国密 SM3 单向摘要密码编解码器：加密为 hex 摘要，不支持解密。 */ |


## 配置键

若存在 `*Properties`，以该类字段及 `application.yml` 中对应前缀为准（见上表类型）。

## 示例

业务模块通过依赖 `quickboot-common` 直接引用上表类型；具体用法见各业务 Service / Controller。
