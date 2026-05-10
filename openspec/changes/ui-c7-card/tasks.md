## 1. 组件与注册

- [x] 1.1 新增 **`quick-ui/src/packages/C7Card/index.vue`**：基于 **`ElCard`**；实现 **默认头**（色块、**`label`、`textSize`(h1~h5)、`isBold`**）、**`#header` 覆盖**、**`#extra` / `#toggle`**、**`collapsible` + `defaultExpanded` + `v-model` + `change`**
- [x] 1.2 **内容区** 使用 **`transition` fade**；折叠 **仅影响 body**，**header** 保持稳定
- [x] 1.3 **`showColorBlock` / `isShowColorBlock`** 别名解析；**`expandText` / `collapseText`** 应用于默认折叠控件（策略见 JSDoc）
- [x] 1.4 **`defineExpose({ toggle, expand, collapse })`**；**`inheritAttrs: false`** 下将 **`shadow` 等** 透传 **`ElCard`**
- [x] 1.5 **`packages/index.js`** 导出并 **`installPackages`** 注册 **`C7Card`**

## 2. 与规格对齐校验

- [x] 2.1 对照 **`openspec/changes/ui-c7-card/specs/ui-c7-card/spec.md`** 自测：**默认展开 → 折叠 → fade**、**`v-model`**、**ref 方法**
- [x] 2.2 验证 **`#header`** 时 **无默认折叠按钮**；**`collapsible` + `#toggle`** 替换默认控件

## 3. 工程与健康

- [x] 3.1 **`pnpm -C quick-ui build:prod`**（或仓库既定生产构建命令）通过

## 4. 可选：Dev

- [x] 4.1 增加 Dev 演示页：默认头 / **`#header`** / **`#extra`** / **受控与非受控** / **`shadow`**，路由在 PR 说明
