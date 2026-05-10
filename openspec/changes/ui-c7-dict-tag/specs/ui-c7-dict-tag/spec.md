# ui-c7-dict-tag

## Purpose

为 **quick-ui** 提供 **`C7DictTag`**：基于 **`options` + `modelValue`** 将字典 **value** 展示为一枚或多枚 **`ElTag`**（及空态 **`-`**），支持 **多值**、**`max` 折叠（`+N`）**、**`collapse` tooltip**、**`+N` 可点展开**、**未匹配展示策略** 与 **字典类型驱动 `ElTag.type`**。需求来源：**`原始需求/前端/C7字典标签.md`**；行为澄清来自变更内 **design.md**。

## ADDED Requirements

### Requirement: 组件与注册位置

**`C7DictTag`** MUST 位于 **`quick-ui/src/packages/C7DictTag`**（至少包含 **`index.vue`**），并通过 **`quick-ui/src/packages/index.js`** 的 **`installPackages(app)`** 注册为全局组件 **`C7DictTag`**；亦 MAY **`import { C7DictTag } from '@/packages'`** 按需使用。

#### Scenario: 全局注册可用

- **WHEN** 应用已执行 **`installPackages(app)`**
- **THEN** 模板中 MUST 能使用 **`<C7DictTag />`** 而无需逐页 **import** 注册

### Requirement: `modelValue` 形态与解析

组件 MUST 接受 **`modelValue`** 为 **`number`**、**`string`**、**`string[]`** 或 **`number[]`**。当 **`modelValue`** 为 **`string`** 且包含 **`separator`** 时，MUST 按 **`separator`** 拆分为 **原子值数组**（与 **`C7Select`** 的 **`separator`** 语义对齐；默认 **`','`** 以 **design** 为准）；**`string`** 不含分隔符时 MUST 视为 **单原子**。**`null`** 与 **`undefined`** MUST 视为 **无原子值**（走空态）。**`number`** MUST 视为 **单原子**。组件为 **只读展示**，MUST **不** **`emit('update:modelValue')`**。

#### Scenario: 逗号串解析为两枚 tag

- **WHEN** **`options`** 含 **`value`** 为 **`'1'`** 与 **`'2'`** 的项，**`modelValue`** 为 **`'1,2'`** 且 **`separator`** 为 **`','`**
- **THEN** 界面 MUST 展示 **两枚** 对应 **`label`** 的 **`ElTag`**（在 **`max<=0`** 或未限制时）

### Requirement: 匹配规则

对每个 **原子值 `val`**，组件 MUST 在 **`options`** 中选取 **首个** 满足 **`String(opt.value) === String(val)`** 的 **`opt`**，并以其 **`label`** 作为展示文案。

#### Scenario: 数字与字符串等价匹配

- **WHEN** **`options`** 中某项 **`value`** 为数字 **`1`**，**`modelValue`** 原子为字符串 **`'1'`**
- **THEN** 该项 MUST 判定为 **已匹配** 并展示该项 **`label`**

### Requirement: 整体空态

当解析后 **原子值列表为空**（含 **`modelValue`** 为 **`null` / `undefined` / ''**、或拆分后无非空片段）时，组件 MUST 展示 **单个 `-` 字符**（**U+002D HYPHEN-MINUS**）作为内容，且 MUST **不**展示 **`ElTag`**。

#### Scenario: null 显示横杠

- **WHEN** **`modelValue`** 为 **`null`**
- **THEN** 组件 MUST 渲染 **`-`** 且无 **`ElTag`**

### Requirement: 重复原子值不去重

解析后的 **原子值数组** MUST **不去重**；同一 **`value`** 出现 **k** 次则 MUST 参与 **k** 次匹配与 **k** 次独立展示（在 **`max`** 规则允许范围内）。

#### Scenario: 两枚相同 value 两枚 tag

- **WHEN** **`modelValue`** 为 **`'1,1'`** 且 **`options`** 存在 **`value`** **`'1'`** 的项，且 **`max<=0`**
- **THEN** 界面 MUST 展示 **两枚** 该 **`label`** 的 **`ElTag`**

### Requirement: 未匹配与 `showValue`

当某原子 **未匹配** 到 **`options`**：

- **`showValue=true`** 时，该原子位置 MUST 渲染 **`ElTag`**，且 **`type`** MUST 为 **`info`**，文案为 **`String(val)`**（**`val`** 为该原子原始值）。
- **`showValue=false`** 时，该原子位置 MUST 渲染 **`-`**（与 **整体空态** 相同字符）。

#### Scenario: showValue=false 的未匹配为横杠

- **WHEN** 某原子 **无**匹配项且 **`showValue=false`**
- **THEN** 该位置 MUST 展示 **`-`**

#### Scenario: showValue=true 的未匹配为 info tag

- **WHEN** 某原子值为 **`'x'`** 且无匹配项且 **`showValue=true`**
- **THEN** 该位置 MUST 展示 **`type=info`** 的 **`ElTag`**，且可见文案 MUST 包含 **`x`**

### Requirement: `max` 与 `+N` 仅作用于已匹配序列

