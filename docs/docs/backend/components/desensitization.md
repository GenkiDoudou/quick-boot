# desensitization

## 用途

`quickboot-common` 能力包 `io.github.genkidoudou.common.desensitization`。

源码目录：`quickboot/quickboot-common/src/main/java/io/github/genkidoudou/common/desensitization/`

## 公开类型 / API

| 类型 | 相对路径 | 说明 |
| --- | --- | --- |
| Sensitive | desensitization/Sensitive.java | /** * 标记需在 &lt;b&gt;JSON 序列化&lt;/b&gt; 时脱敏的 &#123;@link String} 字段或 getter； * &lt;b&gt;不会修改&lt;/b&gt; 内存中的属性值。 * &lt;p&gt; * 实际掩码还须当前请求命中带 &#123;@link Sensitive |
| SensitiveJacksonAutoConfiguration | desensitization/SensitiveJacksonAutoConfiguration.java | /** * Jackson 字段脱敏自动装配：注册 &#123;@link SensitiveJacksonModule}， * Spring Boot Web 默认经由 &#123;@code JsonMapperBuilderCustomizer} 合并进 |
| SensitiveJacksonModule | desensitization/SensitiveJacksonModule.java | /** * Jackson 模块：注册 &#123;@link SensitiveBeanSerializerModifier}， * 使 &#123;@link Sensitive} 在 Spring MVC JSON 写出链路生效。 * &lt;p&gt; * &lt;b&gt; |
| SensitiveMasking | desensitization/SensitiveMasking.java | /** * 字符串掩码算法：供 Jackson 序列化使用，&#123;@code input} 本身不会被改写（返回新字符串）。 * &lt;p&gt; * 规则与 OpenSpec &#123;@code common-field-desensitization} 对 |
| SensitiveResponse | desensitization/SensitiveResponse.java | /** * 标记「本接口响应需要字段脱敏」。 * &lt;p&gt; * 仅当当前请求命中的 Controller 方法（或类）带有本注解时， * 字段上的 &#123;@link Sensitive} 才会在 JSON 序列化时生效；否则原样输出，避免编辑回显 |
| SensitiveResponseContext | desensitization/SensitiveResponseContext.java | /** * 判断当前请求是否启用字段脱敏（是否命中 &#123;@link SensitiveResponse}）。 * &lt;p&gt; * 单元测试可通过 &#123;@link #enableForTest()} / &#123;@link #clear()} 绕过 Ser |
| SensitiveType | desensitization/SensitiveType.java | /** * &#123;@link Sensitive} 内置脱敏类型（仅对 &#123;@link String} 序列化生效）。 */ |


## 配置键

若存在 `*Properties`，以该类字段及 `application.yml` 中对应前缀为准（见上表类型）。

## 示例

业务模块通过依赖 `quickboot-common` 直接引用上表类型；具体用法见各业务 Service / Controller。
