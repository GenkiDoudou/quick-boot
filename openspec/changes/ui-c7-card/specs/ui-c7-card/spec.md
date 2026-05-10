# ui-c7-card

## Purpose

为 **quick-ui** 提供 **`C7Card`**：在 **`ElCard`** 上统一 **默认标题栏**（色块、**`label`**、**`textSize` h1~h5**、加粗）、**右侧 `extra`、折叠** 与 **内容区 fade**，减少页面重复拼装。需求来源：**`原始需求/前端/C7卡片.md`**。

## ADDED Requirements

### Requirement: 组件与注册位置

**`C7Card`** MUST 位于 **`quick-ui/src/packages/C7Card`**，并通过 **`quick-ui/src/packages/index.js`** 的 **`installPackages(app)`** 注册为全局组件 **`C7Card`**。

#### Scenario: 全局注册可用

- **WHEN** 应用已执行 **`installPackages`**
- **THEN** 模板中 MUST 能直接使用 **`<C7Card />`** 而无额外局部注册

### Requirement: 默认头部与 `#header` 覆盖

当 **未**提供 **`#header`** 插槽时，组件 MUST 渲染 **默认头部**：左侧 **可选色块**（由 **`showColorBlock` / `isShowColorBlock`** 与 **`colorBlockColor`** 控制）+ **`label`** 文案；**`textSize`** MUST 为 **`h1` | `h2` | `h3` | `h4` | `h5`** 之一并映射到实现文档化的标题视觉档位；**`isBold`** 为 true 时标题 MUST 以加粗样式展示。

当 **提供** **`#header`** 插槽时，组件 MUST **仅渲染**该插槽作为 **`ElCard` header** 内容，**MUST NOT** 同时渲染默认头的色块、**`label`** 与 **默认折叠按钮**。

#### Scenario: 默认头展示色块与标题

- **WHEN** 未使用 **`#header`**，且 **`showColorBlock=true`**（或别名解析后为 true，见下一 Requirement），且 **`label`** 非空
- **THEN** 用户 MUST 能看到 **色块** 与 **标题文案**，且字号档位与 **`textSize`** 一致

#### Scenario: header 完全自定义

- **WHEN** 使用 **`#header`**
- **THEN** 默认头的 **色块、`label`、默认折叠控件** MUST 均不出现

### Requirement: 色块布尔与别名

系统 MUST 同时接受 **`showColorBlock`** 与 **`isShowColorBlock`**。解析规则 MUST 固定为：**若二者均传入，以 `showColorBlock` 为准**；若仅传入其一，则以该值为准；**未传入**时默认 **不展示色块**（实现须在 JSDoc 写明默认布尔值）。

#### Scenario: 别名被主 prop 覆盖

- **WHEN** **`showColorBlock=false`** 且 **`isShowColorBlock=true`**
- **THEN** 色块 MUST **不展示**

### Requirement: 折叠与 v-model

当 **`collapsible=false`**（或未启用折叠）时，组件 MUST **不展示**默认折叠触发器（**`#toggle` 不生效于默认头**，因默认头不存在折叠控件占位）。

当 **`collapsible=true`** 且 **未**使用 **`#header`** 时，组件 MUST 展示 **默认折叠触发器**（文案或图标由 **`expandText` / `collapseText`** 与实现选定组合固定并在 JSDoc 说明）。

组件 MUST 支持 **`defaultExpanded`** 作为 **非受控**初始展开状态；支持 **`v-model` / `modelValue`** 作为 **受控**展开状态。展开状态变化时 MUST **`emit`**：**`update:modelValue`** 与 **`change(expanded: boolean)`**。

当 **未**绑定 **`v-model`/`modelValue`** 且 **未**传入 **`defaultExpanded`** 时，初始展开状态 MUST 为 **展开（`true`）**（与原始需求「默认展开」一致）。

#### Scenario: 默认展开与点击折叠

- **WHEN** **`collapsible=true`** 且处于 **非受控**、**未传 `defaultExpanded`**（或显式 **`defaultExpanded=true`**）
- **THEN** 初始 **内容区** MUST 可见；用户点击默认折叠控件后，**内容区** MUST 隐藏，且 **MUST** 应用 **fade** 过渡

#### Scenario: 外部 v-model 控制

- **WHEN** 父组件将 **`v-model`** 绑定为 **`false`**
- **THEN** 内容区 MUST 处于折叠隐藏状态；父改为 **`true`** 时 MUST 展开

### Requirement: 内容区 fade 与 header 稳定

内容区（默认插槽）外层 MUST 使用 **`transition`**（**name** 实现固定为 fade 语义）包裹；折叠时 **MUST** 仅切换 **内容区** 的 **v-if / v-show** 策略之一（实现选定并在 JSDoc 说明），**MUST NOT** 在折叠时卸载 **`ElCard` header** 中与标题无关的整块 **`header` 插槽结构**导致标题闪烁（**`#header` 自定义**时同理：**折叠只影响 body**）。

#### Scenario: 折叠时标题仍可见

- **WHEN** 使用默认头且 **`collapsible=true`**，用户折叠卡片
- **THEN** **标题栏** MUST 仍可见，仅 **正文区域** 隐藏

### Requirement: 插槽 extra 与 toggle

组件 MUST 提供 **`#extra`** 插槽，渲染于 **默认头** 的 **右侧区域**（在 **折叠触发器** 左侧或实现文档化的固定顺序）。

组件 MUST 提供 **`#toggle`** 插槽用于 **替换**默认折叠控件；**仅当** **`collapsible=true`** 且 **未**使用 **`#header`** 时 **`#toggle`** 生效。

#### Scenario: extra 展示操作按钮

- **WHEN** 在 **`#extra`** 放入按钮
- **THEN** 用户 MUST 在卡片头 **右侧** 看到该按钮

### Requirement: defineExpose 方法

组件 MUST **`defineExpose`** 对象包含：**`toggle()`**、**`expand()`**、**`collapse()`**。

- **`expand()`** 后状态 MUST 为 **展开**；**`collapse()`** 为 **折叠**；**`toggle()`** 在二者间切换。
- 受控模式下调用上述方法 MUST **`emit`** 与 **`v-model`** 一致的更新（与实现 **同步/await** 策略在 JSDoc 说明）。

#### Scenario: 父组件 ref 调用 toggle

- **WHEN** 父组件通过 **ref** 调用 **`toggle()`**
- **THEN** 展开状态 MUST 翻转，且 **`change`** MUST 携带最新 **boolean**

### Requirement: ElCard 透传

**`shadow`** 及其它 **`ElCard`** 合法 props（除 **`C7Card`** 保留字段外）MUST 透传至 **`ElCard`**。保留字段集合 MUST 至少包含：**`label`、`textSize`、`isBold`、`showColorBlock`、`isShowColorBlock`、`colorBlockColor`、`collapsible`、`defaultExpanded`、`modelValue`、`expandText`、`collapseText`**（及实现内部使用的别名归一字段，若有）。

#### Scenario: shadow 生效

- **WHEN** 传入 **`shadow="never"`**（或当前 EP 支持的合法值）
- **THEN** **`ElCard`** MUST 应用对应阴影策略

### Requirement: 无障碍默认折叠控件

默认折叠控件 MUST 为 **`button type="button"`**，且 MUST 具备 **`aria-expanded`** 与 **`aria-controls`**（指向内容容器 **`id`**）。

#### Scenario: 屏幕阅读器可读状态

- **WHEN** 折叠状态切换
- **THEN** **`aria-expanded`** MUST 与真实展开状态一致
