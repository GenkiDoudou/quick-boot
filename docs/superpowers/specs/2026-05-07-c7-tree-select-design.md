# C7TreeSelect（C7 树选择）设计说明

**日期**：2026-05-07  
**状态**：已定稿（经 brainstorming 澄清：1A～6A；方案 **A**——独立组件、契约对齐 `C7Select`）  
**依据**：`原始需求/前端/C7树选择.md` + 本节 Q&A 与方案对比结论

---

## 1. 背景与目标

树选择用于**部门 / 分类**等层级数据场景，需在 **`ElTreeSelect`** 之上统一：**静态与异步整树加载**、**字段映射**、**多选对外格式**（数组 vs 逗号分隔字符串）、**值类型**，并与现有 **`C7Select`** 的加载与命名习惯对齐，降低业务侧心智成本。

---

## 2. 组件边界

### 2.1 职责

- 数据来源：`dataList`（及别名 `options`）**优先**；否则通过 `fetchData` + `resultKey`（可选 `dataFormatter`）加载**整棵树**数组。
- 将业务字段映射为 `ElTreeSelect` 所需的 `label` / `value` / `children`：`labelKey`、`valueKey`、`childrenKey`。
- 多选：`multiple === true` 时由 EP 展示 checkbox 行为；对外 `v-model` / `change` 支持 **`separator`** 逗号字符串或数组（见 §5）。
- `valueType`：`auto` | `string` | `number`，控制对外标量/数组元素类型。
- 透传：`filterable`、`filterNodeMethod` 及未在保留键中的 `ElTreeSelect` attrs。
- 事件：`update:modelValue`、`change`、`load-error`；若 EP 暴露与下拉可见性相关事件，实现时**可**与 `C7Select` 一样增加 `visible-change` / `loading-change` 透传（以 EP 类型为准，不阻塞一期）。
- 暴露：`reload()`，用于重新请求或刷新静态来源（行为见 §4）。

### 2.2 非职责（一期明确不做）

- **节点懒加载**（`lazy` + `load`）、按关键字远程搜树（与 `C7Select` 的 `remote` 模式不对齐一期）。
- **不**为减小与 `C7Select` 的重复而一期抽取公共 composable（避免牵动 `C7Select`）；后续若重复明显再抽离。

### 2.3 与原始需求文档的差异

- 原始需求中的 **`rangeMerge`** 与实现对齐时统一为 **`separator`**（与 `C7Select` 一致）；建议在实现阶段同步更新 `原始需求/前端/C7树选择.md` 中的用词。

---

## 3. 技术选型（已定稿）

- **底层组件**：`ElTreeSelect`（Element Plus）。
- **实现策略**：**方案 A**——独立 `C7TreeSelect` 组件内实现与 `C7Select` 同类的请求与列表解析逻辑；树专有映射保留在本组件。

---

## 4. 数据流与加载策略

1. **静态优先**：若 `dataList !== undefined`（含空数组），以 `dataList` 为数据源，**不因存在 `fetchData` 而自动发请求**（与 `C7Select` 一致）。`options` 为 `dataList` 别名；**`dataList !== undefined` 时仅以 `dataList` 为准**。
2. **异步整树**：当无有效静态约定下的数据需求时，由 **`autoLoad`**（默认与 `C7Select` 一致：`true` 时在无静态数据场景挂载后拉取一次，参数为 `fetchParams` 浅拷贝、**无 `query`**）或 **`reload()`** 触发 `fetchData`。
3. **解析链**：`fetchData` 返回值 → 从 `response.data`（或与项目 axios 封装一致的数据根）上按 **`resultKey`** 取数组 → 可选 **`dataFormatter(list)`** → **`mapTreeKeys`** 生成传入 `ElTreeSelect` 的 `data`。
4. **失败**：请求异常时 **`emit('load-error', error)`**，树数据保持**上一次成功结果**或空数组；**不**伪造成功。
5. **加载态**：内部维护 loading 标志；若调用方通过 attrs 传入 `loading`，以**不破坏 EP 行为**为原则合并（实现细节：优先内部请求态，或与 attrs 做 OR，在 JSDoc 中写清）。

