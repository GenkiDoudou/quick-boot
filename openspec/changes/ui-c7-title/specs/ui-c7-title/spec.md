# Delta Spec: ui-c7-title

## ADDED Requirements

### Requirement: C7Title 组件注册与根节点

系统 MUST 在 **`quick-ui`** 提供名为 **`C7Title`** 的 Vue 组件，并在 **`quick-ui/src/packages/index.js`** 中导出且通过 **`installPackages`** 全局注册。根节点 MUST 为可挂载 **`class`/`style`** 的容器（如 **`div.c7-title`**），且根上 MUST 可设置 CSS 变量 **`--c7-title-decoration-color`** 以控制底部线颜色。

#### Scenario: 应用 installPackages 后可使用标签名

- **WHEN** 应用已执行 **`installPackages(app)`**
- **THEN** 模板中可使用 **`<C7Title />`** 且组件解析为注册的 **`C7Title`**

---

### Requirement: 语义标签与 labelSize 联动

组件 MUST 支持 **`tag`** 取值为 **`h1`**~**`h6`**、**`div`**、**`p`**。为区分「未传 **`tag`**」与「显式传 **`h4`**」，**`tag` prop 的默认值 MUST 为 `undefined`**；当 **`tag` 为 `undefined`** 且 **`labelSize`** 非 h 级预设时，实现 MUST 将最终语义标签视为 **`h4`**（与 **`C7Card`** 默认标题层级对齐）。

组件 MUST 支持 **`labelSize`** 为 **`h1`**~**`h6`** 或带单位的字号字符串（如 **`20px`**、**`1.2rem`**、**`1.2em`**）。

当 **`labelSize`** 为 **`h1`**~**`h6`** 且 **`tag` 为 `undefined`** 时，渲染所用语义标签 MUST 与 **`labelSize`** 同级。当 **`tag`** 已传入（非 **`undefined`**）时，**`labelSize`** 为 h 级时 MUST **不** 改写该 **`tag`**。

当 **`labelSize`** 为自定义单位字符串时，组件 MUST 仅应用字号相关样式（及合理行高），MUST **不** 因 **`labelSize`** 改变 **`tag`**。

#### Scenario: labelSize 为 h2 且未传 tag 时标题为 h2

- **WHEN** **`labelSize="h2"`** 且 **`tag` 未传入**（**`undefined`**）
- **THEN** 标题语义节点为 **`h2`**（或 **`ElText`** 等价语义输出为 **`h2`**）

#### Scenario: 显式 tag 与 h 级 labelSize 并存时保留 tag

- **WHEN** **`labelSize="h2"`** 且 **`tag="div"`**
- **THEN** 标题语义节点为 **`div`**，字号样式仍按 **`h2`** 预设应用

#### Scenario: 显式 tag 为 h4 时不被 labelSize h2 覆盖

- **WHEN** **`labelSize="h2"`** 且 **`tag="h4"`**（显式传入）
- **THEN** 标题语义节点为 **`h4`**（**不** 升级为 **`h2`**）

#### Scenario: 自定义 labelSize 不改变 tag

- **WHEN** **`labelSize="18px"`** 且 **`tag="h4"`**
- **THEN** 语义节点仍为 **`h4`**，展示字号反映 **`18px`**（及约定行高）

---

### Requirement: 主文案与 title 插槽

组件 MUST 支持 **`label`** 与 **`title`** 字符串属性；二者语义均为标题主文案。**`label`** 优先级高于 **`title`**：当二者同时为非空时，展示与逻辑 MUST 仅以 **`label`** 为准。组件 MUST 支持 **`#title`** 插槽；当 **`#title`** 存在时，MUST **不** 再渲染 **`label`/`title`** 的默认文本（由插槽完全覆盖）。

#### Scenario: 仅 label 展示

- **WHEN** **`label="基本信息"`** 且无 **`#title`**
- **THEN** 用户可见标题文本为 **`基本信息`**

#### Scenario: label 与 title 同时存在时认 label

