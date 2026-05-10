## Context

- **quick-ui**：Vue 3 + Element Plus；**`C7Select`** 已约定 **`fetchData` / `fetchParams` / `resultKey` / `dataFormatter`** 与 **`response.data`** 解析起点（见 **`openspec/changes/ui-c7-select`**）。
- **原始需求**：**`原始需求/前端/C7单选框.md`**。
- **对话拍板**（相对探索结论的定稿）：
  1. **其余行为尽量以 props / 插槽配置**，不在实现里写死唯一策略。
  2. **数据契约与 `C7Select` 对齐**（静态优先级、**`fetchData` 合并、`resultKey`、`dataFormatter`**）；**单选专有**的 **`labelKey` / `valueKey`** 按原始需求保留。
  3. **根级 attrs 透传**至 **`ElRadioGroup`**（与 **`C7Select` → `ElSelect`** 同一技术模式）。
  4. **与 Element Form 透传规则一致**：表单项 **`prop` + `rules`** 与 **`ElRadioGroup`** 官方示例一致；**`C7Radio`** 不引入额外 form 魔法层。
  5. **边界默认策略**（可被 props 覆盖）：见下文 **Decisions** 与 **spec**。

## Goals / Non-Goals

**Goals**

- **静态**：**`dataList` 与 `options` 别名**；**同时存在时 `dataList` 优先**（与 **`C7Select`** 一致）。
- **异步**：**`fetchData(mergedParams)`**，**`mergedParams`** 至少为 **`fetchParams` 浅拷贝**；**单选组件不提供 `query` / `remote`**（不在 **`mergedParams` 中注入 `query` 键**）。
- **解析链**：自 **`response.data`** 起 → **`resultKey`**（点路径）→ **`dataFormatter`** → 行对象；行上取 label/value 使用 **`labelKey` / `valueKey`**（点路径或实现与 JSDoc 一致的解析方式）。
- **首次加载**：**`autoLoad`** 控制是否在 **挂载后** 自动发起一次 **`fetchData`**（无静态绑定且存在 **`fetchData`** 时才有意义）。**推荐默认值：`true`**（与 **`C7Select` 默认 `false`** 不同：单选组无「展开再拉」交互，首屏应尽早有选项；若调用方要手动 **`reload()`**，可设 **`autoLoad=false`**）。
- **样式切换**：**`radioStyle`** 驱动子节点使用 **`ElRadio` / `ElRadioButton` / border `ElRadio`**。
- **可观测性**：**`loading-change`**；**`defineExpose({ loading, reload })`**。

**Non-Goals**

- **不提供**关键字远程搜索（**无 `remote` / `remote-method` / `query` 约定**）。
- 不定义通用业务字典 HTTP 接口（仅 **`fetchData` + axios 习惯**）。

## Architecture Sketch

```
┌─────────────────────────────────────────────────────────────┐
│ C7Radio                                                      │
│  props: dataList/options, fetchData, fetchParams,           │
│         resultKey, dataFormatter, labelKey, valueKey,       │
│         autoLoad, radioStyle, fetchErrorBehavior,           │
│         invalidModelBehavior, emptyDisplay, …              │
├─────────────────────────────────────────────────────────────┤
│  内部 options 状态 ◀──── fetchData / 静态 props              │
│  行映射 ◀── labelKey / valueKey（在 normalize 之后）         │
├─────────────────────────────────────────────────────────────┤
│  ElRadioGroup（$attrs 透传：size/disabled/fill/…）           │
│    ├─ radioStyle=button → ElRadioButton ×N                  │
│    └─ 否则 ElRadio（border 由 radioStyle=border 决定）       │
└─────────────────────────────────────────────────────────────┘
```

## Decisions

1. **保留字段（不参与透传）**  
   实现 MUST 维护 **`RESERVED_ATTR_KEYS`**（或等价），至少包含：**`dataList`、`options`、`fetchData`、`fetchParams`、`resultKey`、`dataFormatter`、`labelKey`、`valueKey`、`autoLoad`、`radioStyle`** 以及 spec 中列出的 **行为配置 props**；其余 **attrs 键** 进入 **`ElRadioGroup`**。

2. **`fetch` 失败策略（单一 prop）**  
   使用 **`fetchErrorBehavior`** 枚举（见 **spec**），避免多个布尔交叉。默认推荐：**保留上一轮成功选项**（若有），**不清空外部 v-model**。

3. **当前值不在选项中（选项已就绪后）**  
   使用 **`invalidModelBehavior`** 枚举（见 **spec**）。**默认**：**不改写**外部 **`modelValue`**；**开发环境** **`console.warn` 一次**（可通过 prop **关闭**）。

4. **空选项 UI**  
   使用 **`emptyDisplay`**：**`'none' | 'text' | 'slot'`**；**`'text'`** 时显示 **`emptyText`**；**`'slot'`** 时使用 **`#empty`**（实现将 **`empty`** 插槽渲染在 **`ElRadioGroup` 外或内**的固定占位，并在 JSDoc 标明 DOM 位置）。

5. **`change` 载荷**  
   **`change(value)`** 与 **`update:modelValue`** 的 **value 形态一致**（单选无 **`separator`**）。

6. **竞态**  
   **`fetchData`** 多次并发（如快速 **`reload()`**）采用 **last-write-wins** 更新内部选项列表。

## Risks / Trade-offs

| 风险 | 缓解 |
|------|------|
| **`ElRadioGroup` 无原生 `empty` 插槽** | 由 **`C7Radio` 外层**提供 **`empty`** 插槽约定，并在 JSDoc 配图/说明 DOM 结构 |
| **与 `C7Select` 的 `autoLoad` 默认值不同** | 在 **proposal / design** 已写明理由；组件 JSDoc **高亮** |

## Migration Plan

- 新表单/字典页优先 **`C7Radio`**；旧页可逐步替换手写 **`ElRadioGroup` + onMounted 请求**。

## Open Questions

- （无）边界默认值已在 **spec** 以枚举 + 默认项写死；若产品后续要「失败自动 Toast」，可在实现层通过 **`@loading-change` + 业务封装** 完成，不强制进入本组件。
