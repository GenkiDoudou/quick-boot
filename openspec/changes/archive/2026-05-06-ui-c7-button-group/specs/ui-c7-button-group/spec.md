# ui-c7-button-group

## Purpose

为 **quick-ui** 提供 **`C7ButtonGroup`**：在 **`C7Button`**（**`ui-c7-button`**）之上封装工具栏式 **按钮布局与折叠**，支持 **`inline` / `dropdown` / `auto`** 三种模式，并同时支持 **`buttons[]` 数据驱动** 与 **默认插槽内多个 `C7Button`**。需求来源：`原始需求/前端/C7按钮组.md`。

## Requirements

### Requirement: 布局模式

系统 MUST 支持 **`mode`**：

- **`inline`**：所有可见按钮在同一行（或容器允许换行时按容器规则）平铺展示。
- **`dropdown`**：所有可见按钮均置于「更多」下拉触发器之内（或等价交互），不外露独立主按钮区。
- **`auto`**：最多外露 **`maxVisible`** 个可见按钮（从左至右或文档约定的顺序），其余可见按钮 MUST 进入「更多」下拉。

**`maxVisible`** 仅在 **`mode=auto`** 下生效；其它模式下 MAY 忽略该 prop。**`maxVisible`** MUST 为不小于 **1** 的整数（非法值时实现须有明确降级策略并在组件注释中说明）。

### Requirement: 数据驱动模式

当传入 **`buttons`**（非空数组）时，组件 MUST 根据数组项渲染 **`C7Button`**。每项 MUST 至少支持与 **`C7Button`** 对齐的常用字段，包括但不限于：**`hidden`**、**`disabled`**、**`icon`**、**`label`**、**`btnType`**、**`clickFunction`**，以及 **`C7Button`** 已支持的校验/确认相关 props（若组组件选择透传字段表，须在实现与 JSDoc 中列明）。

- **`hidden=true`** 的项 MUST NOT 占用 **`maxVisible`** 计数，且 MUST NOT 展示。
- 折叠分区 MUST NOT 改变 **`clickFunction`** 与 **`C7Button`** 点击流水线的语义（见 **Requirement: 命令路径与 C7Button 等价性**）。

### Requirement: 插槽模式

当使用默认插槽且未使用 **`buttons`**（或 **`buttons`** 为空且约定回退到插槽）时，组件 MUST 从插槽中识别 **Vue 组件实例类型为 `C7Button`** 的子节点，并按与 **`mode` / `maxVisible`** 相同的规则划分外露区与「更多」区。

- 非 **`C7Button`** 的插槽内容：默认策略 MUST 为 **忽略**（不参与折叠计算、不渲染于组内按钮区）；若实现支持混合内容，须在 JSDoc 中单独声明（本规格默认 **忽略**）。
- 插槽子节点动态变化后，调用方 MAY 调用 **`forceUpdate()`**；调用后 MUST 重新计算折叠分区。

### Requirement: 命令路径与 C7Button 等价性

无论按钮位于外露区还是「更多」下拉内，用户触发命令时 MUST 通过 **`C7Button`** 的标准交互路径执行，使得 **`debounce`、`busy`、`internalLoading`**、表单校验、确认框、**`clickFunction`**、**`checkSuccess`**、成功/失败提示等行为与单独使用 **`C7Button`** 时一致。

系统 MUST NOT 以下拉菜单项 **`click` 处理器直接调用业务函数或仅调用 `clickFunction()`** 等方式绕过 **`C7Button`** 内部实现。

### Requirement: 组级事件与按钮级事件并存

**`C7ButtonGroup`** MUST **`emit`**：

- **`before-command`**：在用户触发某一子按钮的命令且 **即将进入该子按钮流水线之前** 触发；载荷为 **`item`**（见下）。
- **`after-command`**：在该子按钮 **`C7Button`** 流水线结束后触发；载荷 MUST 包含 **`item`**，且 MUST 包含与 **`C7Button`** **`after-click(success)`** 对齐的 **整体成功与否** 信息（推荐使用 **`{ item, success }`** 形态）。

**`item`** 形状：

- **数据模式**：MUST 对应 **`buttons`** 数组中的项标识（推荐提供稳定 **`key`** 字段由调用方设定；若无 **`key`**，实现 MAY 使用索引并在文档中警告列表重排场景）。
- **插槽模式**：MUST 至少包含 **`slotIndex`**（0-based）；MAY 包含调用方在子 **`C7Button`** 上约定的 **`name` / `data-command`** 等（若实现读取，须在 JSDoc 写明）。

同时，子 **`C7Button`** MUST 照常 **`emit`**：**`before-click`**、**`success`**、**`error`**、**`after-click`** 等（与 **`ui-c7-button`** 规格一致），**`C7ButtonGroup`** MUST NOT 拦截或吞并上述事件。

推荐触发顺序 MUST 为：**`before-command`** →（子按钮）**`before-click`** → … →（子按钮）**`after-click`** → **`after-command`**。

### Requirement: 样式与响应式钩子

系统 MUST 支持 **`spacing`**：接受预设关键字或数值（像素）；用于控制按钮之间的间距。

系统 MUST 支持将 **`size`** 传递给子 **`C7Button`**（与 Element Plus / **`C7Button`** 约定一致）。

系统 MUST 支持 **`responsive`**：当为 truthy 时，向组件根元素添加约定 class（具体类名由实现固定并文档化），供业务侧按需编写 CSS。

### Requirement: 「更多」触发器配置

系统 MUST 支持配置「更多」触发按钮的文案、图标、类型、朴素样式与下拉触发方式，对应 props：**`moreText`**、**`moreIcon`**、**`moreButtonType`**、**`moreButtonPlain`**、**`trigger`**（取值与 Element Plus **`el-dropdown`** 的 **`trigger`** 对齐或子集，须在 JSDoc 说明）。

### Requirement: forceUpdate

组件 MUST 通过 **`defineExpose`** 暴露 **`forceUpdate()`** 方法：调用后 MUST 在下一合适时机重新计算插槽模式下子 **`C7Button`** 列表与 **`auto`** 分区。

### Requirement: 验收场景

- **`mode=auto`** 且 **`maxVisible=2`**（数据模式）：若存在不少于 3 个非 **`hidden`** 按钮，则第 3 个及以后 MUST 出现在「更多」内；点击该折叠项 MUST 执行对应 **`clickFunction`** 完整流水线（可与 **`confirm=true`** 组合抽检）。
- **插槽模式**：默认插槽内放置 3 个 **`<C7Button />`** 时，折叠与展示 MUST 正确；点击折叠项与外露项行为 MUST 一致。

### Requirement: 全局可用性

**`C7ButtonGroup`** MUST 位于 **`quick-ui/src/packages/C7ButtonGroup`**（或项目约定的 packages 子目录），并通过 **`packages/index.js`** 注册为全局组件 **`C7ButtonGroup`**；亦 MAY 按需 **`import { C7ButtonGroup } from '@/packages'`**。
