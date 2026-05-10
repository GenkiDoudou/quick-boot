## 1. 组件与注册



- [x] 1.1 新增 `quick-ui/src/packages/C7Cascader/index.vue`：基于 **`ElCascader`**，**`inheritAttrs: false`** + **保留字过滤** + 其余 **attrs 透传**；实现 **`mapTree`**（**`labelKey` / `valueKey` / `childrenKey`**）、**静态 `dataList` / `options`（`dataList` 优先）**

- [x] 1.2 **整树异步**：**`autoLoad=true`**、无静态、**非懒加载**时 **`onMounted`** 调用 **`fetchData({ ...fetchParams })`**；**`resultKey` + `dataFormatter`** 自 **`res.data`** 解析（对齐 **`C7TreeSelect`**）；**`fetchGeneration`** + **`inFlightCount`** + **`load-error` / `loading-change`**

- [x] 1.3 **懒加载**：**`lazy=true`** 且 **`fetchData`** 存在时实现 **`lazyLoad`**：根 **`parentId: rootParentId`**，子层 **`parentId`** 为父节点映射后 **`value`**；子接口返回 **扁平数组** → 映射后 **`resolve`**

- [x] 1.4 **`multiple` + `separator` + `valueType`**：对齐 **`C7TreeSelect`** 的 **内外转换**；**`emitPath=true`** 或 **非一维标量多选** 时 **`separator` 无效** + **DEV `console.warn`**

- [x] 1.5 **事件**：**`update:modelValue`**、**`change`**、**`visible-change`**、**`loading-change`**、**`load-error`**；**`defineExpose`**：**`loading`**（或等价）、**`reload()`**（与树选语义一致：静态仅刷新内部形态 / 异步重新 **`executeFetch`**）

- [x] 1.6 **`quick-ui/src/packages/index.js`**：**export** + **`installPackages`** 注册 **`C7Cascader`**



## 2. 与规格对齐校验



- [x] 2.1 对照 `openspec/changes/ui-c7-cascader/specs/ui-c7-cascader/spec.md` 逐项核对：**保留字**、**懒加载入参**、**separator 边界**、**`autoLoad` 无 `fetchData` 的 warn**

- [x] 2.2 本地验收：整树挂载加载成功 / 失败 **`load-error`**

- [x] 2.3 本地验收：懒加载展开 → **`fetchData({ parentId, ... })`**

- [x] 2.4 本地验收：**`multiple` + `separator` + `emitPath=false`** 逗号串；**`emitPath=true` + `separator=true`** 为数组且有 **warn**



## 3. 工程与健康



- [x] 3.1 `cd quick-ui` 执行 **`pnpm build:prod`**（或仓库等价生产构建命令）通过



## 4. 文档与可选 Dev



- [x] 4.1 新增 `docs/docs/frontend/components/通用组件/c7-cascader.md`（Props/事件/数据模式/与 **`C7TreeSelect`** 选型建议），并更新 **`docs/.vitepress/config/sidebar.ts`** 侧栏入口

- [x] 4.2 （可选）扩展 **`views/dev`** 下演示页：静态 / 整树 / 懒加载 / **separator** 各一段最小示例


