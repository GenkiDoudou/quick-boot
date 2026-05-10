# ui-c7-switch

## Purpose

为 **quick-ui** 提供 **`C7Switch`**：在 **`ElSwitch`** 上统一 **值体系**（**`modelValue` / `activeValue` / `inactiveValue`**）、**文案**（**`dictList` 字典优先**，**`activeText` / `inactiveText` 兜底**）、**`beforeChange` → 确认 → 可选 `asyncChange` → 可选 `afterChange`** 流水线，以及 **CSS 变量**注入的 **开/关颜色**。需求来源：**`原始需求/前端/C7开关.md`**；**`beforeChange` 静默**、**事件时序**、**无 `asyncChange` 时同步路径与 `afterChange`** 等细化见本变更 **`design.md`**。

## ADDED Requirements

### Requirement: 组件与注册位置

**`C7Switch`** MUST 位于 **`quick-ui/src/packages/C7Switch`**（至少包含 **`index.vue`**），并通过 **`quick-ui/src/packages/index.js`** 的 **`installPackages(app)`** 注册为全局组件 **`C7Switch`**；亦 MAY **`import { C7Switch } from '@/packages'`** 按需使用。

#### Scenario: 全局注册可用

- **WHEN** 应用已执行 **`installPackages(app)`**
- **THEN** 模板中 MUST 能使用 **`<C7Switch />`** 而无需逐页 **import** 注册

### Requirement: 值体系与受控更新

组件 MUST 支持 **`modelValue`** 与 **`activeValue` / `inactiveValue`**（默认值可与 **`ElSwitch`** 常见约定对齐，但 **MUST** 在 JSDoc 写明）。用户意图切换后的目标值 **MUST** 为「当前非 **`modelValue`** 的一侧」所对应的 **`activeValue` 或 `inactiveValue`**。

#### Scenario: 非布尔取值

- **WHEN** **`activeValue='1'`**、**`inactiveValue='0'`** 且 **`modelValue='0'`**
- **THEN** 用户完成一次有效切换且流水线允许提交后，对外的 **`modelValue`** MUST 变为 **`'1'`**

### Requirement: 文案 — `dictList` 优先于显式文本

若提供 **`dictList`**（**`{ label, value }[]`**），展示用 **`activeText` / `inactiveText`（或传给 `ElSwitch` 的等价文案 props）** MUST 按侧取值：

- **若**存在 **`dictList` 项** 其 **`value`** 与 **`activeValue`**（或 **`inactiveValue`**）**全等**匹配，**MUST** 使用该 **`label`**。
- **若**无匹配项，**MUST** 回退到对应 **`activeText` / `inactiveText`**（显式兜底）。

**字典项匹配** MUST 使用 **严格相等**（**`===`**）或 **与 `modelValue` 比较一致的等价语义**；**`value` 类型**（**`string` / `number` / `boolean`**）组合 **MUST** 在 JSDoc 说明避免隐式转换陷阱。

#### Scenario: 字典命中覆盖显式

- **WHEN** **`dictList`** 含 **`{ label: '启用', value: 1 }`** 且 **`activeValue=1`**，同时 **`activeText='开'`**
- **THEN** 激活侧展示文案 MUST 为 **`启用`**（**非** **`开`**）

#### Scenario: 字典未命中回退显式

- **WHEN** **`dictList`** 不包含与 **`inactiveValue`** 匹配的项，且 **`inactiveText='关'`**
- **THEN** 非激活侧展示文案 MUST 为 **`关`**

### Requirement: 流水线顺序

