# C7Cascader（C7 级联选择器）设计说明

**日期**：2026-05-08  
**状态**：已定稿（经 brainstorming 澄清与「设计批准」）  
**依据**：`原始需求/前端/C7级联选择器.md` + Q&A 结论（**1A、2B、3A、4A、5A**）  
**方案选型**：**方案一** — 单文件封装，结构与 `C7TreeSelect` / `C7Select` 对齐（不额外抽 composable，待树系重复逻辑明显增多时再考虑抽取）。

---

## 1. 背景与目标

- **背景**：后端常返回树形数据（地区、部门、分类等），业务需要级联选择；同时要统一**整树一次加载**与**按层懒加载**两种模式，以及**字段映射**与**对外值格式**（与现有 C7 下拉/树选一致）。
- **目标**：在 **Element Plus `ElCascader`** 之上封装：**静态 `dataList` / 整树 `fetchData` / 懒加载 `fetchData`**、`resultKey` + `dataFormatter`、**`labelKey` / `valueKey` / `childrenKey`** 映射、**`multiple` + `separator`** 与 **`valueType`**、**`load-error` / `loading-change`**；其余行为与 EP 透传属性保持一致（尤其 **`emitPath` / `checkStrictly`** 下的值语义）。
- **非目标**：不替代或 fork EP 级联面板实现；不在本期承诺「任意 `emitPath` 组合下均可用英文逗号 `separator` 无损表达」（见第 7.3 节限制）。

---

## 2. 组件边界与落点

| 项 | 约定 |
|----|------|
| **组件名** | **`C7Cascader`** |
| **目录** | `quick-ui/src/packages/C7Cascader/index.vue`（单文件；与当前 `C7TreeSelect` 体量同量级） |
| **注册** | 在 `quick-ui/src/packages/index.js` 中 **export** 与 **`installPackages`** 注册（与现有 C7 包一致） |
| **与 EP 关系** | `inheritAttrs: false`；**保留字**内的 props 由本组件消费，**其余 attrs 透传**至 `ElCascader`（含 `emitPath`、`checkStrictly`、`props`、`show-all-levels` 等）。 |

---

## 3. 技术选型

- **基底**：`ElCascader`（版本与工程内 Element Plus 保持一致）。
- **懒加载**：使用 EP 支持的 **`lazy` + `lazyLoad`**；在 `lazyLoad` 回调内调用业务 **`fetchData`**，将返回的**当前层子节点扁平列表**规范为节点列表后 **`resolve`**（或 EP 要求的完成方式）。
- **命名与 C7 统一**：多选逗号串使用布尔 **`separator`**（**不使用**原始需求稿中的 `resultType` 枚举，避免与 `C7Select` / `C7TreeSelect` 分裂）。

---

## 4. 数据来源与加载时机

| 模式 | 条件 | 行为 |
|------|------|------|
| **静态** | `dataList` 或 `options` 已绑定 | **`dataList !== undefined` 时仅用 `dataList`**（与 `C7TreeSelect` 一致）；不发起 `fetchData`。数据经 `mapTree` 后作为 `options`。 |
| **整树异步** | 无静态绑定、**非懒加载**（`lazy` 不为真或未启用懒加载语义）、且提供 `fetchData` | 当 **`autoLoad === true`** 时，在 **`onMounted`** 调用 **`fetchData({ ...fetchParams })`**（**不含 `parentId`**）。响应解析规则与 `C7TreeSelect` 一致（见第 5 节）。 |
| **懒加载** | `lazy === true` 且提供 `fetchData` | 根层：首次由 EP 触发懒加载时调用 **`fetchData({ parentId: rootParentId, ...fetchParams })`**。子层：**`parentId` 取当前父节点在映射后的 `value`**（即业务 `valueKey` 字段映射结果，保证与接口约定一致）。 |

- **`rootParentId`**：类型与默认值在实现阶段与常见后端约定对齐（如 `null` / `0` / `''`）；**文档与 JSDoc 必须写清**「根节点请求使用的 `parentId`」。
- **`autoLoad`**：语义对齐 `C7TreeSelect`；若 `autoLoad=true` 但未提供 `fetchData`，**DEV** 下 **`console.warn`** 并跳过（与树选一致）。

---

## 5. 响应解析与字段映射（含 `resultKey` 语义）

- **`resultKey`**：与 `C7Select` / `C7TreeSelect` 相同 — 从 **`fetchData` 返回的 `res.data`** 上按 **lodash `get` 点路径**取列表（如 `rows`、`data.list`）；**不**表示「子节点字段名」。
- **`dataFormatter`**：在 `resultKey` 解析之后、**数组校验**之前对列表做最终整形；返回须为数组（否则按空数组处理）。
- **`labelKey` / `valueKey` / `childrenKey`**：输入侧业务树字段名；内部 **`mapTree`** 规范为 EP 所需 **`{ label, value, children }`**。静态与整树异步路径下，若子层已有 `childrenKey` 数组则递归映射；懒加载单层返回为**扁平子列表**，由组件挂到父节点 **`children`**。
- **禁用**：节点对象上 **`disabled === true`** 时映射为 EP 节点 **`disabled: true`**（与 `C7TreeSelect` 一致）。

