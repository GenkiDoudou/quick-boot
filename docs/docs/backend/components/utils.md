# utils

## 用途

`quickboot-common` 能力包 `io.github.genkidoudou.common.utils`。

源码目录：`quickboot/quickboot-common/src/main/java/io/github/genkidoudou/common/utils/`

## 公开类型 / API

| 类型 | 相对路径 | 说明 |
| --- | --- | --- |
| ServletUtils | utils/ServletUtils.java | /** * Servlet 工具（基于 Hutool &#123;@link JakartaServletUtil}，适配 Spring Boot 3+/Jakarta）。 */ |


## 配置键

若存在 `*Properties`，以该类字段及 `application.yml` 中对应前缀为准（见上表类型）。

## 示例

业务模块通过依赖 `quickboot-common` 直接引用上表类型；具体用法见各业务 Service / Controller。