用户发起切换时，组件 MUST 按下述顺序执行（任一步 **中止**则后续步骤 **MUST NOT** 执行，**`asyncChange` loading`** **MUST** 结束或 **不**开始）：

1. **`beforeChange(newVal)`**（**`newVal`** 为目标值）：若提供且返回 **严格 `false`**（同步或 **resolve 为 `false`** 的 **Promise**），**MUST** **完全静默**中止（见下条 **Requirement**）。
2. **确认**：若配置了 **`confirmFn`**，**MUST** **仅**走 **`confirmFn`**；**不得**同时弹 **`confirmMessage`**。**若未配置 `confirmFn`** 且配置了 **`confirmMessage`**（实现 **MUST** 在 JSDoc 定义「何种 truthy 配置会触发 **`ElMessageBox.confirm`**」），**MUST** 弹窗确认。
3. **`asyncChange(newVal)`**：**若**提供，**MUST** 在执行期展示 **loading**（与 **`ElSwitch`** 的 loading 能力对齐）；**仅当** **Promise resolve**（或同步函数正常返回）后 **MUST** 提交新 **`modelValue`** 并继续 **`afterChange`**。**若 reject 或抛错**，**MUST NOT** **`emit('update:modelValue')`** 为失败侧新值（对外值保持旧值，**UI 与 `modelValue` 一致**）。
4. **`afterChange(newVal)`**：**若**提供，**MUST** 在实际提交 **`modelValue === newVal`** 成功后调用。

**若未提供 `asyncChange`**：在 **步骤 1、2** 通过后，**MUST** **同步**提交新值（等价于普通受控 **`ElSwitch`** 一次有效切换），并 **MUST** 在提交后执行 **步骤 4**（若提供 **`afterChange`**）。

#### Scenario: `asyncChange` 失败不切换

- **WHEN** **`modelValue=false`** 且用户切换到 **`true`**，**`asyncChange`** 返回 **reject** 的 **Promise**
- **THEN** 流程结束后 **`modelValue`** MUST 仍为 **`false`**，且开关视觉状态 MUST 与 **`false`** 一致

#### Scenario: 无 `asyncChange` 仍调用 `afterChange`

- **WHEN** **未**提供 **`asyncChange`**，且 **`beforeChange` 与确认**（若配置）均通过
- **THEN** 提交新值后 **MUST** 调用 **`afterChange(newVal)`**（若已提供 **`afterChange`**）

### Requirement: `beforeChange` 返回 `false` 完全静默

当 **`beforeChange`** 中止切换（返回 **严格 `false`** 或 **Promise resolve `false`**）时，组件 **MUST NOT** **`emit('cancel')`**；**MUST NOT** 进入确认或 **`asyncChange`**；**MUST NOT** 弹出组件内置的默认错误/警告提示（业务侧可自行在 **`beforeChange` 内**提示）。

#### Scenario: 与确认取消区分

- **WHEN** **`beforeChange`** 返回 **`false`**
- **THEN** 组件 MUST **不** **`emit('cancel')`**

### Requirement: 确认中止与 `cancel`

以下情形 **MUST `emit('cancel')`**：

- **`confirmMessage`** 路径：**`ElMessageBox`** 被用户 **取消**或 **关闭**导致 **reject**（与 **`C7Button`** 确认取消行为可对照）。
- **`confirmFn`** 路径：调用结果 **假值**（**`false`** / **`Promise` resolve 为假值**）视为用户中止确认。

**`confirmFn` 抛错或 `Promise` reject**：**MUST** **不**更新 **`modelValue`**；**MUST** **`emit('cancel')`**（与 **`confirmMessage` 用户取消** 一致，便于父组件统一处理「未提交」）；**MUST** 在 JSDoc 说明是否与 **`ElMessageBox` 使用相同的错误对象形态**。

#### Scenario: `confirmMessage` 取消触发 `cancel`

- **WHEN** 使用 **`confirmMessage`** 弹窗且用户点击 **取消**
- **THEN** 组件 MUST **`emit('cancel')`** 且 **不**更新 **`modelValue`**

### Requirement: `activeColor` / `inactiveColor` 与 CSS 变量

**`activeColor`** 与 **`inactiveColor`** MUST 通过 **CSS 自定义属性（变量）** 注入到 **`ElSwitch`** 可生效的作用域（变量名与挂载元素 **MUST** 在组件 JSDoc 说明）。

#### Scenario: 变量可被主题覆盖

- **WHEN** 父级或全局样式对约定变量赋新值
- **THEN** **`C7Switch`** 呈现的颜色 MUST 随之变化（在浏览器默认层叠规则下）

### Requirement: 事件与时序

组件 **MUST** 声明 **`update:modelValue`**、**`change`**、**`cancel`**；**MUST NOT** 为 **`asyncChange` 成功/失败** 单独新增 **`success` / `error`** 事件（与 **`design`** 一致）。

在 **实际提交新值成功** 时：

- **MUST** **`emit('update:modelValue', newVal)`**
- **随后 MUST** **`emit('change', newVal, oldVal)`**

其中 **`oldVal`** 为提交前一刻对外 **`modelValue`**。

#### Scenario: 成功路径 emit 顺序

- **WHEN** 一次切换从 **`oldVal`** 提交到 **`newVal`** 成功
- **THEN** 监听顺序上 **`update:modelValue`** MUST 先于 **`change`** 被触发

### Requirement: `disabled` 与重入

当 **`disabled=true`** 时，组件 **MUST NOT** 发起上述流水线（**不**调用 **`beforeChange` / `asyncChange` / `afterChange`**，**不**弹确认，**不** **`emit('change')` / `emit('cancel')`**）。

**`asyncChange` 执行中**（**loading** 为真）**MUST** 忽略新的切换意图或按 JSDoc 固定策略排队 **二选一**（实现 **MUST** 文档化）。

#### Scenario: disabled 不触发 `beforeChange`

- **WHEN** **`disabled=true`** 且用户点击开关区域
- **THEN** **不得**调用 **`beforeChange`**

## 验收映射（原始需求）

以下 **MUST** 成立，并与 **`原始需求/前端/C7开关.md`** 一致：

- **`asyncChange` 失败**时对外 **`modelValue`** **不**变，**UI** 与旧值一致。
- **`confirmMessage`** 配置后用户取消确认 **MUST `emit('cancel')`**。
- **`beforeChange` 返回 `false`**：**完全静默**，**不** **`emit('cancel')`**（见上文）。