当 **`max`** 为 **大于 0** 的整数时，组件 MUST 仅对 **已匹配** 原子建立序号 **`mk`**（从 **1** 开始按 **原子顺序** 对 **已匹配** 递增）。对每个 **已匹配** 原子：

- 若 **`mk <= max`**，MUST 在该原子位置渲染 **字典类型映射后的 `ElTag`**（**`label`**）。
- 若 **`mk === max + 1`**，MUST 在该原子位置渲染 **唯一一枚 `+N`**，其中 **`N`** 为 **已匹配总数 − `max`**，且 **`N`** MUST **大于 0**。
- 若 **`mk > max + 1`**，MUST **不**在该原子位置渲染任何可见节点（**不占布局空间**）。

**`max`** 为 **`0`、负数或未传** 时，MUST **不**应用上述折叠规则（即 **全部已匹配** 原子均渲染 **`ElTag`**，**无 `+N`**）。

#### Scenario: max 为 1 时出现 +N

- **WHEN** 已匹配原子有 **3** 个且 **`max`** 为 **`1`**
- **THEN** 界面 MUST 恰好展示 **1** 枚正常 **`ElTag`** 与 **1** 枚 **`+2`**（或 **`+N`** 文案与 **`N=2`** 等价），且 MUST **不**为第三枚已匹配原子单独展示 **`ElTag`**

#### Scenario: 未匹配不参与 +N

- **WHEN** **`max`** 为 **`1`** 且原子序列为 **「已匹配、未匹配(`showValue=false`)、已匹配、已匹配」**
- **THEN** **`+N`** 的 **`N`** MUST **仅**由 **已匹配超出部分** 计数，**未匹配** 位置的 **`-`** MUST **不**计入 **`N`**

### Requirement: `+N` 可点与溢出列表

当 **`collapse=false`**（或未启用仅 tooltip 模式）时，**`+N`** **MUST** 可通过 **鼠标点击** 展开，并 **MUST** 以 **Popover/Dropdown** 等可交互容器展示 **所有 `mk > max` 的已匹配项** 的 **`label`** 列表（顺序与 **原子序** 中这些项的出现顺序一致）。

#### Scenario: 点击 +N 可见剩余 label

- **WHEN** 存在 **`+N`** 且 **`collapse=false`**
- **THEN** 用户点击 **`+N`** 后 MUST 能读取到 **超出 `max`** 的各 **`label`**

### Requirement: `collapse` 与 tooltip

当 **`collapse=true`** 时，**`+N`** **MUST** 具备 **tooltip**（**`ElTooltip`** 或项目内等价），并在 **hover（或键盘聚焦，若实现提供）** 时展示 **同上溢出 `label`** 列表。

#### Scenario: collapse 时 hover 可见溢出 label

- **WHEN** **`collapse=true`** 且存在 **`+N`**
- **THEN** 用户将指针悬停于 **`+N`** 时 MUST 能看到 **溢出项 `label` 列表**

### Requirement: `dictType` 与 `ElTag.type`

组件 MUST 提供 **`dictType`**（具体 prop 名实现与 **JSDoc** 一致，此处以 **`dictType`** 为准）用于选择 **`ElTag`** 的 **`type`**。**未匹配 info tag`** 的 **`type`** MUST **始终**为 **`info`**，**不受 `dictType` 影响**。

对 **已匹配** 的 **`ElTag`**，组件 MUST 将 **`dictType`** 映射到 **Element Plus** 允许的 **`type`**；当 **`dictType`** **无**映射条目时，**`type`** MUST **fallback** 为 **`primary`**。

#### Scenario: 未知 dictType 回退 primary

- **WHEN** **`dictType`** 为 **实现未收录的字符串** 且某原子 **已匹配**
- **THEN** 对应 **`ElTag.type`** MUST 为 **`primary`**

### Requirement: `effect` / `round` / `size`

组件 MUST 将 **`effect`**、**`round`**、**`size`** **透传**至 **所有**用于展示字典 **`label`** 的 **`ElTag`**（含 **`+N`** 若实现为 **`ElTag`** 形态）；**`info` 未匹配 tag`** 亦 MUST **透传**上述 props。

#### Scenario: size 生效于字典 tag

- **WHEN** **`size`** 设为 **`small`** 且存在 **已匹配** 展示
- **THEN** 字典 **`ElTag`** MUST 呈现 **小尺寸**（与 **Element Plus `ElTag` size** 语义一致）

### Requirement: 验收对齐（与原始需求一致）

在 **`showValue=true`** 且某原子 **无匹配项** 时，组件 MUST **仍**展示 **info `ElTag`**。

在 **`modelValue`** 为 **`'1,2'`**、**`options`** 覆盖 **`1`/`2`**、**`max=1`** 时，界面 MUST 出现 **`+1`**（或 **`N=1`** 的等价 **`+N`** 文案）。

#### Scenario: 原始需求双 tag

- **WHEN** **`modelValue`** 为 **`'1,2'`** 且 **`options`** 完整匹配两值且 **`max<=0`**
- **THEN** MUST 展示 **两枚** 对应 **`label`** 的 **`ElTag`**
