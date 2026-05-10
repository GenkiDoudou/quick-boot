## 1. 组件与注册



- [x] 1.1 新增 `quick-ui/src/packages/C7Switch/index.vue`：实现 **`modelValue` / `activeValue` / `inactiveValue`**、**`dictList`（字典优先）+ `activeText`/`inactiveText` 兜底**、**`activeColor`/`inactiveColor` CSS 变量**、流水线 **`beforeChange` → 确认（`confirmFn` 优先）→ 可选 `asyncChange`（loading）→ 可选 `afterChange`**；**无 `asyncChange` 时同步提交并调用 `afterChange`**；事件 **`update:modelValue` → `change`**、**`cancel`**；**`beforeChange===false` 静默**；对齐 `openspec/changes/ui-c7-switch/specs/ui-c7-switch/spec.md`

- [x] 1.2 **`packages/index.js`**：导出 **`C7Switch`** 并在 **`installPackages`** 中全局注册 **`C7Switch`**



## 2. 与规格对齐校验



- [x] 2.1 对照 **spec**：**`asyncChange` reject** 后 **`modelValue` 与 UI 不变**；**`confirmMessage` 取消** **`emit('cancel')`**；**`beforeChange` false** **不** **`emit('cancel')`**

- [x] 2.2 对照 **spec**：**字典命中** 覆盖显式 **`activeText`**；**字典未命中** 回退显式

- [x] 2.3 对照 **spec**：成功提交时 **`update:modelValue` 先于 `change`**



## 3. 工程与健康



- [x] 3.1 `quick-ui` 生产构建通过（**`pnpm build:prod`** 或仓库等价脚本）



## 4. 可选：Dev / 文档 / 自动化



- [x] 4.1 增加 Dev 演示页（覆盖 **字典/显式**、**`beforeChange` 静默**、**确认取消 `cancel`**、**`asyncChange` 失败**、**无 `asyncChange` + `afterChange`**）

- [x] 4.2 若 **`docs`** 需 **C7Switch** 说明：补 **VitePress** 页或链接


