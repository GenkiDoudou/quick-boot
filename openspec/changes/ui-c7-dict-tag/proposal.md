## Why

列表与详情中常需把字典 **value** 展示为 **label** 标签，并支持多值、折叠与字典类型样式；各页自行拼 **`ElTag`** 与拆分逻辑易不一致。原始说明见 **`原始需求/前端/C7字典标签.md`**；需在 **quick-ui** 内提供统一的 **`C7DictTag`**（与探索阶段澄清的 **`modelValue` / 重复值 / `max` 仅作用于已匹配项 / `+N` 可点 / 字典类型驱动 tag 类型** 对齐）。

## What Changes

- 新增 **`C7DictTag`**：**`modelValue`** 支持 **number / string / array / 逗号分隔 string**；**`options`** 为 **`{ label, value, ... }[]`**；匹配规则 **`String(opt.value) === String(单值)`**。
- **多值**：按 **`separator`** 拆分逗号串（与 **`C7Select` / `C7Checkbox`** 的 **`separator`** 语义对齐）；**不去重**，同一 **value** 出现几次则渲染几枚匹配 tag。
- **`max > 0`**：仅对 **已成功匹配** 的项构成的序列做「前 **`max`** 个 + **`+N`**」；**`+N`** 在 **`collapse=false`** 时仍可 **点击** 展开剩余项（具体交互见 **design**）。
- **`collapse=true`**：**`+N`** 使用 **tooltip** 等形式展示超出项的 **label** 列表（与原始需求一致；与「可点」组合方式见 **design**）。
- **未匹配**：**`showValue=true`** 时以 **info** 风格 **tag** 展示原始值；**`showValue=false`** 时该项展示与 **整体空值** 相同，为 **`-`**。
- **空值**：在 **无有效原子值**（经解析与 trim 后）时展示 **`-`**。
- **字典类型**：通过 **`dictType`**（或 design 中最终 prop 名）将 **业务字典类型** 映射到 **`ElTag` 的 `type`**（及与 **`effect` / `round`** 的组合规则见 **design**）。
- **集成**：在 **`quick-ui/src/packages/index.js`** 中导出并 **`installPackages`** 全局注册；可选 Dev 演示页与测试（见 **tasks**）。

## Capabilities

### New Capabilities

- **`ui-c7-dict-tag`**：**`C7DictTag`** 的 **props**（**`options` / `modelValue` / `separator` / `showValue` / `max` / `collapse` / `dictType` / `size` / `effect` / `round`** 等）、**值解析与匹配**、**`max` 仅约束已匹配序列**、**未匹配与空值展示**、**`+N` 可点与 `collapse` tooltip**、**字典类型 → `ElTag` type** 的验收标准。

### Modified Capabilities

- （无）新增前端 packages 能力；不修改已归档主 spec 的对外契约。

## Impact

- **代码**：新增 **`quick-ui/src/packages/C7DictTag/`**（至少 **`index.vue`**）；修改 **`quick-ui/src/packages/index.js`**。
- **文档**：本变更目录下 **proposal / design / tasks** 与 **`specs/ui-c7-dict-tag/spec.md`**；**VitePress** 侧若需组件说明，在实现阶段按既有 C7 文档结构补充。
- **依赖**：以现有 **Vue 3**、**Element Plus**（**`ElTag`**、**`ElTooltip` / `ElPopover`** 等按需）为准，不新增专用 npm 包除非 **design** 论证必要。
