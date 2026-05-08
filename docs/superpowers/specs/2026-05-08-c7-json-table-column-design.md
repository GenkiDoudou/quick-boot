# C7JsonTableColumn（C7 JSON 表格列）设计说明

**日期**：2026-05-08  
**状态**：已定稿（经 brainstorming 澄清 **1B、2B、3B、4A、5A** 与「设计确认」）  
**依据**：`原始需求/前端/C7JSON表格列.md`、`原始需求/前端/C7JSON表格.md`（边界参考）  
**实现路径**：首版 **单文件 `index.vue`**（`v-for` + 按 `columnType` 分支）；若逻辑膨胀再抽 **`normalizeColumns.ts`**。

---

## 1. 背景与目标

- **背景**：列表列渲染重复（字典标签、图片预览、链接、具名 slot 等），与父级 **C7JsonTable** 的 **`tableColumns[]`** 方向一致。
- **目标**：提供 **`C7JsonTableColumn`**：根据 **`columns`** 生成 **`ElTableColumn`** 集合；内置 **`columnType`** 渲染；支持 **`#header-${prop}`** 列头插槽；可在 **任意 `ElTable`** 中单独使用，也可被未来 **C7JsonTable** 引用。
- **非目标**：搜索区、分页、列持久化、批量操作；**多选列 / 序号列**（由父级自行声明 **`el-table-column type="selection"`** 等）；树表懒加载实现细节。

---

## 2. 命名、边界与落点

| 项 | 约定 |
|----|------|
| **对外组件名** | **`C7JsonTableColumn`** |
| **目录** | **`quick-ui/src/packages/C7JsonTableColumn/index.vue`** |
| **根节点** | **多个 `el-table-column`**（Vue 3 **Fragment 多根**），父级写法：`<el-table><C7JsonTableColumn :columns="cols" />…</el-table>`。 |
| **全局注册** | **`quick-ui/src/packages/index.js`**：**`export`** + **`installPackages`**。 |

**职责**：列过滤、排序、常用属性透传、按 **`columnType`** 渲染单元格与列头转发。

**不负责**：表格数据请求、行选择状态、列显隐持久化（属 JsonTable 或其它父级）。

---

## 3. 对外 Props

| Prop | 类型 | 说明 |
|------|------|------|
| **`columns`** | `Array` | **必填**；列描述对象见 **第 4 节**。非法非数组：不渲染列，**开发环境 `console.warn`**。 |
| **`emptyText`** | `string` | **可选**；表级默认空文案，供 **`columnType==='text'`** 且列上 **未写 `emptyText`** 时使用。 |

---

## 4. 列描述对象

### 4.1 过滤与排序

- **`visible !== false`** 的列才渲染（默认展示）。
- **`order`**：**数值升序**；**未设置 `order` 的列排在后面**；**`order` 相同**时保持 **`columns` 数组中的原始相对顺序**（稳定排序）。

### 4.2 透传到 `el-table-column`

以下字段若存在则绑定到 **`el-table-column`**（与 Element Plus 一致）：

**`prop`、`label`、`width`、`minWidth`、`fixed`、`align`、`headerAlign`、`sortable`、`showOverflowTooltip`**

**`props`（需求名）**：表示「其余列级属性」对象，实现时建议内部变量名 **`columnProps`** 或文档明确 **`props` 键**，以 **`v-bind` 合并**到 **`el-table-column`**，用于前表未列出的 EP 列属性（如 **`class-name`**、**`resizable`** 等，以 EP 文档为准）。

### 4.3 `columnType`（默认 **`text`**）

| `columnType` | 行为 |
|--------------|------|
| **`text`**（默认） | 若配置了 **`formatter`**：签名与 EP 一致 **`(row, column, cellValue, index) => string`**，**直接传给 `el-table-column` 的 `formatter`**，**不在此路径上自动套 `emptyText`**（**4A**）。若 **未配置 `formatter`**：按 **`prop`** 取 **`cellValue`**；若为空（**`null` / `undefined` / ''`**；**不含**仅空白字符串，除非后续 JsonTable 另有约定——本文档固定为 **三者**）则展示 **列级 `emptyText` ?? 表级 `emptyText`（第 3 节）?? `'-'`**（实现可统一为常量，须在代码注释与本文保持一致）。 |
| **`tag`** | **`C7DictTag`**：**`modelValue`** = 行字段值（**`prop` 必填**）；**`options`** = **`col.options ?? col.dictList`**（**`options` 优先于 `dictList`**，**2B**）。其它 **`C7DictTag`** 属性（如 **`dictType`、`separator`、`showValue`、`max`**）允许在列对象上同名透传，或通过 **`columnProps`/扩展字段** 传入（实现以「少魔法、文档示例为准」）。 |
| **`image`** | **`C7Preview`**：**`urls`** 绑定 **`row[prop]`**（**string**，与 **C7Preview** 首版逗号分隔一致）；**`coverType='none'`**；**`autoDetect`** 默认 **`true`**（与 **C7Preview** 默认一致即可）。单元格内缩略尺寸通过 **C7Preview 已有 props**（如 **`width`/`height`/class**）在列对象扩展字段上传入（文档给出一组推荐默认值）。 |
| **`link`** | **`<a>`**。**`linkHref` / `linkText`**： **`string`** 或 **`(row, column, cellValue, index) => string`**（**3B**）。**`linkTarget`**：首版仅 **`string`**（如 **`_blank`**），不传则 **不设 `target`**。**解析后 `href` 为空串 / `null` / `undefined`**：不渲染可点击链接，展示 **`-`**（与 **text** 空占位视觉一致）。 |
| **`slot`** | 具名 slot 名：**`slotName || prop`**（二者至少其一有值，否则 **dev warn** 且该列不渲染内容区）。作用域与 **`el-table-column` 默认 `#default`** 一致：**`{ row, column, $index }`**（**5A**）。 |

