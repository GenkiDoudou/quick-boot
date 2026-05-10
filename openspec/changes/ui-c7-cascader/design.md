## Context

- **仓库**：`quick-ui` 已存在 **`C7Select`**、**`C7TreeSelect`** 等封装，约定 **`dataList`/`options`**、**`fetchData`/`fetchParams`**、**`resultKey`/`dataFormatter`**、**`autoLoad`**、**`separator`**、**`valueType`**、**`load-error`/`loading-change`** 等。
- **输入**：定稿 **`docs/superpowers/specs/2026-05-08-c7-cascader-design.md`** 与 **`原始需求/前端/C7级联选择器.md`**。
- **约束**：与 **Element Plus `ElCascader`** 版本对齐；**不 fork** 级联面板；实现为 **单文件** **`index.vue`**，结构与 **`C7TreeSelect`** 可读性对齐。

## Goals / Non-Goals

**Goals:**

- 提供 **`C7Cascader`**，覆盖 **静态**、**整树异步**、**懒加载扁平子列表** 三种数据来源。
- **`fetchData`**：整树 **`{ ...fetchParams }`**；懒加载 **`{ parentId, ...fetchParams }`**，根层 **`parentId === rootParentId`**。
- **`mapTree`** + **`lazyLoad`** 桥接 EP；**`v-model` 适配** 处理 **`valueType`** 与 **`multiple + separator`**（在允许的组合下）。
- 与 **`C7TreeSelect`** 一致的错误与加载事件、**`fetchGeneration`** 防竞态。

**Non-Goals:**

- 不在本期实现「任意 **`emitPath`** 下 **`separator` 逗号串可逆解析**」。
- 不强制与 **`C7Button`** 流水线耦合；不修改后端 API。

## Decisions

1. **单文件实现**  
   - **选型**：全部逻辑放在 **`C7Cascader/index.vue`**。  
   - **理由**：与已定稿 superpowers 设计一致，与 **`C7TreeSelect`** 维护方式一致。  
   - **备选**：抽 `useC7CascaderData` — 待与树选重复度升高再抽。

2. **`resultKey` 仅表示响应取列表路径**  
   - **选型**：与 **`C7Select`/`C7TreeSelect`** 相同；子节点字段名只用 **`childrenKey`**。  
   - **理由**：避免与原始需求稿中「resultKey(children)」混读。

3. **`separator` 与 `emitPath` / 多维值**  
   - **选型**：当 **`emitPath === true`** 或内部值为 **非一维标量数组**（如路径嵌套）时，**`separator` 不生效**，对外保持 **EP 数组**；**DEV** **`console.warn`**。  
   - **理由**：逗号拼接无法无损逆解析；不静默丢数据。

4. **`rootParentId` 默认值**  
   - **选型**：默认 **`null`**（JSDoc 写明；若后端根为 **`0`** 或 **`''`**，由业务显式传入 **`rootParentId`**）。  
   - **理由**：与常见「根无父」语义一致；避免误用 **`0`** 误判。

5. **`lazy` 与 `fetchData`**  
   - **选型**：**`lazy === true`** 且存在 **`fetchData`** 时，由组件提供 **`lazyLoad`** 实现；**不**在懒模式下依赖 **`onMounted` 整树 `executeFetch`**（除非 EP 仍需初始根数据且与设计一致时再在 tasks 中写死一条：仅根由 lazy 拉取）。  
   - **理由**：与设计 §4、§6 一致。

6. **`loading` 展示**  
   - **选型**：若 **`ElCascader`** 当前版本支持 **`loading` prop**，则 **`mergedLoading = internalInFlight || attrs.loading`**（与树选一致）；若不支持，则 **`loading-change`** 仍暴露，UI 由业务用 **`attrs`** 或其它方式处理 — 实现前在代码中 **查 EP 类型定义** 二选一落代码。  
   - **理由**：避免假设不存在的 prop。

## Risks / Trade-offs

- **[Risk] `ElCascader` 的 `lazyLoad` 签名与节点字段** 与版本绑定 → **Mitigation**：实现时以 **`node`、`resolve`** 为准阅读当前 **element-plus** 文档/类型；在组件 JSDoc 中写明 **`parentId` 取自映射后的 `value`**。  
- **[Risk] 多选 + 路径 + `valueType` 组合复杂** → **Mitigation**：spec 中只要求 **标量层** 的 coerce 行为；边界场景以 **DEV warn** + 文档表格说明。  
- **[Risk] 与 `C7TreeSelect` 行为细微不一致** → **Mitigation**：实现阶段对 **`executeFetch` / `outerToInner`** 等对照树选 diff，必要时在 tasks 中列「对齐检查」子项。

## Migration Plan

- **部署**：随 **`quick-ui`** 发版；业务按需将页内 **`ElCascader`** 逐步替换为 **`C7Cascader`**。  
- **回滚**：移除引用即可；无数据库迁移。

## Open Questions

- （无）**`rootParentId`** 默认值已在 Decisions 中定为 **`null`**；若产品要求默认 **`0`**，可在实现 PR 中变更并同步 spec 一句。
