## Why

当前系统缺少完整的字典管理闭环，导致状态枚举、业务标签与展示样式分散在前后端代码中，维护成本高且容易不一致。现在补齐字典与字典项主从能力，可以统一配置入口、缓存刷新与导出流程，降低后续业务扩展成本。

## What Changes

- 新增字典类型管理能力：新增、修改、删除、查看、导出、刷新缓存、进入字典项列表。
- 新增字典项管理能力：按 `dictType` 管理字典项，支持新增、修改、删除、查看、导出。
- 明确主从关系：字典项必须绑定字典类型，删除字典前必须校验是否仍存在字典项。
- 统一接口风格：读 `GET`、写 `POST`（不使用 `PUT/DELETE`）。
- 前端新增主从页面与路由，优先使用 `@/packages` 组件（如 `C7Button`、`C7Dialog`、`C7JsonTable`、`C7DictTag`）。
- **BREAKING**：字典相关写接口采用 `POST /update`、`POST /remove/...` 语义，调用方不得再假定 `PUT/DELETE`。

## Capabilities

### New Capabilities
- `dict-management`: 字典类型与字典项主从管理、缓存刷新与分级导出能力。

### Modified Capabilities
- 无。

## Impact

- 后端：`quickboot-web` 新增字典/字典项领域模型、Mapper、Service、Controller、缓存刷新逻辑。
- 前端：`quick-ui` 新增字典管理主从页面与路由，扩展字典 API 调用。
- 数据库：新增字典类型表与字典项表（含唯一约束、状态字段、逻辑删除与审计字段）。
- 依赖与契约：复用现有 `R`、Validation、全局异常与 `sys_normal_disable` 字典，不新增同类基础设施依赖。
