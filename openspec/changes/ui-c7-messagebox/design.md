## Context

- **quick-ui** 已广泛使用 **Element Plus**；部分包（如 **`C7Button`**、**`C7Switch`**）直接调用 **`ElMessageBox.confirm`**，标题等硬编码为「系统提示」等。
- 已定稿设计见仓库 **`docs/superpowers/specs/2026-05-08-c7-messagebox-design.md`**（brainstorming 输出）；本 **OpenSpec design** 与之对齐并补充实现向决策。

## Goals / Non-Goals

**Goals:**

- 提供 **函数式 API**，统一 **浅合并默认 options** 与 **`Promise<{ action, value? }>`** 结果。
- **`asyncConfirm`**：**确定按钮 loading** 优先；失败 **不关弹窗** + **`errorNotify`**。
- **`c7Loading`**：薄封装 **`ElLoading.service`**，返回 **`close`**。

**Non-Goals:**

- **不**封装 **`ElMessage` / `ElNotification`**。
- **不**在第一版批量迁移存量组件内部调用。

## Decisions

| 决策 | 选择 | 理由 |
|------|------|------|
| 模块落点 | **`packages/C7MessageBox/`** 纯 JS/TS 导出 | 与 **`C7Copy`** 等同属 **`packages`**，但无需 **`.vue`** 与 **`installPackages`** 注册。 |
| 默认合并 | **浅合并** `{ ...defaults, ...perCall }` | 与设计定稿一致；避免深合并与 EP 嵌套 options 的意外拷贝。 |
| EP reject | **内部捕获**，映射为 **resolve + `action`** | 满足「cancel/close 不抛」验收；**`action`** 与 **EP `MessageBoxData`** 对齐。 |
| async 路径 | **仅 `asyncConfirm`** | 避免与 EP 其它钩子组合产生二义。 |
| Loading 回退 | 先 **`confirmButtonLoading`**，否则 **`ElLoading.service`** | 优先保持焦点在弹窗内；回退策略在实现注释中标明触发条件。 |
| 扩展名 | 与 **`quick-ui`** 现有 **`packages`** 文件一致（**`.js`** 或 **`.ts`**） | 与仓库风格一致；若目录内已有 **TS** 则以 **TS** 为准。 |

## Risks / Trade-offs

| 风险 | 缓解 |
|------|------|
| **EP 大版本**变更 **`MessageBoxData` / reject 行为** | 实现时以 **`quick-ui/package.json`** 锁定版本为准写单测或手工清单；升级时回归 **`normalizeResult`**。 |
| **`asyncConfirm` 无 `errorNotify`** | 失败仍不关窗；**开发环境**可对 **`Error`** **`console.error`**（与 ESLint 约定一致即可），**不**静默吞错。 |
| **`beforeClose` 与内部关闭**竞态 | **`asyncConfirm`** 内控制 **loading** 与 **关闭** 顺序；避免业务同时传冲突的 **`beforeClose`**（文档注明不推荐组合）。 |

## Migration Plan

- **新增能力**：业务按需 **`import`**；存量代码 **可渐进**替换，无强制迁移窗口。
- **回滚**：删除 **`C7MessageBox`** 目录与 **`index.js`** 导出即可；未改存量组件则无回归面。

## Open Questions

- （无）实现阶段若 **EP** 某小版本 **`confirmButtonLoading`** 与 **`beforeClose`** 交互异常，在 **tasks** 执行中记录并必要时收窄文档中的组合用法。
