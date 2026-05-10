## 1. 组件与注册



- [x] 1.1 新增 `quick-ui/src/packages/C7Dialog/index.vue`：实现 **`mode`**、**双 v-model**（**主值 `modelValue` + dev warn**）、**默认 footer / `footer` 插槽 / `extra`**、**`onConfirm` + internal loading + `confirmLoading` 覆盖**、**无 `onConfirm` 时 `confirm` + `submit`**、**`cancel`**、卸载 **双 emit false**、**透传优先**、**EP `open/opened/close/closed` 等转发**

- [x] 1.2 **`packages/index.js`**：导出 **`C7Dialog`** 并在 **`installPackages`** 中全局注册



## 2. 与规格对齐校验



- [x] 2.1 对照 `openspec/changes/ui-c7-dialog/specs/ui-c7-dialog/spec.md` 核对 **关闭双 emit**、**`confirmLoading !== undefined` 覆盖**、**透传冲突**、**卸载同步**、**reject 不关**

- [x] 2.2 手工或 e2e：**dialog / drawer** 各至少一条 **打开 → 确定成功关 / 确定失败不关 / 取消**



## 3. 工程与健康



- [x] 3.1 `quick-ui` 生产构建通过（**`pnpm build:prod`** 或仓库等价脚本）



## 4. 可选：Dev / 文档



- [x] 4.1 Dev 演示页：覆盖 **双模式**、**`onConfirm`**、**`confirmLoading` 覆盖**、**自定义 `footer`**

- [x] 4.2 **`docs`** 侧栏 **`C7Dialog`** 链接：补全 **VitePress** 页面或修正死链


