## Context

- **quick-ui**：Vue 3 + Element Plus；C7 系列组件位于 **`quick-ui/src/packages/`**，在 **`packages/index.js`** 统一导出与全局注册。
- **已定稿设计**：**`docs/superpowers/specs/2026-05-08-c7-excel-upload-design.md`**（brainstorming **1A～7A**；实现路径 **路径 1**：单文件 **`C7ExcelUpload/index.vue`**）。
- **对称组件**：**`C7ExcelDownload`** 已约定 **`v-model:downloading`**、**`notify`**、**`$attrs` 透传** 等模式；本组件 **`uploading`** 与之对齐命名与语义（导入 vs 下载）。

## Goals / Non-Goals

**Goals**

- 实现 **`C7ExcelUpload`**：隐藏 **`input[type=file]`**、校验、**`v-model:duplicateStrategy`**、**`uploadFn`**、结果展示、**`errorFileUrl`** 链接、**`v-model:uploading`**、**`reset`**、**`success` / `error`**、**`notify` / `ElMessage`**。
- **校验失败**与 **`uploadFn` reject** 的 **`emit('error')`** 边界按设计区分（见 Decisions）。

**Non-Goals**

- 不修改 **`request.js`**（导入由业务在 **`uploadFn`** 内自行调用 **`upload` / `request`**）。
- 不内置页面外壳；不解析后端 **`R`** 包装。

## Decisions

1. **策略控件**  
   - **A**：**`ElRadioGroup` + `ElRadio`**。  
   - **B**：**`ElSegmented`**（若当前 Element Plus 版本已稳定提供）。  
   - **最佳推荐：A**（兼容面广、无障碍语义清晰）；若项目其它导入页已统一 **Segmented** 可改为 **B** 并在 **`tasks`** 中写死一种。

2. **`uploadFn` 缺失**  
   - 与 **`C7ExcelDownload`** 一致：**`console.warn` + no-op**（或纯 no-op），JSDoc 写明。

3. **`defineExpose` 的 `uploading`**  
   - 暴露与内部 **`ref`** 同步的只读或同一引用，避免父组件与内部状态分叉；在 JSDoc 说明父组件 **`ref` 上调用 `reset()`** 的推荐方式。

4. **扩展名校验**  
   - 在 **`accept` 默认 `.xls,.xlsx`** 下，实现 MUST 将实际允许列表规范为 **`.xls` / `.xlsx`**（大小写不敏感策略在实现中固定并写入 JSDoc）。

## Risks / Trade-offs

| 风险 | 缓解 |
|------|------|
| **`v-model:uploading` 与父组件同时写入** | 以组件内部 **`try/finally`** 为准；复杂双向同步非本期范围，JSDoc 简述 |
| **超大文件仅前端拦截** | 依赖 **`maxSizeMb`**；后端仍应校验，组件不承诺服务端安全 |

## Migration Plan

- **新增组件**：无数据迁移；业务页按需引入或全局注册后逐步替换自建导入块。

## Open Questions

- （无）与 **`docs/superpowers/specs/2026-05-08-c7-excel-upload-design.md`** 一致；若实现阶段在 **决策 1 的 A/B** 间切换，同步更新本 **`design.md`** 与 **`tasks.md`**。
