# exception

## 用途

`quickboot-common` 能力包 `io.github.genkidoudou.common.exception`。

源码目录：`quickboot/quickboot-common/src/main/java/io/github/genkidoudou/common/exception/`

## 公开类型 / API

| 类型 | 相对路径 | 说明 |
| --- | --- | --- |
| BaseException | exception/BaseException.java | /** * 统一异常基类，携带业务错误码、默认文案与国际化占位参数。 * &lt;p&gt; * 约定：&#123;@code code} 为空时会兜底为 &#123;@link HttpCodes#INTERNAL_ERROR}，用于保证响应链路始终有可用错误码。 */ |
| ErrorCodes | exception/ErrorCodes.java | /** * 异常体系业务码常量。 * &lt;p&gt; * 分段规则：1xxxx（通用）/ 2xxxx（业务）/ 3xxxx（安全）/ 4xxxx（系统）。 * 其中部分安全码直接复用既有 &#123;@link HttpCodes} 以避免冲突与双份定义。  |
| ErrorException | exception/ErrorException.java | /** * 严重异常：用于系统内部故障、关键依赖失败等需要按 5xx 语义处理的场景。 */ |
| WarningException | exception/WarningException.java | /** * 可预期异常：用于业务校验失败、安全拦截等可被调用方理解和处理的场景。 * &lt;p&gt; * 约定： * &lt;ul&gt; * &lt;li&gt;&#123;@link #WarningException(Integer, Object...)} — 第二参及之后 |


## 配置键

若存在 `*Properties`，以该类字段及 `application.yml` 中对应前缀为准（见上表类型）。

## 示例

业务模块通过依赖 `quickboot-common` 直接引用上表类型；具体用法见各业务 Service / Controller。
