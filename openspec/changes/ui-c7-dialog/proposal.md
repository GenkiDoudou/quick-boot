## Why

业务同时使用 **弹窗** 与 **抽屉**，但 **footer、确认/取消、异步提交 loading、v-model 写法** 分散在各页，难以统一。原始说明见 **`原始需求/前端/C7弹窗抽屉.md`**；需在 **quick-ui** 提供 **`C7Dialog`**（命名与原始需求一致），在 **`ElDialog` / `ElDrawer`** 之上收敛交互与受控显隐。

## What Changes

- 新增 **`C7Dialog`**：**`mode=dialog` | `drawer`** 分别承载 **`ElDialog` / `ElDrawer`**；统一 **标题区、默认插槽内容区、footer**（含 **`extra` 插槽** 与默认 **取消/确定**）。
- **显隐**：**`v-model:modelValue` 为主**；兼容 **`v-model:visible`**；关闭时 **同时** **`emit('update:modelValue', false)`** 与 **`emit('update:visible', false)`**（见 **design** 中双绑与 dev 提示规则）。
- **确定键**：若传 **`onConfirm`**，点击确定进入 **内部 loading**（与 **`confirmLoading` prop 覆盖** 规则见 **spec**）；**`onConfirm` resolve** 后 **自动关闭**；**reject** **不关闭**，错误提示由业务处理。未传 **`onConfirm`** 时仅 **`emit('confirm')`**，并兼容 **`emit('submit')`**。
- **卸载**：若仍打开，**仅**通过 **同步 v-model 为 `false`** 使父状态一致（见 **design**），不强制调用底层实例方法。
- **透传**：**`modalProps`** 等与 EP 对齐的透传；与内部默认 **冲突时以透传（调用方传入）为准**。
- **集成**：在 **`quick-ui/src/packages/index.js`** 导出并 **`installPackages`** 注册 **`C7Dialog`**；文档与 Dev 演示见 **tasks**。

## Capabilities

### New Capabilities

- **`ui-c7-dialog`**：**`C7Dialog`** 的 **`mode`、双 v-model、`footer`、默认 footer 与自定义 **`footer` 插槽、`onConfirm`、事件（含 `open`/`close` 等与 EP 一致转发）、`confirmLoading` 覆盖、卸载同步、透传冲突规则**。

### Modified Capabilities

- （无）新增 packages 能力；不修改后端契约。

## Impact

- **代码**：新增 **`quick-ui/src/packages/C7Dialog/`**；修改 **`packages/index.js`**。
- **文档**：本目录 **proposal / design / tasks** 与 **`specs/ui-c7-dialog/spec.md`**；**`docs`** 侧栏若已有 **C7Dialog** 链，实现阶段补全页面或修正死链。
- **依赖**：现有 **Vue 3 + Element Plus**，不新增 npm 包除非 **design** 论证必要。
