# C7ViewSwitch（C7 视图切换容器）设计说明

**日期**：2026-05-08  
**状态**：已定稿（经 brainstorming 澄清与「整稿确认」）  
**依据**：`原始需求/前端/C7视图切换容器.md` + Q&A 结论（1B、2A、3A、4A、5A、6A）及实现路径 **路径 1**（单文件 `index.vue`）

---

## 1. 背景与目标

- **背景**：部分页面需在多个「视图 / 表单步骤」间切换（如列表 → 新增 → 编辑 → 详情），希望由**单一容器**统一管理切换、返回、标题区与过渡动画。
- **目标**：提供基于 **`v-model`**（视图名 `string`）的切换容器：按 **`views`** 配置与当前值渲染**具名插槽**；内部维护**历史栈**，提供 **`goBack`**；可选 **`ElPageHeader`**；过渡可关闭或自定义。
- **非目标**：与 **`vue-router`** 的深度绑定或自动同步；复杂「浏览器级」前进 / 后退（仅栈式上一视图）。

---

## 2. 命名、边界与落点

| 项 | 约定 |
|----|------|
| **对外组件名** | **`C7ViewSwitch`**（与 `C7Switch` 开关区分） |
| **目录** | `quick-ui/src/packages/C7ViewSwitch/index.vue` |
| **实现形态** | **单文件组件**；栈与渲染同处一处。若后续栈逻辑显著膨胀（经验阈值约 **80+ 行**），再抽离 `useViewStack` 等 composable（**本期不做**）。 |
| **全局注册** | 在 **`quick-ui/src/packages/index.js`** 中 **`export`** 与 **`installPackages`** 注册，与其它 C7 一致。 |

**职责**：按配置与 `modelValue` 渲染对应插槽；维护内部历史栈；暴露 **`switchTo` / `goBack`** 及只读栈快照；可选页头与过渡；未匹配视图时 **`not-found` + `#empty` 兜底**。

**不负责**：路由跳转、权限；业务表单的校验与提交（由槽内页面自行处理）。

---

## 3. 视图配置与别名

- **主配置 `views`**：`Array<ViewConfig>`，其中 **`ViewConfig`** 至少包含：
  - **`name: string`**：视图标识，与 `v-model` 比较；具名插槽名为 **`#<name>`**。
  - **`title?: string`**：可选；供 `ElPageHeader` 标题等展示。
  - **`closeIndex?: string`**：可选；**`goBack` 且历史栈为空**时，作为回落目标视图名（须能在 `views` 中匹配，否则按「无回落」处理并走 **`back-empty`**）。
- **兼容别名 `showIndexs`**：与 **`views` 同结构、同语义**。若二者**同时**传入，以 **`views` 为准**（与原始需求「views 优先」一致）。

---

## 4. 历史栈与 `v-model` 语义（2A）

- **栈内元素**：历史 **`modelValue` 字符串**（视图名），栈底为较早、栈顶为较近；**仅**由 **`switchTo`**、**`goBack`** 修改（push / pop）。
- **`switchTo(targetName)`**  
  - 若 **`views` 中无 `name === targetName`**： **`emit('not-found', targetName)`**，**不**更新 `modelValue`，**不**改栈。  
  - 若 **`targetName` 与当前 `modelValue` 相同**：**no-op**（不入栈、不 `emit('update:modelValue')`、不 `emit('change')`）。  
  - 否则：将**当前** `modelValue` **压入栈顶**，再 **`emit('update:modelValue', targetName)`**，并 **`emit('change', targetName, matchedConfig)`**（`matchedConfig` 为匹配到的 `ViewConfig` 对象引用或等价只读形态，实现计划里写死）。
- **`goBack()`**  
  - 若栈**非空**：弹出栈顶作为上一视图名，**`emit('update:modelValue', previousName)`**，并 **`emit('back', previousName, previousConfig)`**（载荷与 `change` 对齐策略一致）。  
  - 若栈**为空**：依次尝试 **当前**匹配项的 **`closeIndex`**、组件 **`defaultView` prop**（`string`，须为合法视图名）；若存在且与当前不同，则 **`emit('update:modelValue', …)`** 并 **`emit('change', …)`**（视为一次导航）。若仍无法回落：**`emit('back-empty')`**，**不改变** `modelValue`（3A）。  
  - **`back-empty` 载荷**：**无参数**（保持简单；若业务需要当前上下文，可读 `modelValue` / `ref.currentConfig`）。
