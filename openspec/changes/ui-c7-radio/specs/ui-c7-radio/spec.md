# ui-c7-radio

## Purpose

为 **quick-ui** 提供 **`C7Radio`**：在 **Element Plus `ElRadioGroup`** 之上统一 **静态/异步字典选项**、**`response.data` 解析链** 与 **`default` / `button` / `border` 样式切换**，并与 **`C7Select`** 的 **`fetchData` 契约**对齐，减少重复逻辑。需求来源：**`原始需求/前端/C7单选框.md`**。

## Requirements

### Requirement: 组件与注册位置

**`C7Radio`** MUST 位于 **`quick-ui/src/packages/C7Radio`**，并通过 **`quick-ui/src/packages/index.js`** 的 **`installPackages(app)`** 注册为全局组件 **`C7Radio`**；亦 MAY **`import { C7Radio } from '@/packages'`** 按需使用。

### Requirement: 静态数据来源

系统 MUST 支持通过 **`dataList`** 或 **`options`**（**二者为别名，语义相同**）传入静态选项；当二者同时存在时，**`dataList` MUST 优先于 `options`**，并在组件 JSDoc 中写明。

### Requirement: 异步数据来源与解析链

系统 MUST 支持 **`fetchData(mergedParams)`**，其中 **`mergedParams`** MUST 至少包含调用方 **`fetchParams`** 的浅拷贝（或文档化等价合并规则）。

- **单选组件 MUST NOT** 在 **`mergedParams` 中注入 `query` 键**（不提供远程关键字搜索语义）。
- HTTP 层 MUST 与项目 **`request` / axios** 习惯一致：**Promise reject** 表示加载失败；**resolve** 后方可进入解析链。
- 解析起点 MUST 为 **`response.data`**（与 **`C7Select`** 一致）。
- **`resultKey`**：MAY 为从 **`response.data`** 取数组的点路径；未配置时 **`response.data` 本身** 作为 **`dataFormatter`** 的输入或直接使用（与 **`C7Select`** 一致）。
- **`dataFormatter`**：MAY 在 **`resultKey` 之后**对数组做最终映射/过滤。

### Requirement: 行字段映射（labelKey / valueKey）

系统 MUST 支持通过 **`labelKey`** 与 **`valueKey`**（字符串，点路径或实现与 JSDoc 一致的键访问方式）从每一行对象取出 **展示文案** 与 **选项值**。

- 若某行缺少映射结果，实现 MUST 有明确降级策略（推荐：**label 降级为空串或 `String(value)`**；**value 降级为 `undefined` 的行 MUST 被过滤或不在 UI 渲染**，具体一种并在 JSDoc 写明）。

### Requirement: autoLoad

**`autoLoad`** MUST 为 **`boolean`**。

- 当 **`autoLoad=true`** 且 **未**使用静态绑定（见 **`C7Select`** 同等判定：**`dataList` / `options` 任一 props 被显式传入即视为静态路径已定义**）且 **`fetchData` 为函数** 时，组件 MUST 在 **挂载后** 自动调用 **`fetchData(mergedParams)`** 一次。
- 当 **`autoLoad=true`** 且 **`fetchData` 非函数**，实现 MUST **no-op** 并 SHOULD **`console.warn`**（开发环境），与 **`C7Select`** 风格一致。
- **默认值**：**`autoLoad` 默认 `true`**（与 **`C7Select` 默认 `false`** 不同；理由见 **design.md**）。

### Requirement: radioStyle

**`radioStyle`** MUST 为 **`'default' | 'button' | 'border'`** 之一（大小写敏感以 JSDoc 为准）。

- **`default`**：子项为 **`ElRadio`**，**不带** **`border`**。
- **`button`**：子项为 **`ElRadioButton`**。
- **`border`**：子项为 **`ElRadio`** 且 **启用** Element Plus 文档中与 **边框单选** 对应的 **`border`** 行为。

### Requirement: 根级 attrs 透传（ElRadioGroup）

组件 MUST **`inheritAttrs: false`**，并将 **非保留 props/attrs 键** 绑定到 **`ElRadioGroup`**（保留键集合见 **design.md**，须覆盖所有 **`C7Radio` 自有 props**）。

- **`size`、`disabled`、`fill`、`text-color`** 等 **Element Plus 文档中属于 `ElRadioGroup` 的合法属性** MUST 通过透传生效（与 **`C7Select` → `ElSelect`** 模式一致）。

### Requirement: Element Form 一致性

