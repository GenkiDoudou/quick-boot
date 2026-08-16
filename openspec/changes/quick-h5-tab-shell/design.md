## Context

- Change `quick-h5` 已提供登录、HTTP Basic/Bearer、`/auth/me` 与极简 home/mine。
- 产品已确认 Tab IA（见 `docs/superpowers/specs/2026-08-12-quick-h5-design.md` §4.0；静态原型 `docs/demo/quick-h5-tab-prototype.html`）。
- 仓库内可能已有部分壳层实现；本 design 以可验收行为为准，apply 时以对照补齐 / 校验为主。

## Goals / Non-Goals

**Goals:**

- 落地三 Tab 壳与我的子页，结构与 mock 形状对齐「后台可配置菜单」。
- 保持与登录脚手架解耦：不改 OAuth client / `/login` 协议。

**Non-Goals:**

- 工作台菜单管理端配置页、真实菜单 API、权限过滤。
- 消息 / 待办 / 快捷入口真实业务域与后端。
- bak 任务域页面移植。

## Decisions

1. **原生 `tabBar`（非自定义 tab 组件）**  
   - 理由：H5 / 小程序路径一致，成本低。  
   - 备选：自定义 tabBar（更灵活，但首版过重）。

2. **Mock 模块 `src/mock/*`，页面只消费数据结构**  
   - 理由：后续替换 API 时改数据源即可，UI 少动。  
   - 备选：写死在 `.vue`（更快但难替换）。

3. **工作台菜单形状：`groups[] → items[]`**  
   - 字段至少：`id`、`title`/`label`、展示用 short/icon、预留 `path`。  
   - 理由：对齐 design「后台下发」约定。  
   - 备选：扁平列表（不利于分组配置）。

4. **清缓存默认保留 token**  
   - 仅清应用缓存键（如 `quick_h5_app_cache`），不 `clearStorage` 全量。  
   - 备选：清后强制重登（需产品明确再改）。

5. **我的子页用 `navigateTo`，Tab 根页用 `switchTab`/`reLaunch` 进首页**  
   - 登录成功仍进入首页 Tab；子页不进 `tabBar.list`。

## Risks / Trade-offs

- [Mock 被当成真实能力] → README / 工作台 tip 标明 mock；菜单点击仅 toast。  
- [与未归档 `quick-h5` change 页面冲突] → Tab 壳以本 change 为准增量改 `pages.json`；登录任务仍归 `quick-h5`。  
- [tabBar 图标缺失导致启动失败] → 提供 `static/tab/*.png` 占位图标。

## Migration Plan

- 纯前端包内变更；无 DB migration。  
- 回滚：还原 `pages.json` 与相关页面 / mock 即可。

## Open Questions

- 快捷入口是否与工作台菜单同源配置：首版各自 mock，后续再统一（不阻塞本 change）。
