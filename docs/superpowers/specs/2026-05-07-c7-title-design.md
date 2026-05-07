# C7Title（C7 标题）设计说明

**日期**：2026-05-07  
**状态**：已定稿（经 brainstorming 澄清与「设计批准」）  
**依据**：`原始需求/前端/C7标题.md` + Q&A 结论（1B、2A、3A、4A、5A、6A）

---

## 1. 背景与目标

页面区块标题需统一：**字号、加粗、底部装饰线、左侧图标、右侧操作区**，且可通过 **语义标签** 与 **CSS 变量** 控制装饰色。

---

## 2. 组件边界

- **职责**：独立用于**页面 / 表单分块**标题条；**不**承担 Card 容器职责。
- **与 `C7Card`**：**解耦**，不抽取共享子组件；允许与 Card 在 **CSS 变量命名习惯** 上对齐（如 `--c7-title-decoration-color`），**不要求**改动 `C7Card`。
- **非目标**：路由、权限、数据请求；右侧内容由默认插槽完全由业务提供。

---

## 3. 技术选型（已定稿）

- **标题节点**：使用 **`ElText`**，`:tag="resolvedTag"`，与 `C7Card` 标题语义路径一致。
- **图标**：`import * as ElementPlusIconsVue from '@element-plus/icons-vue'`，按 **PascalCase 字符串** 动态解析；未找到组件时 **`console.warn`** 且不渲染图标。
- **底部线**：**装饰线与 `showBorder` 为同一视觉元素**；`showBorder === false` 时不显示。

---

## 4. 对外 API

### 4.1 命名与注册

- 组件名：**`C7Title`**
- 路径：`quick-ui/src/packages/C7Title/index.vue`
- 在 **`quick-ui/src/packages/index.js`** 中导出并 **`installPackages`** 注册。

### 4.2 根节点与透传

- 根节点：**`div.c7-title`**（或带 BEM 根类名的容器），**不**使用会吞掉布局的单一 EP 表单控件作为根。
- **`defineOptions({ name: 'C7Title', inheritAttrs: true })`**：除下文显式 props 外，**`class` / `style` 等** 绑定根节点，便于页面级微调。

### 4.3 Props

| Prop | 类型 / 约束 | 说明 |
|------|----------------|------|
| **`tag`** | `h1`~`h6`、`div`、`p` | 语义标签，**默认 `h4`**（与 `C7Card` 默认标题层级对齐）。 |
| **`labelSize`** | `h1`~`h6` 或含单位的字符串（如 `20px`、`1.2rem`、`1.2em`） | 若为 **`h1`~`h6`**：应用预设 **字号 / 行高**（及实现内与层级一致的排版）；且当调用方 **未显式传入 `tag`**（即使用默认值）时，**将 `resolvedTag` 设为与 `labelSize` 相同层级**（1B）。若调用方 **显式传入** `tag`，**不**因 `labelSize` 为 h 级而改写 `tag`。若为 **自定义单位字符串**：仅设置 **`font-size`**（及合理 **`line-height`**），**不改变** `tag`。非法或无法解析的字符串：**开发环境 `console.warn`**，字号回退为与默认 `tag` 对应的预设或实现约定基线。 |
| **`label`** | `string` | 主文案（6A）。 |
| **`title`** | `string` | **兼容别名**，语义同 **`label`**。**若 `label` 与 `title` 同时传入**：**仅以 `label` 为准**；若仅在开发环境检测到二者均有非空值，可 **`console.warn`** 一次。 |
| **`decorationColor`** | `string` | **优先**；用于底部线 / 装饰色，写入根节点 CSS 变量（见 4.5）。 |
| **`labelColor`** | `string` | **兼容别名**：仅当 **`decorationColor` 未传**（`undefined`）时参与计算有效颜色。 |
| **`showBorder`** | `boolean`，默认 **`true`** | **`true`** 显示底部线；**`false`** 不显示（2A）。 |
| **`icon`** | `string`（EP 图标 PascalCase 名） | 与 **`#icon`** 二选一优先级见插槽节。 |

**字重**：区块标题 **固定 `font-weight: 600`**，**不**增加 `bold` prop（YAGNI）。

### 4.4 插槽

| 插槽 | 说明 |
|------|------|
| **`icon`** | 若存在： **完全覆盖** `icon` 字符串解析结果，由业务自行放置 `el-icon` 等。 |
| **`title`** | 若存在：**覆盖** `label` / `title` prop 的文本渲染（支持富标题、多节点）。 |
| **默认** | **右侧 actions 区**；布局上使用 **flex**，**`margin-left: auto`** 或等价使主标题 + 图标靠左、操作区靠右。 |

**图标显隐**：存在 **`#icon`** 或 **`icon` 解析出有效组件** 时显示图标区；否则不占位（避免空槽影响对齐时可不渲染图标容器）。

### 4.5 CSS 变量（验收：装饰线颜色可控）

根节点设置至少：

- **`--c7-title-decoration-color`**：有效值为 **`decorationColor` ?? `labelColor` ?? `var(--el-color-primary)`**（5A）。

底部线（与装饰合一）使用该变量；**高度约 `2px`**，**宽度 100%**（相对根容器）。

---

## 5. 布局结构（逻辑）

```text
[根 .c7-title]
  [行：flex align-items center]
    [可选 图标区]
    [ElText：resolvedTag + 字号/加粗]
    [默认插槽：actions，ml-auto]
  [可选 底部线：showBorder]
```

---

## 6. `labelSize` 预设表（实现约束）

- 实现侧维护 **`h1`~`h6` → font-size / line-height** 映射表；**数值应对齐 Element Plus 文档中标题层级或设计稿**，并在组件 JSDoc 中注明「与 EP 标题 token 对齐」或给出具体 rem/px。
- **自定义单位** 分支不修改 **`tag`**，仅覆盖字号相关样式。

---

## 7. 无障碍

- 标题层级由 **`resolvedTag`** 决定，勿为装饰目的滥用 `h1`。
- 右侧操作区为 **`div`** 包裹默认插槽即可；插槽内控件须自带可访问名称（与 `C7Card` 的 `extra` 约定一致）。

---

## 8. 验收标准（与原始需求对齐）

- 不同 **`labelSize`**（`h1`~`h6` 与 `px`/`rem`/`em`）下标题字号/行高表现正确。
- **`decorationColor` / `labelColor`** 优先级正确；底部线颜色可通过根上 **CSS 变量** 在 DevTools 中验证。
- **`icon` + `#icon` 覆盖**、**`#title` 覆盖**、**默认插槽右侧** 行为符合第 4 节。
- **`showBorder`** 控制底部线显隐。

---

## 9. 测试建议（实现阶段）

- 组件级或 Story：**`labelSize`** 各分支；**颜色优先级**；**`showBorder`**；**有效 / 无效 `icon` 字符串**；**`#title` / `#icon` / 默认插槽** 组合；**`label`+`title` 同时传** 仅认 `label`。

---

## 10. 澄清结论索引

| 题号 | 选项 | 含义摘要 |
|------|------|----------|
| 1 | B | `labelSize` 为 h 级时默认同步 `tag`，可被显式 `tag` 覆盖 |
| 2 | A | 装饰线与 `showBorder` 为同一元素 |
| 3 | A | 与 `C7Card` 独立，最多共享变量命名 |
| 4 | A | PascalCase 动态映射 icons，未知 warn |
| 5 | A | 默认主色变量，线高约 2px |
| 6 | A | `label` 为主文案，`title` 别名；`#title` 可覆盖 |