---

## 5. 对外值形态与 `valueType`

### 5.1 对内

与 `ElTreeSelect` 一致：单选为标量；多选为**数组**（原始 value 类型由节点 `valueKey` 字段决定）。

### 5.2 对外 `v-model` / `change`

| 场景 | 行为 |
|------|------|
| 单选 | 按 `valueType` 输出标量；**`auto`**：以 **映射后根列表第一条节点**的 **`valueKey` 字段的 JavaScript 类型**为准，将当前选中值转为该类型（无节点或无法取样时退化为 `string` 或与 `C7Select` 相同兜底策略）。 |
| 多选且 **`separator === false`** | 对外为 **数组**；元素类型服从 `valueType` / `auto`。 |
| 多选且 **`separator === true`** | 对外为 **英文逗号分隔字符串**；**空选择**对外为 **`''`**（与 `C7Select` 一致）。 |

### 5.3 外部值回写

- **`separator`** 模式下外部传入字符串：解析为数组再传入 EP；解析规则与 `C7Select` **对齐**（含「值中含逗号」的文档警示）。
- **`valueType === 'number'`**：对外对内数字一致时的转换与 `C7Select` 行为对齐，避免浮点与大整数歧义处须在 JSDoc 中注明边界。

### 5.4 多选父子联动

**默认不传 `check-strictly`** 等与 EP 默认不一致的 prop，即 **与 Element Plus `ElTreeSelect` 默认父子联动行为一致**（澄清 3A）。业务若需父子独立勾选，自行传 `check-strictly` 等 attrs。

---

## 6. Props 表（实现清单）

保留键（不得原样落入 `forwardedAttrs`）建议与 `C7Select` 对齐命名：

| Prop | 说明 |
|------|------|
| `dataList` / `options` | 静态树；别名语义见 §4 |
| `fetchData` / `fetchParams` / `resultKey` / `dataFormatter` / `autoLoad` | 异步整树加载 |
| `labelKey` / `valueKey` / `childrenKey` | 字段映射，默认值建议 `label` / `value` / `children` |
| `separator` | 多选时对外逗号串；非多选无效 |
| `valueType` | `'auto'` \| `'string'` \| `'number'`，默认 `'auto'` |
| `modelValue` | 由 `v-model` 绑定（实现侧标准） |

其余 attrs 透传至 `ElTreeSelect`（`inheritAttrs: false` + 显式 `v-bind`）。

---

## 7. 事件与暴露

| 名称 | 说明 |
|------|------|
| `update:modelValue` | 对外形态见 §5 |
| `change` | 载荷与对外 `modelValue` 一致 |
| `load-error` | 参数为 `Error` 或 rejection 值 |

`defineExpose({ reload })`：`reload()` 触发与首次加载相同的获取逻辑（静态则重新应用映射；异步则重新请求）。

---

## 8. 工程约定

- **路径**：`quick-ui/src/packages/C7TreeSelect/index.vue`
- **注册**：在 `quick-ui/src/packages/index.js` 中导出并 `installPackages` 注册，名称 **`C7TreeSelect`**
- **文档**：实现阶段建议补充 VitePress 通用组件文档（与 `c7-select` 等并列），本设计稿不替代用户文档。

---

## 9. 验收标准

- 异步加载整树后可单选、多选；多选 + `separator` 时对外为逗号字符串且空为 `''`；否则为数组。
- `labelKey`/`valueKey`/`childrenKey` 映射正确；`filterable` 与 `filterNodeMethod` 透传生效。
- `reload()` 后数据更新；加载失败时触发 `load-error` 且不静默成功。

---

## 10. 澄清结论速查

| 项 | 结论 |
|----|------|
| 逗号模式命名 | **`separator`**（对齐 `C7Select`） |
| 异步形态一期 | **整树一次拉取** + `resultKey` |
| 父子勾选默认 | **EP 默认** |
| `valueType === 'auto'` | **根列表首条** `valueKey` 字段类型 |
| 首次请求 | **`autoLoad`**，与 `C7Select` 语义对齐 |
| 静态别名 | **`options` 同 `C7Select`** |