**未知 `columnType`**：按 **`text`** 处理，**开发环境 `console.warn`**。

### 4.4 列头 slot

- 命名：**`header-${prop}`**（对应需求 **`#header-[prop]`**）。
- **前提**：列必须具备 **`prop`**；无 **`prop`** 的列 **不支持** 该具名插槽（列头仅 **`label`** 或 EP 默认行为）。

---

## 5. 插槽转发约定

- **单元格（`slot` 类型）**：**`C7JsonTableColumn`** 在对应 **`el-table-column`** 的 **`#default`** 内 **`<slot :name="slotName" v-bind="scope" />`**，其中 **`slotName = col.slotName || col.prop`**。
- **列头**：若父级提供了 **`#header-${prop}`**，则在 **`el-table-column` 的 `#header`** 中转发 **`v-bind="headerScope"`**；**`headerScope` 以 Element Plus 当前 **`ElTableColumn` header 插槽** 类型为准**（实现阶段以 EP 类型定义或文档为准写入 JSDoc）。若未提供该插槽，则使用 **`label`** 作为默认列头文本（有 **`label`** 时）。

### 5.1 父组件用法示例（语义）

```vue
<el-table :data="rows">
  <C7JsonTableColumn :columns="columns" :empty-text="'—'">
    <template #header-username="{ column }">…</template>
    <template #action="{ row, $index }">…</template>
  </C7JsonTableColumn>
</el-table>
```

（具体 **`header` 作用域字段名** 以实现时 EP 版本为准，不在此伪代码中写死错误字段。）

---

## 6. `text` 与 `formatter` 的互斥语义（实现必守）

- **存在 `formatter`**：**完全遵循 EP**，不在组件内对返回值再做「空值改 `emptyText`」处理。
- **不存在 `formatter`**：走 **第 4.3 节 `text`** 的空值与 **`emptyText` / 表级默认 / `'-'`** 规则。

---

## 7. 与依赖组件的契约

- **C7DictTag**：仅只读展示；列配置 **`options`/`dictList`** 须为数组；不匹配行为以 **C7DictTag** 既有逻辑为准。
- **C7Preview**：表格内 **`coverType` 不为 `button`**，避免嵌套表格内再叠「仅按钮」交互造成理解成本；若业务需要按钮式预览，在 **`slot`** 列自行组合。

---

## 8. 错误处理与边界

| 场景 | 行为 |
|------|------|
| **`columns` 非数组** | 不渲染；**dev warn**。 |
| **`columns` 为空数组** | 无列，合法。 |
| **`slot` 类型但未提供对应插槽** | 单元格展示 **`-`**（便于发现配置遗漏）。 |
| **`tag`/`image`/`link`/`text` 无 `prop`** | **`tag`/`image`/`text`**：**dev warn**，该列跳过渲染；**`link`**：无 **`prop`** 时 **`cellValue`** 无法定义——**dev warn** 并跳过该列（与「链接列需绑定字段」一致）。 |

---

## 9. 验收与测试建议

- **各 `columnType`** 渲染结果与 **第 4.3 节** 一致；**`dictList` vs `options`** 优先级可测。
- **`formatter`**：与 EP 签名一致且生效；**有 `formatter` 时** 不出现组件强加的 **`emptyText`** 覆盖。
- **具名 slot**：**`#default`** 作用域为 **`row, column, $index`**；**`#header-${prop}`** 可覆盖列头。
- **排序**：**`order` 未设置**在后；同 **`order`** 保持输入顺序。
- **E2E**：新增 **`C7JsonTableColumnE2E`**（或等价 dev 页）+ 路由，覆盖上表关键路径。
- **可选单测**：若抽出 **`normalizeColumns`**，则对其过滤与稳定排序编写单测。

---

## 10. 文档

- **VitePress**：在与其它 C7 通用组件一致的目录下增加 **C7JsonTableColumn** 说明（**Props / 列配置表 / 示例 / 与 C7JsonTable 关系**）。

---

## 11. 后续流程

- 实现前：由 **`writing-plans`** 产出实现计划（含 **`packages/index.js` 注册**、**E2E 页**、**与 C7Preview/C7DictTag 联调**）。
- 本文档经用户审阅文件无修改诉求后，再进入编码阶段。
