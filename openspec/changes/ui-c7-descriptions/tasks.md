## 1. 组件与注册



- [x] 1.1 新增 **`quick-ui/src/packages/C7Descriptions/index.vue`**：根 **`ElDescriptions`**，**`defineOptions({ name: 'C7Descriptions', inheritAttrs: false })`**，根 **`v-bind="$attrs"`**；显式 **`data`、`items`、`defaultEmptyText`**（默认 **`暂无`**）

- [x] 1.2 实现 **`items`** 遍历：**`el-descriptions-item`** **`v-bind`** 列配置；**`lodash/get`** 解析 **`prop`** 点路径；**`title`/`extra`** 插槽转发

- [x] 1.3 渲染优先级：**`slotName` + 父插槽存在** → 具名插槽（作用域 **`{ row, value, item }`**，**`row === data`**）；否则 **`columnType`** 分支：**`tag`→`C7DictTag`**、**`image`→`ElImage`**、**`link`→`<a>`**、**`copy`/`copyable`→`C7Copy`**、默认文本；**未知类型** 降级文本 + **`import.meta.env.DEV`** 下 **`console.warn`**

- [x] 1.4 **非 `tag` 展示空**（**`null`/`undefined`/`''`**）→ **`item.emptyText ?? defaultEmptyText`**；**`tag`** **不适用**空文案 props；**`link` 展示空** 不渲染 **`<a>`** 仅展示空文案（见 **design.md**）

- [x] 1.5 **`copy`**：**`copyProps`** **`v-bind` `C7Copy`**，复制串优先级见 **`spec.md`**（**`copyProps.text`** 优先）；**`image`**：**`imageAttrs`** 合并且 **`src`** 以解析值为准

- [x] 1.6 **`packages/index.js`**：**import/export** 并 **`installPackages`** 注册 **`C7Descriptions`**

- [x] 1.7 组件与关键纯函数补充 **JSDoc**（简体中文），写清 **`data` 为 `null`** 时 **`row`** 与取值行为、**`formatter`** 调用时机



## 2. 与规格对齐校验



- [x] 2.1 对照 **`openspec/changes/ui-c7-descriptions/specs/ui-c7-descriptions/spec.md`** 自测：点路径、各 **`columnType`**、**`tag` 与 `C7DictTag` 空态**、**非 `tag` 暂无**、**插槽作用域**、**`link` 空值无链接**

- [x] 2.2 确认 **未修改** **`C7DictTag`** / **`C7Copy`** 源码



## 3. 工程与健康



- [x] 3.1 **`pnpm -C quick-ui build:prod`**（或仓库既定生产构建命令）通过



## 4. 可选：文档与 Dev



- [ ] 4.1 （可选）**`docs/docs/frontend/components/通用组件/c7-descriptions.md`** 与 Dev 演示路由：覆盖主要 **`columnType`** 与 **`slotName`** 示例

