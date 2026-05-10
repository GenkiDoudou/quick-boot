## Context



- **quick-ui**：Vue 3 + Element Plus；业务页在开关上重复处理 **确认框**、**请求 loading**、**失败回滚** 与 **字典文案**。

- **原始需求**：**`原始需求/前端/C7开关.md`**。

- **家族对齐**：**`C7Button`** 已存在「**前置钩子 → 确认 → 异步执行 → 结束**」流水线心智（命名上开关使用 **`beforeChange`** / **`asyncChange`** 以贴合控件语义）。



## Goals / Non-Goals



**Goals**



- **值体系**：**`v-model`（`modelValue`）** 与 **`activeValue` / `inactiveValue`** 明确分离于「布尔-only」开关。

- **流水线**：**`beforeChange`** → **确认**（**`confirmFn` 优先于 `confirmMessage`**）→ 可选 **`asyncChange`**（**loading**；**仅成功路径**更新对外值）→ 可选 **`afterChange`**。

- **无 `asyncChange`**：在 **`beforeChange` 与确认**（若配置）通过后，行为等价于 **普通受控开关的一次有效切换**：**立即**（同步）提交新值，并 **MUST** 调用 **`afterChange(newVal)`**（若提供）。

- **文案**：支持 **`dictList`** 与 **`activeText` / `inactiveText`**；**字典优先**（见 **Decisions**）。

- **样式**：**`activeColor` / `inactiveColor`** → **CSS 变量**注入 **`ElSwitch`** 根或约定选择器（实现 JSDoc 写明变量名）。



**Non-Goals**



- 不规定业务接口 **URL**、**错误码映射** 或全局 **request** 封装细节（**`asyncChange` 由调用方提供**）。

- 不实现 **多选** 或 **非开关型** 控件（范围限定为 **Switch**）。



## Decisions（已与需求方确认）



1. **`beforeChange` 返回 `false`**  

   - **完全静默**：**不** **`emit('cancel')`**，**不**弹出默认错误/取消类提示，**不**进入确认与 **`asyncChange`**。  

   - **与「用户取消确认」区分**：后者 **MUST `emit('cancel')`**（见 **spec**）。



2. **`dictList` 与 `activeText` / `inactiveText` 优先级（字典优）**  

   - 对 **当前侧**（**激活侧 / 非激活侧**）展示文案：**若 `dictList` 中存在与对应 `activeValue` 或 `inactiveValue` 匹配的项**，**以该项 `label` 为准**。  

   - **若字典无匹配项**：**回退**到 **`activeText` / `inactiveText`**（显式文案兜底）。  

   - **理由**：字典为业务主数据口径；显式 props 作为迁移与特例兜底。



3. **无 `asyncChange` 时的行为**  

   - **是**：在流水线允许切换的前提下，**同步**更新 **`modelValue`**（与 **普通 `ElSwitch`** 受控更新一致），并 **MUST** 在值提交后调用 **`afterChange(newVal)`**（若提供）。



4. **事件集合与时序（按评审约定）**  

   - **仅** **`update:modelValue`**、**`change(newVal, oldVal)`**、**`cancel`**；**不**新增 **`success` / `error`**。  

   - 成功提交新值时：**MUST 先** **`emit('update:modelValue', newVal)`**，**再** **`emit('change', newVal, oldVal)`**。  

   - **`change`**：**仅在实际提交新值成功后**触发（含 **`asyncChange` resolve** 后的提交，与 **无 `asyncChange` 的同步提交**）。



5. **`confirmFn` 与 `confirmMessage`**  

   - 与原始需求一致：**若提供 `confirmFn`**，**不得**在未调用它的情况下直接使用 **`confirmMessage`** 弹窗。  
   - **中止确认**（**`confirmFn` 返回假值**，或 **`confirmMessage` 的 `ElMessageBox` 被用户取消/关闭**）**MUST `emit('cancel')`**。  
   - **唯一静默前置**：**`beforeChange` 返回严格 `false`**（见 **Decisions #1**）。

6. **与 Element Plus `ElSwitch` 集成**  

   - 实现 **MUST** 在 JSDoc 注明所依赖的 **Element Plus 主版本** 及是否使用 **`before-change`**；若 EP 行为与「失败不切换」冲突，**以实现层受控状态为准**，保证 **spec 验收**优先。



## Risks / Trade-offs



| 风险 | 缓解 |

|------|------|

| **EP `ElSwitch` 内部状态与 v-model 短暂不同步** | 以 **单一受控源**（实现内 **`committedValue`** 或等价）驱动渲染；**spec** 验收 **async 失败不回滚 emit** |

| **快速连点** | **loading** 或 **inFlight** 忽略重复切换；JSDoc 固定策略 |



## Migration Plan



- 新页使用 **`C7Switch`**；旧页逐步替换手写开关逻辑，无数据迁移。



## Open Questions



- （无）本轮 **`confirmFn` 假值** 已与 **`cancel`** 对齐；若后续需区分 **`veto`/`skip`**，另开变更扩展事件。


