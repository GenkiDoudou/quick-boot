# C7Descriptions（C7 描述列表）设计说明

**日期**：2026-05-07  
**状态**：已定稿（经 brainstorming 澄清与确认）  
**依据**：`原始需求/前端/C7描述列表.md` + 与调用方约定（Q&A 结论）

---

## 1. 背景与目标

详情页需以「描述列表」展示对象字段，并支持字典标签、图片、链接、复制等形态。目标：在 **`ElDescriptions`** 之上提供 **配置驱动** 的 **`C7Descriptions`**，减少重复模板代码。

---

## 2. 组件边界与对外 API

### 2.1 命名与注册

- 组件名：**`C7Descriptions`**
- 位置：`quick-ui/src/packages/C7Descriptions/index.vue`（与其它 C7 包一致）
- 在 **`quick-ui/src/packages/index.js`** 中注册并导出。

### 2.2 根节点与属性透传

- 根节点为 **`ElDescriptions`**。
- **`defineOptions({ name: 'C7Descriptions', inheritAttrs: false })`**
- 根 **`ElDescriptions` 上使用 `v-bind="$attrs"`**  
  文档说明：**除下文「显式 props」外，其余属性与 Element Plus `el-descriptions` 一致**（如 `border`、`column`、`direction`、`size` 等）。

### 2.3 显式 props（不占 attrs）

| Prop | 说明 |
|------|------|
| **`data`** | 详情对象。实现时建议默认 `{}`，避免出现 `null` 时子层取值/插槽语义不一致；若传入 `null`，以「无数据对象」处理，点路径解析结果为 `undefined`。 |
| **`items`** | 列配置数组，见第 3 节。 |
| **`defaultEmptyText`** | **非 `tag` 列**在「展示空」时的默认文案，默认 **`'暂无'`**（与原始验收口径一致）。 |

### 2.4 插槽转发

- **`title`**、**`extra`**：与 Element Plus 一致，绑定到 **`ElDescriptions`**。

---

## 3. `items[]` 列配置与渲染规则

### 3.1 公共字段

- **`prop`**：支持 **点号路径**（如 `user.name`），从 **`data`** 取值。
- **`label`**、**`span`**：及 **`el-descriptions-item`** 支持的其它合法属性，建议 **`v-bind` 到对应 item 根**（与 EP 文档对齐）。
- **`columnType`**：驱动内置渲染类型，见 3.3。
- **`formatter(value, row, item)`**：可选；在默认文本、`copy` 等需要「展示串 / 复制串」的分支中按各节约定调用。
- **`emptyText`**：覆盖 **`defaultEmptyText`**，**仅用于非 `tag` 列**的空展示。
- **`slotName`**：若 **有值** 且父组件提供了同名具名插槽，则 **优先走插槽**，不再走 `columnType` 内置渲染。

### 3.2 具名插槽作用域（已定稿）

当使用 **`item.slotName`** 时，对应具名插槽作用域为：

```text
{ row, value, item }
```

- **`row`** 与 **`data` 为同一引用**（命名贴近表格习惯）。
- **`value`**：按 **`prop`**（含点路径）从 **`data`** 解析得到的当前值。
- **`item`**：当前列配置对象。

### 3.3 `columnType` 分支

**优先级**：`slotName` + 插槽存在 → **插槽**；否则按 **`columnType`**（缺省视为 **文本**）。

