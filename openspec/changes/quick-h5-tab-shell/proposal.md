## Why

登录脚手架（change `quick-h5`）已落地，但移动端仍缺与产品确认的三 Tab 信息架构：首页（快捷入口 / 消息 / 今天待办）、工作台（后台可配置菜单）、我的（个人信息 / 联系我们 / 关于·清缓存 / 退出）。需要按已确认 design 把壳层 IA 固化为可验收能力，与登录底座解耦。

## What Changes

- 在 `quick-h5/` 配置 `tabBar`：首页 / 工作台 / 我的。
- **首页**：快捷入口、消息列表、今天待办（首版本地 mock，非 bak 任务域）。
- **工作台**：按「后台可配置菜单」结构渲染分组宫格；首版 `mock` 下发（`groups[] → items[]`），本 change **不实现**管理端配置页与真实 API。
- **我的**：入口列表 + 子页（个人信息 / 联系我们 / 关于）；清缓存默认保留登录态；退出登录。
- 更新/对齐 `docs/superpowers/specs/2026-08-12-quick-h5-design.md` 与 README 页面说明；静态原型 `docs/demo/quick-h5-tab-prototype.html` 作为对照。
- 不引入 bak 任务/分类/四象限业务页；不修改后端 OAuth / 登录协议。

## Capabilities

### New Capabilities

- `quick-h5-tab-shell`: 三 Tab 壳层信息架构、mock 数据约定、我的子页与清缓存/退出行为。

### Modified Capabilities

- （无；登录与 OAuth client 仍由 change `quick-h5` 覆盖，本 change 不改其 requirement。）

## Impact

- 前端：`quick-h5/src/pages.json`、`pages/home`、`pages/workbench`、`pages/mine/*`、`src/mock/*`、`static/tab/*`。
- 后端：无（菜单 API / 管理端配置为后续 change）。
- 依赖：无新包；沿用现有 uni-app + uView Pro。
- 参考：`docs/superpowers/specs/2026-08-12-quick-h5-design.md`、`docs/demo/quick-h5-tab-prototype.html`。
