## 1. API 与算法（quickboot-common）

- [x] 1.1 定义 `SensitiveType` 枚举与 `@Sensitive` 注解（`type` 默认 `CUSTOM`，`strategy` 仅 CUSTOM 生效）
- [x] 1.2 实现脱敏内核：对 `String` 应用的 `NAME`/`ID_CARD`/`MOBILE`/`BANK_CARD`/`EMAIL`/`ADDRESS`/`PASSWORD`/`CUSTOM`（含「长度不足/非法 CUSTOM 原样」与 **中间段等长 `*`**、`EMAIL` 本地 `≤2` 原样等规则），附 JavaDoc 与边角样例
- [x] 1.3 绑定 Jackson：`SimpleModule` + `BeanSerializerModifier` **或** 元注解 + `@JsonSerialize`（任选其一写入实现说明），仅在 **序列化写出**替换字符串且不修改 Bean 字段

## 2. Spring Boot 集成

- [x] 2.1 提供自动配置类：通过 `Jackson2ObjectMapperBuilderCustomizer`、`ObjectMapper` `@Bean` 后处理或与 Boot 推荐的扩展点兼容，确保 Web JSON 默认 `ObjectMapper` 注册模块（`META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports` 按需登记）
- [x] 2.2 JavaDoc/模块 README 片段：说明「仅 Web 默认 Mapper」「若与其它组件共用同一 `ObjectMapper` 可能影响缓存 JSON」

## 3. 测试

- [x] 3.1 单元测试：覆盖 `MOBILE`、`CUSTOM("3,4")`、`NAME` 单字、`EMAIL`（`abcde@example.com`、`a@b.com`）、`PASSWORD`、`null`/空串/长度不足、非法 CUSTOM；断言内存字符串未被修改（序列化前后字段引用或值不变）

## 4. （可选）试点

- [ ] 4.1（可选）在 `quickboot-web` 选取一个现有查询 VO 增补 `@Sensitive` 字段并人工验 JSON（不做可留空）
