## 1. 组件骨架与导出

- [x] 1.1 新建 **`quick-ui/src/packages/C7DictTag/index.vue`**，**`defineOptions({ name: 'C7DictTag', inheritAttrs: false })`**，补齐 **JSDoc**（**只读**、**`modelValue` 解析**、**`mk`/`max`/`+N`**、**`dictType` 映射与 fallback**）。
- [x] 1.2 在 **`quick-ui/src/packages/index.js`** 中 **import / export / installPackages`** 注册 **`C7DictTag`**。

## 2. 解析、匹配与渲染

- [x] 2.1 实现 **`modelValue` → 原子数组**（**`number`/`string`/`array`**、**`separator`** 拆分、**空态**）。
- [x] 2.2 实现 **`String(opt.value) === String(val)`** 匹配与 **`mk`** 序号（**仅已匹配**）。
- [x] 2.3 按 **spec** 输出 **已匹配 / 未匹配 / `+N` / 空 `-`** 的 DOM 与 **`inline-flex` + `gap`** 布局。

## 3. 交互与字典样式

- [x] 3.1 实现 **`dictType` → `ElTag.type`** 内置映射表 + **`primary` fallback**；**未匹配** 固定 **`info`**。
- [x] 3.2 **`max>0`**：**`+N`** 文案 **`N = 已匹配总数 − max`**；**`collapse=true`**：**`ElTooltip`** 溢出 **`label` 列表**。
- [x] 3.3 **`collapse=false`**：**`+N` 可点** + **Popover/Dropdown** 展示溢出 **`label`**（与 **design** 优先级一致）。

## 4. Props 透传与联调

- [x] 4.1 **`effect` / `round` / `size`** 透传到相关 **`ElTag`**。
- [x] 4.2 与 **`C7Select`/`C7Checkbox`** 的 **`options` 形状**、**`separator`** 在 Dev 页做一次 **人工联调**。

## 5. 演示与验证

- [x] 5.1 新增 **`quick-ui/src/views/dev/C7DictTagE2E.vue`**（或等价路由），覆盖 **双值、`max`、`+N` 点击、`collapse` tooltip、未匹配两种、`dictType` fallback**。
- [x] 5.2 执行 **`pnpm build:prod`**（在 **`quick-ui`** 目录）确认 **无类型与构建错误**。

## 6. 文档（可选）

- [x] 6.1 在 **`docs`** 的 C7 组件文档区增加 **`C7DictTag`** 说明页与侧栏入口（若项目已有 C7 文档范式则对齐）。
