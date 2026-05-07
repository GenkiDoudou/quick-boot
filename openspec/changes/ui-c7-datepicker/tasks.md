## 1. 组件与注册

- [ ] 1.1 新增 `quick-ui/src/packages/C7DatePicker/index.vue`：根 **`ElDatePicker`**；**`rangeMerge`（默认 `false`）**、**`mergeDelimiter`（默认 `','`）**；**type → format/valueFormat** 映射（显式传入覆盖）；范围 **合并输出 / 拆分输入**；非法串 **清空 + `console.warn`**；**`update:modelValue`**、**`change`/`blur`/`focus`** 对齐 **`openspec/changes/ui-c7-datepicker/specs/ui-c7-datepicker/spec.md`** 与 **`docs/superpowers/specs/2026-05-07-c7-datepicker-design.md`**

- [ ] 1.2 **`packages/index.js`**：导出 **`C7DatePicker`** 并在 **`installPackages`** 中全局注册 **`C7DatePicker`**

## 2. 与规格对齐校验

- [ ] 2.1 **daterange**：外部 **`"a,b"`** 回显可编辑；**`rangeMerge=false`** 输出数组；**`rangeMerge=true`** 输出 **`mergeDelimiter`** 拼接串

- [ ] 2.2 **未映射 `type`**：不注入默认 **`format`/`valueFormat`**

- [ ] 2.3 **非法合并串**：空值 + **`console.warn`**

## 3. 工程与健康

- [ ] 3.1 `quick-ui` 生产构建通过（**`pnpm build:prod`** 或仓库等价脚本）

## 4. 可选：Dev / 文档

- [ ] 4.1 Dev 演示页：覆盖 **合并/非合并**、**自定义 `mergeDelimiter`**、**显式 format 覆盖**

- [ ] 4.2 若需文档站：补 **`docs/docs/frontend/components/通用组件/c7-datepicker.md`** 与侧栏
