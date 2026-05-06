## 1. 类型与常量（quickboot-common）

- [x] 1.1 在 `quickboot-common` 新增 `R<T>`：`code`、`msg`、`data`、`traceId`、`timestamp` 字段；实现 `ok` / `error` 全量工厂方法与 `isSuccess` / `isError`；构造或序列化时写入非空 `timestamp`（毫秒）；`traceId` 从 MDC（键名与 `logback`/`traceId` 约定对齐）读取，无则按设计处理为 null 或空字符串并加 JavaDoc 说明
- [x] 1.2 新增 HTTP 语义业务码常量类（含 200、400、401、403、404、500、503 等）；`R.ok` 默认 `code` 与该常量一致
- [x] 1.3 新增 `PageRequest<T>`：`current` 默认 1、`size` 默认 10，对 `size` 使用 `@Min(1)`（或等价校验）与 JavaDoc；实现 `getOffset()` 为 `(current - 1) * size`
- [x] 1.4 新增 `PageInfo<T>`：`current`、`size`、`records`、`total`、`pages`、`ext`；提供 `pages` 计算 `(total + size - 1) / size`（在 `size>=1` 前提下）；可选新增 `from(IPage<T>)` 或静态工厂以降低 Service 重复代码（与设计 Open Question 对齐后二选一实现）

## 2. 测试与文档

- [x] 2.1 单元测试：`R` 工厂与 `isSuccess`/`isError`；`PageRequest` 默认值与 `getOffset()`；`PageInfo` 总页数样例（如 total=23,size=10→pages=3）；必要时对 MDC 中有/无 `traceId` 各测一例
- [x] 2.2 对照 `openspec/changes/common-response-paging/specs/common-response-paging/spec.md` 自查或通过测试覆盖各 Requirement 场景；若项目要求同步 `docs/` 或 AGENTS 指向的 common 能力说明，增补「HTTP 200 + body.code」与分页契约一句

## 3. 可选联调

- [ ] 3.1（可选）选取 `quickboot-web` 中一个只读列表接口改为 `R<PageInfo<...>>` 入参 `PageRequest`，Service 内转换为 MyBatis-Plus `Page` 并回填 `PageInfo`，用于端到端验证 JSON 字段名
