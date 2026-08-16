## Why

quick-h5 用户列表 meta 区目前手写多组 `qb-row` / `qb-col` / `qb-kv`，调字段布局要改模板。PC 侧 C7JsonTable 已用 JSON 配列；H5 需要同等「改配置即改布局」的轻量能力，先从卡片字段区落地。

## What Changes

- 新增 `QbJsonCardFields`：按 `columns` JSON 渲染卡片 meta（`prop` / `label` / `span` / `kv` / `type`）
- 用户管理列表改用 `cardColumns` 配置驱动字段展示；搜索、操作按钮仍手写
- 支持 `text` / `dict`（options）/ `slot`；`showIfProp` 控制空值列隐藏
- **非 BREAKING**：不引入完整 C7（无 searchColumns / listFunction / 导入导出）

## Capabilities

### New Capabilities

- `quick-h5-json-card-fields`: H5 列表卡片 meta 区 JSON 列配置渲染组件及用户列表样板接入

### Modified Capabilities

- （无）

## Impact

- 前端：`quick-h5/src/components/qb/QbJsonCardFields.vue`、`pages/system/user/index.vue`
- 依赖产品设计：`docs/superpowers/specs/2026-08-16-quick-h5-json-card-list-design.md`
- 复用现有：`qb-row` / `qb-col-*` / `qb-kv` 工具类、`QbListCard`、`QbDictTag`
