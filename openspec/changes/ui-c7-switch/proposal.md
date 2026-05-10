## Why



列表与表单中「开关型」字段常需要：**切换前校验**、**二次确认**、**异步提交**与**失败时保持原值**。各页在 `ElSwitch` 上重复拼装 `MessageBox`、loading 与 v-model 回滚，行为不一致。原始说明见 **`原始需求/前端/C7开关.md`**；需在 **quick-ui** 内提供统一的 **`C7Switch`**。



## What Changes



- 新增 **`C7Switch`**：基于 **`ElSwitch`** 封装 **`modelValue` / `activeValue` / `inactiveValue`** 值体系；**`activeText` / `inactiveText`** 与 **`dictList[{label,value}]`** 文案映射（**字典优先、显式文案兜底**，见 **design**）。

- **点击流水线**：**`beforeChange(newVal)`**（返回 **`false`** 时**完全静默**中止）→ **确认**（**`confirmFn` 优先**，否则 **`confirmMessage`** 弹窗）→ 可选 **`asyncChange(newVal)`**（执行期 **loading**；**成功**才提交 **`modelValue`**；**失败不 emit 更新**即视觉/值保持旧态）→ 可选 **`afterChange(newVal)`**（**无 `asyncChange` 的同步提交路径 MUST 同样触发**，见 **spec**）。

- **颜色**：**`activeColor` / `inactiveColor`** 通过 **CSS 变量**注入（与原始需求一致）。

- **事件**：**`update:modelValue`**、**`change(newVal, oldVal)`**、**`cancel`**；**不**新增 **`success` / `error`** 事件。成功提交时 **MUST 先 `update:modelValue` 再 `change`**（见 **spec**）。

- **集成**：在 **`quick-ui/src/packages/index.js`** 中导出并 **`installPackages`** 全局注册 **`C7Switch`**。



## Capabilities



### New Capabilities



- **`ui-c7-switch`**：**`C7Switch`** 的 props、流水线语义、**`dictList` 与显式文案优先级**、**`beforeChange(false)` 静默**、**确认取消 → `cancel`**、**`asyncChange` 失败不回滚 emit**、**事件时序**与验收标准（对齐 **`原始需求/前端/C7开关.md`** 与本轮 **design 决策**）。



### Modified Capabilities



- （无）新增前端 packages 能力；不修改后端或其它已发布 spec 的对外契约。



## Impact



- **代码**：新增 **`quick-ui/src/packages/C7Switch/`**（至少 **`index.vue`**）；修改 **`quick-ui/src/packages/index.js`**。

- **文档**：本变更目录下 **proposal / design / tasks** 与 **`specs/ui-c7-switch/spec.md`**；**`docs`** 侧栏若需 **C7Switch** 说明页，实现阶段补全。

- **依赖**：以现有 **Vue 3**、**Element Plus**（**`ElSwitch`**、**`ElMessageBox`** 等）为准；实现 MAY 使用 **`ElSwitch` 的 `before-change`** 或等价受控逻辑，**须在 design 中固定与 EP 主版本对齐的约束**。


