# 后端开发规范

本文是仓库 `sdd/后端代码规范.md` 的**文档站摘要**，开发时以源码与 SDD 全文为准。

## 分层与包结构

```text
controller → service → mapper → domain
         ↘ dto (Bo 入参 / Vo 出参)
```

- **Controller**：只做参数校验、权限注解、调用 Service；补齐 `@Tag`、`@Operation`。
- **Service**：业务逻辑、事务边界。
- **Mapper**：MyBatis-Plus，XML 放在 `mapper/**/*.xml`。
- **Bo**：`@Validated(AddGroup.class)` / `UpdateGroup` 分组校验。
- **Vo**：返回给前端，敏感字段配合 `@Sensitive` 脱敏。

## 接口约定

| 项 | 约定 |
|----|------|
| HTTP 方法 | 修改/删除优先 `@PostMapping`，路径表达语义（如 `/remove`、`/update`） |
| 响应 | 统一 `R<T>`，HTTP 常为 200，以 `code` 判断成败 |
| 分页 | `PageRequest` + `PageInfo` |
| 异常 | 使用 `WarningException` / `ErrorException`，**禁止**用 `IllegalArgumentException` 表示业务失败 |
| 对象转换 | `BeanUtil.copyProperties()` |

## 持久化

- 主键：雪花 `assign_id`（`mybatis-plus.global-config`）。
- 逻辑删除字段：`del_flag`（1 删除 / 0 正常）。
- 库表变更：**仅通过 Flyway** `db/migration/V*.sql`，禁止手工改库不同步脚本。

## 配置与安全

- 敏感配置：Jasypt `ENC(...)`，启动参数 `-Djasypt.encryptor.password=...`。
- 认证：Sa-Token，Header `Authorization: Bearer <token>`。
- 调用方鉴权：OAuth Client **HMAC**（见 [OAuth2 集成](./modules/oauth2)）。
- 横切能力：优先复用 `quickboot-common`（Excel、防火墙、操作日志等），避免在 web 重复实现。

## 代码生成

新增标准 CRUD 模块可先用 [代码生成](./modules/codegen) 产出骨架，再按规范手工调整 Bo/Vo 与权限标识。

## 验证清单

- [ ] 新接口已在 Swagger 可见且 Bo 校验完整
- [ ] Flyway 脚本可重复执行、与实体字段一致
- [ ] 未将 `client_secret` 等敏感信息写入日志
- [ ] `mvn -pl quickboot-web -am test` 或至少 `mvn -pl quickboot-web package` 通过

## 延伸阅读

- [项目结构](./structure)
- [接口规范](../backend/api/index)
- 仓库 `sdd/后端代码规范.md`、`openspec/project.md`
