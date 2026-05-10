## 1. 组件骨架与注册



- [x] 1.1 新增 `quick-ui/src/packages/C7JsonTableColumn/index.vue`：多根 **`el-table-column`**；**`columns`（必填）**、**`emptyText`** 的 props 与 JSDoc；**`columns` 非数组**时 dev warn 且不渲染列

- [x] 1.2 实现列预处理：**`visible !== false`** 过滤；**`order` 升序 + 无 order 在后 + 同 order 稳定排序**（首版可内联于 SFC，若抽离则同步 **`normalizeColumns.ts`** 与可选单测）

- [x] 1.3 **`quick-ui/src/packages/index.js`**：**export `C7JsonTableColumn`** 并在 **`installPackages`** 中注册



## 2. ElTableColumn 透传与 columnType



- [x] 2.1 绑定显式列字段至 **`el-table-column`**：**`prop`、`label`、`width`、`minWidth`、`fixed`、`align`、`headerAlign`、`sortable`、`showOverflowTooltip`**；**`props`（列对象键）** 以 **`v-bind` 合并**（内部变量名 **`columnProps`** 或与文档一致），JSDoc 写明与显式字段的优先级

- [x] 2.2 **`text`**：无 **`formatter`** 时空值 **`null`/`undefined`/`''`** 的占位链（列 **`emptyText`** → 表 **`emptyText`** → **`'-'`**）；有 **`formatter`** 时原样传给 EP，不做空值改写

- [x] 2.3 **`tag`**：**`C7DictTag`**，**`options ?? dictList`**；缺 **`prop`** 时 dev warn 并跳过列

- [x] 2.4 **`image`**：**`C7Preview`**，**`urls=row[prop]`**，**`coverType='none'`**，**`autoDetect`** 默认与 C7Preview 一致；缺 **`prop`** 时 dev warn 并跳过列

- [x] 2.5 **`link`**：**`<a>`**，**`linkHref`/`linkText`** 支持 string 或函数；**`linkTarget`** 仅 string；**href** 为空不渲染链接且显示 **`-`**；缺 **`prop`** 时 dev warn 并跳过列

- [x] 2.6 **`slot`**：具名 **`slotName || prop`**；**`#default`** 转发 **`{ row, column, $index }`**；无插槽时单元格 **`-`**；**`slotName` 与 `prop` 皆无效**时 dev warn 且跳过内容区

- [x] 2.7 **未知 `columnType`**：按 **`text`** 处理并 dev warn



## 3. 列头插槽与边界



- [x] 3.1 有 **`prop`** 的列：在 **`el-table-column` #header** 中检测父级 **`#header-${prop}`** 并转发；**`header` 作用域**以当前 EP 类型为准补 JSDoc

- [x] 3.2 对照 **`openspec/changes/ui-c7-json-table-column/specs/ui-c7-json-table-column/spec.md`** 中错误处理表（**`tag`/`image`/`text` 无 prop**、**`link` 无 prop** 等）逐项实现



## 4. E2E 与文档



- [x] 4.1 新增 E2E / dev 演示页与路由（命名如 **`C7JsonTableColumnE2E`** 或项目惯例等价），覆盖：**各 `columnType`**、**`options` vs `dictList`**、**`formatter` / 无 formatter**、**`#header-${prop}`**、**`order` 排序**、**slot 未提供时 `-`**

- [x] 4.2 新增 VitePress：`docs/docs/frontend/components/通用组件/c7-json-table-column.md`（Props / 列配置表 / 示例 / 与 **C7JsonTable** 关系），并更新 **`docs/.vitepress/config/sidebar.ts`**（侧栏条目已存在，本次补全 md 页面）



## 5. 验证



- [x] 5.1 `cd quick-ui` 执行 **`pnpm build:prod`**（或仓库等价生产构建）通过

- [x] 5.2 （跳过：未抽离 **`normalizeColumns.ts`**，不适用单测；首版逻辑内联于 SFC）

