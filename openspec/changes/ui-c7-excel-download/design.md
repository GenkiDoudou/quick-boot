## Context

- **quick-ui**：Vue 3 + Element Plus；**`axios`** 封装在 **`quick-ui/src/utils/request.js`**；**`responseType: 'blob'`** 时响应拦截器当前 **`return res.data`**，调用方**默认拿不到**响应头。
- **已有能力**：**`downloadRequest`**、**`download()`**（**`file-saver`** + **`blobValidate`**）；**`blobValidate`** 定义于 **`@/utils/ruoyi.js`**（**`data.type !== 'application/json'`**）。
- **已定稿设计**：**`docs/superpowers/specs/2026-05-08-c7-excel-download-design.md`**（brainstorming 结论：**1B、2A、3A、4A、5A、6A**；实现路径 **路径 1**：单文件 **`C7ExcelDownload/index.vue`**）。

## Goals / Non-Goals

**Goals**

- 提供 **`C7ExcelDownload`**：**`downloadFn`** → 归一 **`Blob` / `{ data, headers }`** → 文件名解析 → **`objectURL` + `<a download>`** → **`revokeObjectURL`**；**`v-model:downloading`**；**`success` / `error`**；默认 **`ElMessage`**，可 **`notify`**。
- **`request.js`**：向后兼容地支持可选 **`{ data: Blob, headers }`**，使 **`Content-Disposition`** 解析路径（设计第 4 节 2）、3））可用。
- **`packages/index.js`**：导出并全局注册 **`C7ExcelDownload`**。

**Non-Goals**

- 不强制迁移或删除 **`download()` / `$download`**。
- 不限定仅 **`.xlsx`**；不规定后端导出 URL 或查询参数形态。

## Decisions

1. **`request.js` 扩展方式（二选一，实现时择一并写入代码注释）**  
   - **A**：请求 **`config`** 增加布尔 **`returnBlobWithHeaders`**；拦截器在 **`responseType === 'blob'`** 分支：为 true 时 **`return { data: res.data, headers: res.headers }`**，否则保持 **`return res.data`**。  
   - **B**：新增 **`downloadRequestWithHeaders(url, params, config)`**，内部与 **`downloadRequest`** 同鉴权、同 **`baseURL`**，返回 **`Promise<{ data: Blob; headers }>`**（或等价命名）。  
   - **最佳推荐：A**（调用点少改、与现有 **`downloadRequest` 展开 config** 一致）；若拦截器改动风险评审偏高，可改用 **B**（tasks 中允许「实现择一」）。

2. **组件文件组织**  
   - **`parseContentDisposition`**、**`resolveFileName`** 等同文件底部 **纯函数**，除非单文件过长再局部抽取（与设计一致）。

3. **根元素与 attrs**  
   - **`inheritAttrs: false`**，根 **`ElButton`** 上 **`v-bind="$attrs"`**，避免重复绑定 **`@click`**；**`class` / `style`** 合并策略与 **`C7Copy`** 等现网组件对齐。

4. **`defaultFileName`**  
   - **建议必填**；若未传且无 **`fileName`** 且无可用头，走 **错误路径**（spec 写死）。

5. **成功时 `emit('success', fileName)` 的时机**  
   - 在 **`a.click()`** 完成且已确定最终文件名之后、**`revokeObjectURL`** 之前或之后均可，实现 **固定一种** 并在 JSDoc 说明（须与 spec「成功」语义一致）。

## Risks / Trade-offs

| 风险 | 缓解 |
|------|------|
| **拦截器返回类型由 `Blob` 变为联合类型** | 仅在有 **显式 config** 时改变返回形态；**TypeScript/ JSDoc** 标注 **`downloadRequest` 重载或注释** |
| **`Content-Disposition` 编码与浏览器差异** | 单元测试覆盖 **`filename*`** / **`filename=`**；边界在 spec 中列为验收 |
| **父组件 `v-model:downloading` 与内部竞态** | 以 **内部 `finally` 置 false** 为准；若父在过程中写入，实现阶段 **document** 边界（非本期必做复杂同步） |

## Migration Plan

- **新增组件与可选 request 行为**：无数据库迁移；存量页面 **可逐步**从 **`download()`** 迁至 **`C7ExcelDownload`**，**不**要求同一迭代内全量替换。

## Open Questions

- （无）已定稿设计已覆盖主要歧义；若实现评审在 **决策 1 的 A/B** 间切换，同步更新本 **`design.md`** 与 **`tasks.md`** 对应条。
