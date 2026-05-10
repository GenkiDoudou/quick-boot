## Context

- **quick-ui**：Vue 3 + Element Plus；业务页大量使用 **`ElCard`** 但标题区、折叠与 **`extra`** 各写一套，视觉与交互不一致。
- **原始需求**：**`原始需求/前端/C7卡片.md`**。
- **proposal** 已定：基于 **`ElCard`** 封装 **`C7Card`**，默认头 + 插槽覆盖、折叠与 **fade**、色块与字号档位、**`shadow`** 透传。

## Goals / Non-Goals

**Goals**

- **默认头部**：左侧 **可选色块** + **`label`** 文案；**`textSize`**（**`h1`~`h5`**）映射到 **Element Plus 文档中与标题层级对应的语义**（实现选用 **`el-text`** 的 **`type`/`size`** 组合或 **`class` 映射表** 之一，在 JSDoc 固定并列表）。
- **右侧**：**`#extra`** 与 **折叠触发器**（当 **`collapsible=true`**）。
- **完全自定义头**：存在 **`#header`** 时 **MUST 覆盖**默认头布局（**`label` / 色块 / 默认折叠控件** 均不渲染；**`#toggle` 是否仍生效**见 **Decisions**）。
- **折叠**：**`collapsible`**；**`defaultExpanded`** 非受控初值；**`v-model`（`modelValue`）** 受控；**`update:modelValue`**、**`change(expanded)`**；**`defineExpose({ toggle, expand, collapse })`**。
- **过渡**：**内容区**（默认插槽外层包裹）**折叠使用 `transition` fade**；**`ElCard` 的 header 区域**不因折叠而整体卸载（仅 **body** 参与显隐），避免标题闪动。
- **Props 别名**：**`isShowColorBlock`** 与 **`showColorBlock`** 的合并规则见 **spec**（单一布尔生效策略）。
- **透传**：**`shadow`** 及 **`ElCard`** 其余合法 props（除本组件保留键外）透传至 **`ElCard`**。

**Non-Goals**

- 不在本变更内规定 **全局主题 Token** 改造或 **设计系统 Figma** 对齐流程。
- 不实现 **嵌套卡片递归** 或 **路由级** 懒加载（仅组件级折叠）。

## Decisions

1. **`#header` 与 `#toggle` 关系**  
   - **当使用 `#header`**：视为调用方 **完全接管头部**；**默认折叠按钮不显示**。若仍需折叠，调用方在 **`#header` 内**自行放控件，或 **不使用 `#header`** 而改用 **`#toggle`** 替换默认折叠控件。  
   - **理由**：避免「自定义头 + 隐式默认按钮」叠两层难预测。

2. **`textSize` 映射**  
   - 推荐：使用 **`el-text`** 包裹标题，`size` 与 **`type`** 或 **`class`** 组合实现 **h1~h5** 五档；**`isBold`** 映射 **`tag`/`bold`** 或与 **`font-weight`** 等价效果。  
   - **备选**：纯 **`class`** 映射到项目内已有标题工具类（若不存在则仅用 EP 内置）。

3. **折叠实现**  
   - 内部 **`expanded`** 状态；**`v-model`** 与 **`defaultExpanded`** 的 **受控/非受控** 判定与 **`ElCollapse` 无关**，采用与常见 **可折叠面板** 相同模式（**首次**以 **`defaultExpanded`** 初始化；存在 **`modelValue` 绑定**时走受控）。

4. **无障碍**  
   - 默认折叠控件 **MUST** 为 **`button type="button"`**，并设置 **`aria-expanded`**、**`aria-controls`**（指向内容区 **`id`**，由组件生成稳定 **`id`**）。  
   - **`#toggle` 插槽** **SHOULD** 通过 **slot props** 下发 **`expanded` / `toggle` / `expand` / `collapse`**，便于自定义控件接入相同 **ARIA**（若调用方完全自定义节点，**ARIA 责任**转移至调用方，JSDoc 须注明）。

5. **`expandText` / `collapseText`**  
   - 应用于 **默认折叠按钮** 的可见文案（或 **`title` 提示`** 二选一，实现固定一种并在 JSDoc 写明）；**`#toggle` 自定义**时 **不强制**使用这两 prop。

## Risks / Trade-offs

| 风险 | 缓解 |
|------|------|
| **EP 版本差异（`ElCard` 插槽名）** | 以 **`quick-ui` 锁定的 element-plus** 为准；实现注释标明依赖插槽 |
| **`#header` + `collapsible` 期望默认按钮** | 在 **spec** 写死「无默认按钮」；文档示例展示 **slot props** 用法 |

## Migration Plan

- 新页直接使用 **`C7Card`**；旧页可逐步把「手写 `ElCard` + 折叠」迁移过来，**无数据迁移**。

## Open Questions

- （无）**`colorBlockColor`** 接受 **`string`**（CSS 颜色）；非法值时浏览器表现为无效色，组件 **不**强制校验。
