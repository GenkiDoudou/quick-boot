## Context

- **quick-ui**：Vue 3 + Element Plus；列表/详情附件多为 **逗号分隔 URL**，各页自行拼 **`el-image` / `el-dialog` / `window.open`** 行为不一致。
- **已定稿设计**：`docs/superpowers/specs/2026-05-08-c7-preview-design.md`（brainstorming 澄清 **1B～6B**）。
- **proposal**：新增 **`C7Preview`** + **`parseUrls` / `inferMediaKind`** + **`packages/index.js`** 注册。

## Goals / Non-Goals

**Goals**

- **路径 2**：`C7Preview/index.vue` + **`parseUrls.ts`** + **`inferMediaKind.ts`**；模板内不写解析/推断细节。
- **视频弹窗**：**`C7Dialog`**、**`footer=false`**；**`<video controls autoplay>`**；关闭 **pause + `currentTime=0`** + **`emit('close')`**。
- **图片**：**`preview-src-list`** 顺序与 **`parsedUrls` 中图片子序列**一致；**`initial-index`** 为当前图在该子序列中的下标；**`button`** 模式用 **隐藏 `el-image` + ref 触发预览**（与当前 **element-plus** 版本 API 对齐，JSDoc 注明依赖版本行为）。
- **钩子顺序**：**`await` 归一化 `onPreview` → 若允许则 `emit('preview')` → 再打开 UI**；拦截时 **不** **`emit('preview')`**。
- **`coverType=file`**：**`el-table`** 极简 + 行点击与同索引 **`none`** 等价行为。

**Non-Goals**

- 首版 **不** 支持 **`urls: string[]`**；不实现 Office 预览、上传、转码；不在组件内发鉴权请求。

## Decisions

1. **`inferMediaKind`**：从 URL 取 **pathname**，**去掉 query/hash 后再取扩展名**；扩展名 **大小写不敏感**；集合与 **已定稿设计** 一致（image / video / file 列表）。
2. **`autoDetect=false`**：每条按 **`displayType`** 渲染；**不**做 URL 与类型冲突的自动纠正（JSDoc 警告调用方）。
3. **`coverType=button` 混合集合**：单次点击优先级 **有图 → 仅图片预览（`initial-index=0`）**；**无图有视频 → 第 7 节弹窗**；**仅 file → 打开索引 0**（见 spec）。
4. **空 `urls` 解析结果**：实现 **二选一写死**——**不渲染触发区** 或 **`button` 禁用**；须在 **tasks** 勾选并在 E2E/单测之一覆盖。
5. **`C7Dialog`**：仅 **dialog** 模式使用（不强制抽屉）；标题 **`videoDialogTitle`**；内容区自定义 **上一条/下一条**（多视频时）。

## Risks / Trade-offs

| 风险 | 缓解 |
|------|------|
| **Element Plus 大图预览无法程序化打开** | 以 **隐藏 `el-image` + `click()`** 为主方案；若 EP 升级 API 变，在实现 PR 中按官方文档调整并更新 JSDoc |
| **`window.open` 被浏览器拦截** | 组件 **不**包一层「假下载」；JSDoc 说明须在用户手势链内调用 |
| **混合类型下 `button` 只进图片预览** | spec 已写明；文档示例提示业务拆分数据或使用 **`none`/`file`** |

## Migration Plan

- **纯新增组件**；旧页零强制迁移，可逐步替换手写预览块。

## Open Questions

- （无）**`imageProps`** 是否在首版实现由 **tasks** 勾选项决定（设计允许可选透传）。