- **WHEN** **`label="A"`** 且 **`title="B"`** 且无 **`#title`**
- **THEN** 用户可见标题文本为 **`A`**

#### Scenario: title 插槽覆盖 prop

- **WHEN** 提供 **`#title`** 插槽内容
- **THEN** 可见标题由插槽渲染，**不** 依赖 **`label`/`title`** 字符串

---

### Requirement: 装饰色、底部线与 showBorder

组件 MUST 支持 **`decorationColor`** 与 **`labelColor`**；有效颜色 MUST 按 **`decorationColor`**（若定义）优先，否则 **`labelColor`**，否则默认 **`var(--el-color-primary)`**，并写入根节点 **`--c7-title-decoration-color`**。

组件 MUST 支持 **`showBorder`**（默认 **`true`**）。当 **`showBorder`** 为 **`true`** 时，MUST 在根容器宽度内显示底部线，颜色取自 **`--c7-title-decoration-color`**，线高约为 **`2px`**（允许实现用 **`border`** 或伪元素，须与设计文档「单一线条」一致）。当 **`showBorder`** 为 **`false`** 时，MUST **不** 显示该底部线。

#### Scenario: decorationColor 覆盖 labelColor

- **WHEN** **`decorationColor="#409eff"`** 且 **`labelColor="#f00"`**
- **THEN** **`--c7-title-decoration-color`** 与底部线可视颜色反映 **`#409eff`**

#### Scenario: showBorder 关闭无底线

- **WHEN** **`showBorder=false`**
- **THEN** 不出现与 **`showBorder=true`** 时同类的底部装饰线

---

### Requirement: 图标与插槽优先级

组件 MUST 支持 **`icon`** 字符串（**`@element-plus/icons-vue`** 导出名的 **PascalCase**）。当 **`icon`** 对应导出存在时，MUST 在标题左侧展示该图标（与 **`el-icon`** 用法一致）。当不存在对应导出时，MUST **`console.warn`** 且 MUST **不** 渲染字符串解析图标。

组件 MUST 支持 **`#icon`** 插槽。当 **`#icon`** 存在时，MUST **不** 使用 **`icon`** 字符串解析结果（插槽完全覆盖）。

#### Scenario: 有效 icon 名渲染图标

- **WHEN** **`icon` 为有效 PascalCase 图标名** 且无 **`#icon`**
- **THEN** 左侧出现对应图标

#### Scenario: 无效 icon 名不渲染且告警

- **WHEN** **`icon="NonExistentIcon"`** 且无 **`#icon`**
- **THEN** 不出现该字符串图标，且控制台出现 **warn**（开发构建下可验证）

#### Scenario: icon 插槽覆盖字符串

- **WHEN** 同时存在 **`icon`** 与 **`#icon`** 插槽
- **THEN** 仅渲染 **`#icon`** 内容

---

### Requirement: 默认插槽为右侧操作区

组件 MUST 将 **默认插槽** 作为标题行 **右侧 actions** 区域渲染，且布局上 MUST 使主标题与图标区靠左、默认插槽内容靠右（如 flex + **`margin-left: auto`** 或等价）。

#### Scenario: 默认插槽内按钮靠右

- **WHEN** 默认插槽内放置按钮且标题文本存在
- **THEN** 按钮出现在标题行右侧区域

---

### Requirement: 字重与无障碍基线

标题文本区域 MUST 使用 **`font-weight: 600`**（或与设计文档一致的固定字重），且 MUST **不** 通过新增 **`bold` prop** 切换。组件 MUST 通过 **`tag`/`resolvedTag`** 输出正确标题层级以供辅助技术；**`h1`** MUST **不** 被滥用为纯装饰（文档/代码注释中说明由业务选择合理层级）。

#### Scenario: 默认可感知为加粗样式

- **WHEN** 渲染任意合法 **`tag`** 与文案
- **THEN** 标题文本视觉字重为约定的 **600**（或与 EP 组合后仍显著加粗）
