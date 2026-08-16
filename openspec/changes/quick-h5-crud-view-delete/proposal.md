## Why

H5 系统/监控列表缺少统一「查看」与部分「删除」：用户/角色/客户端/任务无法删，多数业务只能编辑不能只读浏览。运维在手机端需要与 PC 对齐的常用查看/删除能力。

## What Changes

- 用户、角色、客户端、定时任务：列表删除（确认 + `*:remove`）；参数核对已有删除与禁删
- 有 form 的模块：`mode=view` 只读查看；列表「查看」入口（`query`/`list` OR）
- 无 form 模块补只读页（任务/登录日志/在线/文件）；已有 `detail.vue` 保留
- **非 BREAKING**；无批量删除、无 Cron 编辑、无新权限码

## Capabilities

### New Capabilities

- `quick-h5-crud-delete`: H5 五类模块删除确认与禁删规则
- `quick-h5-crud-view`: H5 列表只读查看（form mode=view 与补只读页）

### Modified Capabilities

- （无）

## Impact

- 前端：`quick-h5/src/pages/system/**`、`pages/monitor/**`、`api/system/*`、`api/monitor/*`、`pages.json`
- 后端：原则上复用现网 remove/get；不新造权限码
- 产品设计：`docs/superpowers/specs/2026-08-16-quick-h5-crud-view-delete-design.md`
