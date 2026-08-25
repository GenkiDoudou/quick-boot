# validation

## 用途

`quickboot-common` 能力包 `io.github.genkidoudou.common.validation`。

源码目录：`quickboot/quickboot-common/src/main/java/io/github/genkidoudou/common/validation/`

## 公开类型 / API

| 类型 | 相对路径 | 说明 |
| --- | --- | --- |
| AddGroup | validation/group/AddGroup.java | /** * 新增场景参数校验分组。 */ |
| UpdateGroup | validation/group/UpdateGroup.java | /** * 修改场景参数校验分组。 */ |
| ValidatorUtils | validation/ValidatorUtils.java | /** * Jakarta Bean Validation 静态入口，委托 Spring 容器中的 &#123;@link Validator} 执行分组校验。 */ |


## 配置键

若存在 `*Properties`，以该类字段及 `application.yml` 中对应前缀为准（见上表类型）。

## 示例

业务模块通过依赖 `quickboot-common` 直接引用上表类型；具体用法见各业务 Service / Controller。
