## 1. 组件与注册

- [x] 1.1 新增 `quick-ui/src/packages/C7Select/index.vue`：基于 **`ElSelect`** 实现 **静态 `dataList` / `options`（别名）**、**`fetchData` + `fetchParams` + `resultKey` + `dataFormatter`**（自 **`response.data`** 解析）、**`autoLoad`**（**非 remote** 挂载加载）、**`remote`**（首次聚焦 **无 `query`** 全量；输入 **`query`** 搜索；**`reloadOnClear`**）、**`multiple` + `separator`** 的 **v-model 适配**（逗号字符串 ↔ 数组；**对不齐 value 保留**）
- [x] 1.2 **插槽透传**：**`prefix` / `label` / `option` / `empty`** → **`ElSelect`** 具名插槽
- [x] 1.3 **事件**：**`update:modelValue`**、**`change(valueOrString)`**、**`visible-change`**、**`loading-change`**
- [x] 1.4 **`defineExpose`**：**`loading`**、**`reload()`**；**`packages/index.js`** 导出并 **`installPackages`** 注册 **`C7Select`**

## 2. 与规格对齐校验

- [x] 2.1 对照 `openspec/changes/ui-c7-select/specs/ui-c7-select/spec.md` 核对 **`fetchData` 参数是否含 `query`**、**全量首载不含 `query`**、**separator 与保留 value** 行为
- [x] 2.2 验收：**`remote=true`** 下输入关键字 → **请求带 `query`** 且选项更新
- [x] 2.3 验收：**`multiple=true`** 下 **`separator=true`** → 对外 **逗号字符串**；**`separator=false`** → **数组**；外部逗号字符串 **能解析回显**，**缺 option 的 value 不被静默删除**

## 3. 工程与健康

- [x] 3.1 `quick-ui` 生产构建通过（若本机默认 Node 过旧，按仓库既有说明使用 **Node 18+ / 22** 跑 **`vite build`**）

## 4. 可选：Dev / 文档

- [x] 4.1 增加或扩展 Dev 页场景（静态 / 异步 / remote / separator 多选），路由与文件位置由实现者选定并在 PR 说明
