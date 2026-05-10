## Context

- **quick-ui**：Vue 3 + Element Plus；请求经 `@/utils/request`，**HTTP 200 + `code`** 约定；失败多为 **Promise reject**。
- **现状**：`C7Button` 已在 `src/packages/C7Button/index.vue` 落地，经 `src/packages/index.js` 的 `installPackages` 于 `main.js` 全局注册；本 design 固定**规格层语义**，供评审与后续迭代对齐。

## Goals / Non-Goals

**Goals**

- 统一 CRUD 类按钮的 **icon / 文案 / type / plain** 默认值，且允许 **显式 props 覆盖**。
- **固定顺序**流水线：`before-click` → 可选 `beforeClick` 否决 → 可选表单校验 → 可选确认 → `clickFunction` → `checkSuccess` → 成功/失败提示 → `success` / `error` → `after-click`。
- **防抖**：`lodash/debounce`，**leading: true、trailing: false**（首击立即执行，窗口内后续点击忽略）；与 **`busy`**（贯穿校验与确认阶段）及 **`internalLoading`**（仅包裹 `clickFunction`）分工明确。
- **`validateRef`**：`unref` 后为 Element Plus **`ElForm` 实例**，具备 **`validate()` 返回 Promise**（校验失败 reject，不弹错误 toast，仅 `after-click(false)`）。
- **确认**：默认 `ElMessageBox.confirm`；可选 **`confirmFn`** 返回 boolean；用户取消 → **`after-click(false)`**，不视为 **error** toast。
- **透传**：`inheritAttrs: false`，过滤 **`on*`** 监听器再 `v-bind` 到 `ElButton`，避免外层 **`@click`** 与内部流水线重复触发；**禁止**在外层对 `C7Button` 写 **`@click`**，应使用 **`clickFunction` + emit**。

**Non-Goals**

- 不实现移动端专属手势、不替代权限指令（`v-hasPermi` 等仍可包在业务外层）。
- 组件位于 **`src/packages/C7Button`**，与其它业务增强组件一并由 **`packages/index.js`** 导出、注册。

## Decisions

1. **成功提示 props**  
   - 采用 **`successMessage`** + **`successNotify`**（通知用 `ElNotification`），与原始需求「successMessage/successNotify」一致；**不使用**原始文中的 **`isSuccessCallback`** 命名（易与布尔混淆）。成功/失败回调统一为 **`success` / `error` emit**。

2. **失败提示**  
   - **`showErrorToast`**（默认 `true`）：校验失败、确认取消、`beforeClick` 否决 — **不**弹错误 toast；**reject**、`checkSuccess === false` — 按标志提示，文案优先 **`errorMessage`**。

3. **`checkSuccess`**  
   - 默认 **`() => true`**；仅在 **`clickFunction` resolve** 之后调用。

4. **预设表**  
   - `add/edit/delete/query/refresh/upload/download/submit/cancel` 映射 **label、type、plain、icon**（`@element-plus/icons-vue`）；缺失 `btnType` 时仅依赖显式 props / 默认 `type`。

## Risks / Trade-offs

| 风险 | 缓解 |
|------|------|
| 与 `request` 解包语义不一致 | 文档与 spec 写明：reject = 失败；业务码非 200 已由拦截器 reject |
| leading debounce + 长异步 | **`busy`** 在执行整段流水线期间挡重入 |
| 外层 `@click` 误用 | `inheritAttrs: false` + 过滤 `on*`；文档强调 |

## Migration Plan

- 新页面优先使用 **`C7Button`**；旧页可渐进替换。
- 若全局组件名冲突，可改为按需导入（实现变更时再评估）。

## Open Questions

- 是否在某一业务列表页 **替换一组示例按钮** 作为样板（可选，非阻塞）。
