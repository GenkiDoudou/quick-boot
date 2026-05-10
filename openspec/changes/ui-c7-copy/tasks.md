## 1. 组件与注册

- [x] 1.1 新增 `quick-ui/src/packages/C7Copy/index.vue`：实现 **`text`（String/Number，`null`/`undefined` 安全）**、**`getCopyText`（含 Promise）**、**`mode`（`button` / `icon` / `text` / `clickable`，**`none` 别名**）**、**`disabled`**、**`showMessage` + `notify`**、**`beforeCopy` / `afterCopy`**；复制链 **Clipboard `writeText` 优先**、失败或不满足上下文时 **`execCommand('copy')` 降级**；**`copy` / `success` / `error`** 与 **design 中的时序**一致；异步复制 **重入忽略**
- [x] 1.2 **`packages/index.js`**：导出 **`C7Copy`** 并在 **`installPackages`** 中全局注册 **`C7Copy`**

## 2. 与规格对齐校验

- [x] 2.1 对照 `openspec/changes/ui-c7-copy/specs/ui-c7-copy/spec.md` 核对 **`beforeCopy === false`** 时 **无 `copy`/`success`/`error`/`afterCopy`**、**`disabled`** 无 emit、**`notify` 与 `showMessage` 分支**
- [x] 2.2 验收：**安全上下文** 下 **Clipboard 成功** → **`success` + 提示**（按 **`showMessage`/`notify`**）
- [x] 2.3 验收：**禁用 Clipboard**（或强制走降级路径）时 **`execCommand` 成功** → 仍 **`success`**

## 3. 工程与健康

- [x] 3.1 `quick-ui` 生产构建通过（**`pnpm build:prod`** 或仓库等价脚本）

## 4. 可选：Dev / 文档

- [x] 4.1 增加 Dev 演示页（至少覆盖 **`button` / `icon` / `text` / 插槽`**、**`getCopyText` Promise**、**`beforeCopy` 阻止**、**`notify`**），路由与文件位置由实现者选定并在说明中注明
- [x] 4.2 若 **`docs`** 侧栏已存在 **C7Copy** 链接：补全对应 **VitePress** 页面或修正死链
