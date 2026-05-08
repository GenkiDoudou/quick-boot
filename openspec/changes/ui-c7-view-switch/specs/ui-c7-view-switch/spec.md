# ui-c7-view-switch

## Purpose

为 **quick-ui** 提供 **`C7ViewSwitch`**：基于 **`views` / `showIndexs`** 与 **`modelValue`（视图名字符串）** 渲染**具名插槽**；内部维护**仅由 `switchTo` / `goBack` 修改**的历史栈；可选 **`ElPageHeader`** 与可配置 **`<Transition>`**；支持 **`not-found`**、**`#empty`** 兜底。需求来源：**`docs/superpowers/specs/2026-05-08-c7-view-switch-design.md`**、**`原始需求/前端/C7视图切换容器.md`**。

## ADDED Requirements

### Requirement: 组件与注册位置

**`C7ViewSwitch`** MUST 位于 **`quick-ui/src/packages/C7ViewSwitch`**（主文件 **`index.vue`**），并通过 **`quick-ui/src/packages/index.js`** 的 **`installPackages(app)`** 注册为全局组件 **`C7ViewSwitch`**，且 MUST 被 **`export`** 供按需引入。

#### Scenario: 全局注册可用

- **WHEN** 应用已执行 **`installPackages`**
- **THEN** 模板中 MUST 能直接使用 **`<C7ViewSwitch />`** 而无额外局部注册

### Requirement: 视图配置与别名优先级

组件 MUST 接受 **`views`** 与 **`showIndexs`**，二者均为 **`Array<{ name: string; title?: string; closeIndex?: string }>`** 语义。当 **`views` 与 `showIndexs` 同时传入**时，系统 MUST **仅使用 `views`** 作为有效配置源。

#### Scenario: 同时传入时以 views 为准

- **WHEN** **`views`** 与 **`showIndexs`** 均非空且 **`name`** 列表不一致
- **THEN** 匹配与插槽分发 MUST 仅依据 **`views`**

### Requirement: 视图匹配与插槽

系统 MUST 使用 **`config.name === modelValue`** 判定当前视图配置。对每个有效 **`name`**，组件 MUST 提供具名插槽 **`#<name>`**（**scoped**），且 slot props MUST 至少包含：**`{ config, switchTo, goBack }`**，其中 **`switchTo` / `goBack`** 与实例暴露方法语义一致。

当 **`modelValue` 无法**在有效配置中匹配任一 **`name`** 时，组件 MUST 渲染 **`#empty`** 插槽（若存在）；若 **不存在** **`#empty`**，则 MUST 渲染**无内容的占位**（不抛异常）。

#### Scenario: 匹配视图渲染对应插槽

- **WHEN** **`modelValue`** 等于某 **`views[].name`**
- **THEN** 该 **`name`** 对应插槽内容 MUST 被渲染，且 scoped props MUST 可用

#### Scenario: 未匹配且无 empty

- **WHEN** **`modelValue`** 非法且未提供 **`#empty`**
- **THEN** 用户 MUST 看到空白占位且应用 MUST 不崩溃

### Requirement: switchTo 与 not-found

**`switchTo(targetName: string)`** MUST：若不存在 **`name === targetName`** 的配置项，则 **`emit('not-found', targetName)`**，且 MUST **不**更新 **`modelValue`**，MUST **不**修改历史栈。

若 **`targetName` 与当前 `modelValue` 相同**，则 MUST **不**压栈、**不** **`emit('update:modelValue')`**、**不** **`emit('change')`**。

否则，系统 MUST 将**当前** **`modelValue`** **压入**历史栈，再 **`emit('update:modelValue', targetName)`**，并 **`emit('change', targetName, matchedConfig)`**，其中 **`matchedConfig`** 为匹配到的视图配置对象（与 **`views`** 同源引用，行为见 **design**）。

#### Scenario: 非法目标不切换

- **WHEN** 调用 **`switchTo('unknown')`**
- **THEN** **`not-found`** MUST 被触发，且 **`modelValue`** MUST 保持不变

#### Scenario: 同值 no-op

- **WHEN** 当前为 **`list`** 且调用 **`switchTo('list')`**
- **THEN** 历史栈长度 MUST 不变，且 MUST **不**触发 **`change`**

### Requirement: goBack、back 与空栈回落

**`goBack()`** MUST：若历史栈**非空**，则弹出栈顶作为上一视图名，**`emit('update:modelValue', previousName)`**，并 **`emit('back', previousName, previousConfig)`**。

若历史栈**为空**，系统 MUST 依次尝试：**当前**匹配配置的 **`closeIndex`**、组件 **`defaultView` prop**；若存在且解析为有效 **`name`** 且**与当前 `modelValue` 不同**，则 **`emit('update:modelValue', …)`** 并 **`emit('change', …)`**。若仍无法回落，则 MUST **`emit('back-empty')`**（脚本侧 **`backEmpty`**），且 MUST **不**改变 **`modelValue`**。