| `columnType` | 行为 |
|--------------|------|
| **`tag`** | **始终挂载 `C7DictTag`**。**`:model-value`** 绑定为当前 **`prop` 解析值**。列配置中与 **`C7DictTag`** 一致的字段 **平铺在 `item` 上**：**`options`**、**`separator`**、**`showValue`**、**`max`**、**`collapse`**、**`dictType`**、**`size`**、**`effect`**、**`round`** 等（以 `C7DictTag` 实际 props 为准）。**不使用** `defaultEmptyText` / `item.emptyText`；空态、未匹配等 **完全遵循 `C7DictTag` 现有实现**（含 **`-`**）。**原始需求中的 `dictList` 不作为本组件正式 API**，文档统一写 **`options`**（与 `C7DictTag` 一致）。 |
| **`image`** | 使用 **`ElImage`**；**`src`** 使用 **`prop` 解析值**（字符串 URL）。扩展：允许 **`item.imageAttrs`** 对象 **`v-bind`** 到 **`ElImage`**（预览列表、teleport 等），首期实现以「能预览」为最低标准。 |
| **`link`** | 渲染 **`<a>`**。字段：**`linkHref`**（字符串，首期不支持函数）、**`linkText`**、**`linkTarget`**。**`linkText` 缺省**时：使用经 **`formatter`**（若有）后的展示串，否则对 **`value`** 做合理字符串化。**展示空**时走 3.4，不输出可点击链接或输出禁用样式由实现与 EP 习惯择一，**须在实现注释中写死一种**。 |
| **`copy`** / **`copyable`** | 内嵌 **`C7Copy`**。**复制文本**：优先 **`formatter`** 返回值（若为字符串可用语义），否则 **`String(value)`**（**`null`/`undefined`** 视为空串）。**`C7Copy` 的其余 props**：通过 **`item.copyProps`** 对象传入并 **`v-bind`**，避免与列公共字段命名冲突。 |
| **默认 / 其它** | 纯文本展示：先 **`formatter`**，再字符串化；**展示空**见 3.4。 |

### 3.4 「展示空」判定（仅非 `tag` 列）

用于决定是否显示 **`item.emptyText ?? defaultEmptyText`**：

- **首期**：**`null`**、**`undefined`**、**`''`** 视为展示空。
- **`[]`**：**首期不**视为空（避免与多值语义混淆）；若产品后续要求，可单列变更。

### 3.5 点路径与健壮性

- 取值使用 **`lodash-es/get`** 或与项目表格列 **一致的** 工具函数。
- **`prop` 缺失或路径不存在**：得到 **`undefined`**，走空值分支；**不向业务抛异常**。
- **未知 `columnType`**：按 **文本** 渲染；可选：**开发环境**对未知类型 **`console.warn` 一次**（实现期决定是否开启）。

---

## 4. 方案结论（实现结构）

- **推荐**：单文件 **`C7Descriptions/index.vue`** + 少量纯函数（点路径取值、是否展示空、分支渲染）。若单文件过长，再抽 **`useDescriptionCell`** 等，**首期不强制拆分**。

---

## 5. 测试与验收

- **点路径**：嵌套字段能正确显示。
- **非 `tag` 列空值**：显示 **`暂无`**（或配置的 **`defaultEmptyText` / `emptyText`**）。
- **`tag` 列**：与单独使用 **`C7DictTag`** 时行为一致（含 **`-`** 空态）。
- **各 `columnType`**：至少覆盖一条用例（含 **`copyProps`**、**`imageAttrs`** 可选 smoke）。
- **插槽**：`slotName` 插槽能拿到 **`{ row, value, item }`**，且 **`row === data`**。

---

## 6. 澄清记录（Q&A）

| 主题 | 结论 |
|------|------|
| `tag` 与 `C7DictTag` | **`columnType: 'tag'` 内部复用 `C7DictTag`**，**`options`** 等与 `C7DictTag` 对齐。 |
| 空值与「暂无」 | **`tag` 列不适用** `defaultEmptyText` / `emptyText`；**其它列**适用 **`defaultEmptyText`（默认「暂无」）** 与 **`item.emptyText`**。 |
| `ElDescriptions` 其它属性 | **`inheritAttrs: false` + 根 `v-bind="$attrs"`**，与 EP 文档一致。 |
| 插槽作用域 | **`{ row, value, item }`**，**`row` 与 `data` 同一引用**。 |

---

## 7. 自检说明（定稿前已核对）

- **无 TBD**：首期 **`linkHref` 仅为字符串**；**`[]` 非空** 已写明。
- **一致性**：**`tag`** 与 **`emptyText`** 职责不交叉；**`dictList`** 已明确不作为 API。
- **范围**：单组件 + 注册导出；**不**包含修改 **`C7DictTag`** 行为。
- **歧义**：**`copy`** 的额外参数统一走 **`copyProps`**；**`image`** 扩展走 **`imageAttrs`**。