---

## 6. 懒加载接口约定（对应 Q&A **2B、3A**）

- **入参**：**`{ parentId, ...fetchParams }`**（浅拷贝合并 `fetchParams`）；根层 **`parentId === rootParentId`**。
- **出参**：当前父节点下的 **子节点扁平数组**；元素形状与整树节点一致（含业务字段），由 **`mapTree` 的单层映射** 转为 EP 节点（子层若无下级，可不返回 `childrenKey` 或返回空数组，**叶子**语义与 EP 一致）。
- **错误**：单次 `fetchData` reject → **`load-error`**（载荷为 **`err`** 原样，与 `C7TreeSelect` 一致）；**不替业务决定**是否 `ElMessage`。
- **并发**：使用 **`fetchGeneration`** 或等价机制，忽略过期回调写入；**`loading-change`** 与 **`inFlightCount`** 语义对齐树选（内部请求进行中为 `true`，可与 attrs 传入的 `loading` **OR** 合并若 EP 支持该 prop）。

---

## 7. 对外值、`valueType` 与 `separator`（对应 Q&A **1A、4A**）

### 7.1 与 Element Plus 一致的部分

- **`emitPath` / `checkStrictly` / `multiple`**：不修改 EP 文档定义；**`v-model` 在「非 C7 适配层」上**与 EP 一致（路径数组 vs 叶子值等）。

### 7.2 `valueType`（`auto` | `string` | `number`）

- 语义对齐 **`C7TreeSelect`**：**`auto`** 时以**规范后选项数据中用于推断的样本节点**的 `value` 类型为准（实现上可取**首帧根层第一条**或数据到达后的稳定样本，与树选文档描述一致）；**标量**进出做 `String` / `Number` 转换。
- **路径形态**（`emitPath === true`）：对路径中**各层标量**做 `valueType` 约定转换；嵌套数组结构本身不变。

### 7.3 `separator` 的适用边界（重要）

- 与 **`C7Select` / `C7TreeSelect`** 相同：**`separator === true`** 时，多选对外为 **英文逗号拼接的字符串**，空选为 **`''`**；对内为 EP 所需数组结构。
- **限制**：当 **`emitPath === true`** 或 EP 内部值为**非一维标量列表**（例如**路径为嵌套数组**）时，**逗号拼接不适用或无唯一逆解析**。本期约定：
  - **实现**：在此类组合下 **`separator` 视为无效**，对外仍输出 **与 EP 一致的数组结构**；**DEV** 下 **`console.warn`** 说明原因。
  - **文档**：明确写出「**仅当 `emitPath === false` 且多选值为叶子标量数组时，推荐使用 `separator`**」，与「value 含英文逗号勿用 `separator`」并列说明。

---

## 8. 事件与加载展示

| 事件 | 说明 |
|------|------|
| **`update:modelValue`** | 对外形态经 **§7** 转换后的值。 |
| **`change`** | 载荷与 **`update:modelValue`** 一致。 |
| **`visible-change`** | 透传 EP 下拉/面板可见性。 |
| **`loading-change`** | **`true`** 表示本组件发起的 **`fetchData` 仍有未完成请求**（与 `C7TreeSelect` 一致）。 |
| **`load-error`** | **`fetchData` reject** 时触发，参数为 **`err`**。 |

---

## 9. 与 `C7TreeSelect` 的差异说明（文档与示例职责）

- **交互形态**：级联为**级联面板**，树选为**树形下拉**；二者均可能对接树形接口，但 UX 不同。
- **选型建议（写入 VitePress）**：**级联多步、层级深、每步选项少**优先 **C7Cascader**；**需在一棵树内搜索/勾选/展示复杂节点**优先 **C7TreeSelect**（表述可为建议非强制）。

---

## 10. 验收标准（与原始需求对齐）

1. **非懒加载**：无静态数据、`autoLoad=true`、`fetchData` 可用时，挂载后能自动出现 **options**（失败则 **`load-error`**，options 可为空）。
2. **懒加载**：展开节点会按 **`{ parentId, ...fetchParams }`** 调用 **`fetchData`**，子层出现预期选项。
3. **多选**：在 **§7.3 允许的组合**下，**`separator`** 对外为逗号分隔字符串；否则为数组且不静默损坏数据。
4. **字段映射**：`labelKey` / `valueKey` / `childrenKey` 在静态、整树、懒加载下均生效。

---

## 11. 实现阶段建议（非本期设计强制细节）

- **文档**：`docs/docs/frontend/components/通用组件/c7-cascader.md`（或团队约定路径）+ 可选 **`views/dev`** 演示页（与 `C7SelectE2E` 类同，按需）。
- **测试**：以演示页或单测覆盖 **整树加载**、**懒加载一层**、**`separator` + `emitPath=false`**、**`load-error`** 为主。

---

## 12. 修订记录

| 日期 | 说明 |
|------|------|
| 2026-05-08 | 初版定稿：brainstorming Q&A 1A–5A + 方案一 |
