## Why

H5 首页快捷入口仍为 mock，无法跳转且与角色无关；工作台已按权限下发全量菜单。首页需要「默认常用 + 个人可配」的轻入口，与工作台分工，避免假数据误导运维。

## What Changes

- 新建表 `sys_user_h5_home_shortcut` 存储个人偏好（menu_id + 顺序）
- 后端：候选池复用 H5 工作台过滤逻辑；`GET` 最终宫格 / 候选；`POST .../save` 全量保存（空数组恢复默认）；**不用 PUT/DELETE**
- H5：首页接真快捷 +「编辑」设置页；消息/待办保留 mock 壳
- **非 BREAKING**

## Capabilities

### New Capabilities

- `quick-h5-home-shortcuts-api`: 首页快捷偏好存储、默认解析与 GET/POST 接口
- `quick-h5-home-shortcuts-ui`: 首页宫格真跳转与个人快捷设置页

### Modified Capabilities

- （无）

## Impact

- 后端：`quickboot-module-system`（Entity/Mapper/Service/Controller）、Flyway
- 前端：`quick-h5` `pages/home/*`、`api/system/menu.ts`、`pages.json`
- 产品设计：`docs/superpowers/specs/2026-08-16-quick-h5-home-shortcuts-design.md`
- 依赖现网：`h5Workbench` / `sys_menu` path `/pages/` 约定