- **父组件直接修改绑定（`v-model` / `modelValue`）**：仅切换当前展示视图，**不入栈、不出栈**；历史栈**保持不变**（2A）。  
  - **说明**：此后栈可能与业务「真实路径」不一致；业务在父级强行改视图时，应自行约定一致性（后续**可选**暴露 **`clearHistory()`**，**本期不列入必做**；若实现成本极低可在实现阶段顺带暴露，以代码评审为准）。

---

## 5. 生命周期与事件（5A）

| 事件 | 触发条件 |
|------|----------|
| **`update:modelValue`** | 任意导致 `modelValue` 合法更新的路径。 |
| **`change(viewName, config)`** | **`modelValue` 变为新值且与旧值不同**时；**不含**首次挂载时的初始同步（5A）。来源含 **`switchTo`**、**`goBack`**、父级直接改绑定、空栈回落成功等。 |
| **`back(viewName, config)`** | **`goBack`** 且**栈非空**、已成功回到上一视图时（载荷为**回到的那一屏**的视图名与配置，实现计划与 JSDoc 写清）。 |
| **`not-found(viewName)`** | **`switchTo`** 指向未配置视图名。 |
| **`back-empty`** | **`goBack`**（或页头触发等价逻辑）时栈空，且**无**有效 **`closeIndex` / `defaultView`** 回落。 |

**模板监听**：Vue 3 中 `emit` 在脚本侧使用 camelCase（如 **`backEmpty`**）时，模板仍写 **`@back-empty`**；设计文档以 **kebab-case** 描述用户侧监听名。

---

## 6. 过渡（4A）

- **`transition` prop**：类型 **`boolean | string`**。  
  - **`false`**：不包裹 **`<Transition>`**，无过渡。  
  - **`true`**：使用组件约定默认 **`name`**（如 **`c7-view-switch`**，实现时写死常量）。  
  - **`string`**：作为 **`<Transition :name="transition">`** 的 `name`，供业务自定义 CSS 过渡类名。

---

## 7. 插槽

| 插槽 | 说明 |
|------|------|
| **`#<view.name>`**（具名，scoped） | 参数：**`{ config, switchTo, goBack }`**，与原始需求一致；`config` 为当前视图 **`ViewConfig`**。 |
| **`#header-content`** | 可选 `ElPageHeader` 的 **content** 区域（与 EP 插槽名对齐方式在实现计划中对照当前 EP 版本文档）。 |
| **`#empty`** | **`modelValue` 无法在 `views` 中匹配**时渲染；**若无此插槽则空白占位**（6A）。 |

---

## 8. 可选 `ElPageHeader`

- **`showPageHeader`**：`boolean`，默认 **`false`**。为 **`true`** 时渲染 **`ElPageHeader`**。  
- **标题**：优先当前匹配项的 **`title`**；若无 **`title`**，使用**空字符串或占位文案**二选一（实现计划里选定一种并写入 JSDoc，避免未定义行为）。  
- **页头「返回」**：触发与 **`goBack()`** 相同的逻辑（含空栈回落与 **`back-empty`**）。

---

## 9. 其它 Props 与暴露

- **`defaultView?: string`**：见第 4 节空栈回落。  
- **`defineExpose`**：**`switchTo`**、**`goBack`**、**`currentConfig`**（当前匹配 `ViewConfig` 或 `null`）、**`viewHistory`**（栈的**只读快照**，如返回新数组或 `readonly`，禁止外部直接 `mutate` 内部栈）。

---

## 10. 错误处理与测试要点

- **`not-found`**：仅非法 **`switchTo`**。  
- **`back-empty`**：空栈且无有效回落。  
- **建议测试**：栈 push/pop 顺序；`switchTo` 同目标 no-op；父级改 `v-model` **不入栈**；首次挂载**无** `change`；未匹配 + 有/无 **`#empty`**；`transition` 三态；`showPageHeader` 返回与 **`goBack`** 一致。

---

## 11. 文档与集成

- **VitePress**：在 `docs/` 侧增加与其它 C7 一致的组件说明页（路径随现有文档结构），含基础示例与「父级直接改 `v-model` 与栈」的说明（**本期纳入实现计划任务项**）。

---

## 12. 与原始需求（C7SwitchForm）对照

| 原始项 | 定稿 |
|--------|------|
| 组件名 C7SwitchForm | **C7ViewSwitch**（避免与 `C7Switch` 混淆） |
| `views` / `showIndexs` | **保留**，优先级 **`views`** |
| 事件 / 插槽 / 暴露能力 | **对齐**；新增明确语义：**`back-empty`**、**`switchTo` 同值 no-op**、**首次不 `change`** |