#### Scenario: 有历史时回到上一屏

- **WHEN** 自 **`A`** 经 **`switchTo`** 到 **`B`** 后调用 **`goBack()`**
- **THEN** **`modelValue`** MUST 变为 **`A`**，且 **`back`** MUST 携带 **`A`** 与其配置

#### Scenario: 空栈无回落

- **WHEN** 历史栈为空且当前配置无有效 **`closeIndex` / `defaultView`**
- **THEN** **`goBack()`** MUST 触发 **`back-empty`**，且 **`modelValue`** MUST 不变

### Requirement: 父级直接修改 v-model 不入栈

当 **`modelValue` 由父组件绑定更新**（非本组件 **`emit`** 的同一更新 tick 语义以实现为准，但效果为「外部改值」）时，组件 MUST **仅**切换当前展示视图，MUST **不**对历史栈执行 **push** 或 **pop**。

#### Scenario: 父级 set 新视图

- **WHEN** 父组件将 **`v-model`** 从 **`list`** 改为 **`edit`** 且未调用 **`switchTo`**
- **THEN** 展示 MUST 切换至 **`edit`**，且历史栈内容 MUST 与变更前一致（无自动 push/pop）

### Requirement: change 不在首次挂载触发

组件 MUST **不在**首次挂载完成时的初始 **`modelValue` 同步**阶段 **`emit('change')`**。在首次挂载完成之后，**凡导致 `modelValue` 变为新值且与旧值不同`**，组件 MUST **`emit('change', newName, newConfig)`**（含 **`switchTo`**、**`goBack`**、父级绑定更新、空栈回落成功等路径）。

#### Scenario: 挂载不产生 change

- **WHEN** 组件挂载且父级传入初始 **`modelValue='list'`**
- **THEN** 挂载过程中及挂载完成瞬间 MUST **不**发出 **`change`**

#### Scenario: 后续切换产生 change

- **WHEN** 挂载完成后 **`switchTo`** 切换到另一有效视图
- **THEN** **`change`** MUST 携带新视图名与配置

### Requirement: transition 三态

**`transition`** prop MUST 接受 **`boolean | string`**。当为 **`false`** 时，视图区域 MUST **不**包裹 **`<Transition>`**。当为 **`true`** 时，MUST 使用约定默认 **`name`**（**`c7-view-switch`**）。当为 **`string`** 时，MUST 将该字符串作为 **`<Transition :name>`**。

#### Scenario: 关闭过渡

- **WHEN** **`transition=false`**
- **THEN** 切换视图时 MUST 无 Vue Transition 类名切换（无该包裹）

### Requirement: 可选 ElPageHeader

当 **`showPageHeader=true`** 时，组件 MUST 渲染 **`ElPageHeader`**。页头 **`@back`**（或 Element Plus 等价返回事件）触发时 MUST 调用与 **`goBack()`** 相同的逻辑分支。

当当前匹配项存在 **`title`** 时，页头标题 MUST 使用该值；当不存在时，**`title`** MUST 为 **空字符串**（见 **design**）。

组件 MUST 提供 **`#header-content`** 插槽，用于 **`ElPageHeader` 的 content 区**（与当前 **element-plus** 版本插槽名对齐，实现注释中写明实际绑定名）。

#### Scenario: 页头返回触发 goBack

- **WHEN** **`showPageHeader=true`** 且用户点击页头返回
- **THEN** 行为 MUST 与调用 **`goBack()`** 一致（含空栈回落与 **`back-empty`**）

### Requirement: defineExpose

组件 MUST **`defineExpose`**：**`switchTo`**、**`goBack`**、**`currentConfig`**（当前匹配配置或 **`null`**）、**`viewHistory`**（历史栈只读快照，**MUST NOT** 允许外部修改内部栈状态；返回新数组或等价只读结构）。

#### Scenario: ref 调用 switchTo

- **WHEN** 父组件通过 **ref** 调用 **`switchTo('detail')`**
- **THEN** 行为 MUST 与模板内 scoped 提供的 **`switchTo`** 一致

### Requirement: 文档

**VitePress** 文档 MUST 新增 **`C7ViewSwitch`** 说明页，且 MUST 包含：**基础用法**、**`views` / `showIndexs` 优先级**、**父级直接修改 `v-model` 与历史栈关系** 的说明或示例。

#### Scenario: 文档可检索

- **WHEN** 开发者打开文档站点并浏览 C7 组件索引
- **THEN** MUST 能找到 **`C7ViewSwitch`** 页面并阅读上述要点
