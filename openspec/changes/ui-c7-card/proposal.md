## Why

业务页面需要 **统一的内容卡片外观**，且多数卡片依赖 **可折叠/展开** 控制信息密度；标题区（色块、字号、加粗）若各页面自行拼 `ElCard`，样式与交互易分裂。原始说明见 `原始需求/前端/C7卡片.md`。

## What Changes

- 在 **quick-ui** 提供 **`C7Card`**：基于 **Element Plus `ElCard`**，封装 **默认标题栏**（可选色块 + 标题文案 + 字号档位 + 加粗）、**右侧 `extra` 与折叠触发器**，并透传 **`shadow`** 等与 `ElCard` 一致的属性（见 design / spec）。
- **折叠**：**`collapsible`** 为总开关；**`defaultExpanded`** 与 **`v-model`（`modelValue`）** 支持非受控/受控；**`update:modelValue`**、**`change(expanded)`**；**`defineExpose`** 暴露 **`toggle` / `expand` / `collapse`**。
- **过渡**：内容区折叠使用 **fade** 过渡（见 design：仅 body，header 不参与隐藏）。
- **插槽**：**`header`**（整头自定义）、**`extra`**、**`toggle`**（替换默认折叠控件）、默认内容区。
- **`showColorBlock` / `colorBlockColor`**，并兼容别名 **`isShowColorBlock`**（解析规则见 spec）。
- **`textSize`（h1~h5）** 映射到 **Element Plus 标题类/变量**（见 design）。
- 在 **`packages/index.js`** 中导出并随 **`installPackages`** 全局注册 **`C7Card`**。

## Capabilities

### New Capabilities

- **`ui-c7-card`**：**`C7Card`** 的默认头与 **`#header` 完全自定义** 的优先级、**`collapsible` / `v-model` / 方法** 语义、**`#toggle` 插槽** 与 **`collapsible` 的关系**、**色块 props 与别名**、**内容区 fade**、**无障碍（内置 button + `aria-*`，`#toggle` slot props）** 及验收标准。

### Modified Capabilities

- （无）新增前端 packages 能力；不修改既有防火墙/通用后端等规格。

## Impact

- **代码**：新增 `quick-ui/src/packages/C7Card/index.vue`（或同目录结构）；修改 `quick-ui/src/packages/index.js`（导出 + 全局注册）；可选 Dev 演示路由/页面（见 tasks）。
- **文档**：本变更目录下 `proposal` / `design` / `tasks` / `specs/ui-c7-card/spec.md`。
- **依赖**：以现有 **`element-plus`** 为准；不新增 npm 包（除非实现时发现缺口，再于 tasks 中说明）。
