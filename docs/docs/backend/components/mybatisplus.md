# mybatisplus

## 用途

`quickboot-common` 能力包 `io.github.genkidoudou.common.mybatisplus`。

源码目录：`quickboot/quickboot-common/src/main/java/io/github/genkidoudou/common/mybatisplus/`

## 公开类型 / API

| 类型 | 相对路径 | 说明 |
| --- | --- | --- |
| BaseBaseMapper | mybatisplus/BaseBaseMapper.java | /** * 项目 Mapper 标记接口：继承 MyBatis-Plus &#123;@link BaseMapper}，便于统一扫描与扩展。 * * @param &lt;T&gt; 实体类型 */ |
| BaseServiceImpl | mybatisplus/BaseServiceImpl.java | /** * MyBatis-Plus Service 基类：封装分页查询、Entity/VO 拷贝等常用能力。 * * @param &lt;M&gt; Mapper 类型 * @param &lt;T&gt; 实体类型 */ |


## 配置键

若存在 `*Properties`，以该类字段及 `application.yml` 中对应前缀为准（见上表类型）。

## 示例

业务模块通过依赖 `quickboot-common` 直接引用上表类型；具体用法见各业务 Service / Controller。
