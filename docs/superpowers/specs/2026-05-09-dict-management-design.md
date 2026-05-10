# 字典管理设计（系统管理）

## 1. 目标与范围

### 1.1 目标
本设计用于落地“字典管理 + 字典项管理”主从能力，提供可维护的数据字典体系，确保：
- 字典与字典项分层清晰，关系稳定。
- 接口契约统一（读 `GET`、写 `POST`）。
- 前后端统一使用状态字典 `sys_normal_disable`（`0/1`）。
- 缓存刷新与导出能力可用且职责明确。

### 1.2 范围
- 覆盖字典类型管理（新增、修改、删除、查看、导出、刷新缓存、进入字典项列表）。
- 覆盖字典项管理（新增、修改、删除、查看、导出）。
- 覆盖主从路由、接口契约、核心校验与异常处理约束。

### 1.3 非范围
- 不扩展多状态体系（仅 `0/1`）。
- 不引入字典版本管理、审批流或多租户隔离。
- 不修改统一响应基础契约 `R`。

## 2. 架构与边界

### 2.1 领域拆分
- `dict-type`：字典类型主表与管理能力。
- `dict-data`：字典项从表与管理能力。

### 2.2 关系约束
- 字典项必须绑定 `dictType`。
- 字典删除前必须校验是否存在未删除字典项。
- 不允许出现脱离 `dictType` 的孤立字典项。

### 2.3 分层责任
- `quickboot-common`：复用通用响应、异常与 i18n 能力。
- `quickboot-web`：Controller / Service / Mapper / 领域对象、全局异常映射。
- `quick-ui`：主从页面、路由、交互与导出触发。

## 3. 数据模型设计

### 3.1 字典类型表（建议：`sys_dict_type`）
核心字段：
- `dict_id`（主键）
- `dict_name`（字典名称）
- `dict_type`（字典类型编码）
- `status`（`0` 正常 / `1` 停用）
- `remark`
- 审计字段（创建人/创建时间/更新人/更新时间）
- 逻辑删除字段

约束：
- `dict_type` 全局唯一（逻辑删除策略下按项目约定实现唯一性）。

### 3.2 字典项表（建议：`sys_dict_data`）
核心字段：
- `dict_code`（主键）
- `dict_type`（所属字典类型）
- `dict_label`（数据标签）
- `dict_value`（数据键值）
- `css_class`（样式）
- `list_class`（回显样式）
- `dict_sort`（显示排序）
- `status`（`0` 正常 / `1` 停用）
- `remark`
- 审计字段
- 逻辑删除字段

约束：
- `dict_type + dict_value` 唯一。
- 字典项写操作必须带 `dictType`。

## 4. 接口契约设计

### 4.1 总体约定
- 读接口使用 `GET`。
- 写接口统一使用 `POST`（不使用 `PUT`/`DELETE`）。
- 入参使用 Jakarta Validation。
- 失败通过自定义异常统一映射为 `R.error(code, message)`。

### 4.2 字典类型接口
- `GET /system/dict/type/list`：字典类型列表（支持筛选）。
- `GET /system/dict/type/{dictId}`：字典类型详情。
- `POST /system/dict/type`：新增字典类型。
- `POST /system/dict/type/update`：修改字典类型。
- `POST /system/dict/type/remove/{dictId}`：删除字典类型（含从表存在校验）。
- `POST /system/dict/type/export`：导出字典类型列表。
- `POST /system/dict/type/refresh`：刷新全部字典缓存。
- `POST /system/dict/type/refresh/{dictType}`：刷新单个字典类型缓存。

### 4.3 字典项接口
- `GET /system/dict/data/list`：按 `dictType` 查询字典项列表。
- `GET /system/dict/data/{dictCode}`：字典项详情。
- `POST /system/dict/data`：新增字典项。
- `POST /system/dict/data/update`：修改字典项。
- `POST /system/dict/data/remove/{dictCode}`：删除字典项。
- `POST /system/dict/data/export`：导出当前 `dictType` 字典项。

## 5. 校验与异常处理

### 5.1 参数校验
- Controller 必须使用 `@Validated` / `@Valid`。
- 字段约束至少覆盖：必填、长度、状态枚举、排序范围。

### 5.2 业务校验
- 新增/修改字典类型时校验 `dictType` 唯一性。
- 新增/修改字典项时校验 `dictType + dictValue` 唯一性。
- 删除字典类型前校验是否存在未删除字典项。

### 5.3 异常策略
- 禁止抛 `IllegalArgumentException` 作为业务失败信号。
- 统一抛项目自定义异常（如 `WarningException`），由全局异常处理映射响应。

## 6. 缓存与导出策略

### 6.1 缓存刷新
- 提供“单个类型刷新 + 全量刷新”两种能力。
- 刷新后后续查询需读取最新缓存/数据源结果。

### 6.2 导出
- 字典类型页导出字典类型列表。
- 字典项页导出当前 `dictType` 下字典项。
- 导出范围与页面筛选条件保持一致（若传筛选）。

## 7. 前端页面与路由

### 7.1 路由结构
- `/system/dict/type`：字典类型管理页。
- `/system/dict/data/:dictType`：字典项管理页。

### 7.2 页面职责
- 字典类型页：新增、修改、删除、查看、导出、刷新缓存、进入字典项列表。
- 字典项页：新增、修改、删除、查看、导出。

### 7.3 组件策略
- 优先使用 `@/packages` 组件（如 `C7Button`、`C7Dialog`、`C7JsonTable`、`C7DictTag`）。
- 权限点建议：
  - `system:dict:list`、`system:dict:query`、`system:dict:add`、`system:dict:edit`、`system:dict:remove`、`system:dict:export`、`system:dict:refresh`

## 8. 测试与验收

### 8.1 后端验收
- 字典类型 CRUD、字典项 CRUD 可用。
- 删除字典类型时，存在字典项则拒绝并返回可读 `msg`。
- 单类型刷新与全量刷新均可执行。
- 统一返回 `R` 契约。

### 8.2 前端验收
- 主从页面可正确跳转并透传 `dictType`。
- 页面操作完整：查看/新增/修改/删除/导出/刷新。
- 状态显示与筛选使用 `sys_normal_disable`。

### 8.3 集成验收
- 删除受限、导出范围、缓存刷新结果与业务预期一致。

## 9. 风险与缓解
- 风险：历史数据导致唯一性冲突。
  - 缓解：上线前执行数据巡检与冲突清洗。

- 风险：缓存刷新与查询一致性短时抖动。
  - 缓解：刷新后采用统一缓存更新策略并记录操作日志。

- 风险：前端页面误用接口语义。
  - 缓解：在 API 层集中封装并固定调用方式。

## 10. 结论
采用“主从双页 + 强绑定 `dictType` + 统一 `POST` 写接口 + 双刷新 + 分级导出 + packages 优先”的方案，可在不破坏现有契约的前提下稳定落地字典管理能力。