在 **`el-form` / `el-form-item`** 中使用时，**`C7Radio`** MUST **不破坏** Element Plus 官方 **`ElRadioGroup` + `v-model`** 的校验与 **`prop` 绑定**惯例：

- **`el-form-item`** 的 **`prop`** MUST 仍能指向表单 model 上的同名字段；
- **`rules`** 触发时机与 **`ElRadioGroup`** **无额外差异**（不在 **`C7Radio`** 内自定义 **`el-form-item`**）。

### Requirement: fetch 失败行为（可配置）

系统 MUST 提供 **`fetchErrorBehavior`**，类型为枚举字符串，至少包含：

- **`'keep-last'`**（**默认**）：**保留**上一次成功加载的选项列表（若从未成功过则为空列表）；**MUST NOT** 因失败自动改写外部 **`v-model`**。
- **`'clear-options'`**：将内部选项列表置空；**MUST NOT** 自动改写外部 **`v-model`**（除非与 **`invalidModelBehavior`** 组合在 spec 中另有明确规定）。
- **`'reset-model'`**：在失败时将 **`modelValue` 更新为 `undefined`**（或实现选定且文档化的「空值」），并 **`emit`** 对应 **`update:modelValue`** / **`change`**。

实现 MUST 在 **`fetchData` reject** 路径应用上述行为，并 MUST **`emit` `loading-change(false)`**。

### Requirement: 当前值不在选项中（可配置）

当内部 **选项列表已就绪**（静态或某次 **`fetchData` resolve 后**），若当前 **`modelValue`** **严格不等于** 任一选项 value（含 **`undefined` / `null` 与类型** 比较规则由实现固定并 JSDoc 说明），系统 MUST 应用 **`invalidModelBehavior`**：

- **`'keep'`**（**默认**）：**不改写**外部 **`modelValue`**；UI 上允许出现 **无选中项** 或与 EP 一致的展示。
- **`'clear'`**：**`emit`** 将 **`modelValue` 置为 `undefined`**（或文档化空值）。

另外 MUST 提供 **`suppressInvalidModelDevWarn`**（**`boolean`**，默认 **`false`**）：当为 **`false`** 且 **`invalidModelBehavior='keep'`** 时，在 **`import.meta.env.DEV`** 下 SHOULD **`console.warn` 一次**（避免渲染循环刷屏）；为 **`true`** 时 MUST **不输出**该 warn。

### Requirement: 空选项展示（可配置）

当内部 **选项列表长度为 0** 时，系统 MUST 依据 **`emptyDisplay`**：

- **`'none'`**（**默认**）：不展示额外占位（仅 **无子项** 的 **`ElRadioGroup`**）。
- **`'text'`**：展示 **`emptyText`**（**`string`**；未传或空串时实现 MUST 有明确降级，推荐：**不渲染文本**）。
- **`'slot'`**：渲染 **`#empty`** 插槽内容；若无插槽且为该模式，实现 MUST **no-op 或降级为 `'none'`** 之一并在 JSDoc 写明。

### Requirement: 事件

组件 MUST **`emit`**：

- **`update:modelValue`**
- **`change(value)`**：载荷 MUST 与当前选中 **value** 一致（与 **`v-model`** 同步语义）。
- **`loading-change(loading: boolean)`**：在 **`fetchData` 发起与结束（成功/失败）** 时通知。

### Requirement: 对外暴露

组件 MUST **`defineExpose`**：

- **`loading`**：只读 **boolean**，表示是否存在进行中的 **`fetchData`**。
- **`reload()`**：按当前 **`fetchParams`** **重新发起** **`fetchData`**（**last-write-wins** 仍适用）。

### Requirement: 竞态

当 **`fetchData`** 并发多次时，内部选项列表 MUST 以 **最后一次成功完成** 的结果为准（**last-write-wins**），除非 **`fetchErrorBehavior='reset-model'`** 与失败路径另有交叉——实现 MUST 在 JSDoc 说明 **失败是否参与 generation**。

### Requirement: 验收场景

- **`fetchData` resolve** 后：选项 MUST 可见且可点选；**`v-model`** MUST 随选择更新。
- **`radioStyle`** 切换为 **`button` / `border` / `default`** 时：MUST 分别对应 **`ElRadioButton` / border `ElRadio` / 默认 `ElRadio`** 形态（以当前 **element-plus** 版本 API 为准）。
- **`el-form-item`** 包裹且 **`rules` 要求必选**：未选时 MUST 能触发校验错误（与 **`ElRadioGroup`** 行为一致）。
