# config

## 用途

`quickboot-common` 能力包 `io.github.genkidoudou.common.config`。

源码目录：`quickboot/quickboot-common/src/main/java/io/github/genkidoudou/common/config/`

## 公开类型 / API

| 类型 | 相对路径 | 说明 |
| --- | --- | --- |
| ConfigValueLookup | config/ConfigValueLookup.java | /** * 按配置键读取参数值（由 system 等域提供实现；他域可选注入）。 */ |


## 配置键

若存在 `*Properties`，以该类字段及 `application.yml` 中对应前缀为准（见上表类型）。

## 示例

业务模块通过依赖 `quickboot-common` 直接引用上表类型；具体用法见各业务 Service / Controller。
