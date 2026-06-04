---
name: create-html
description: >-
  根据自然语言描述生成 Vue 3 + Element Plus 单文件静态 HTML 原型页，内嵌 mock 数据，通过 CDN 引入依赖，不依赖 Node.js。
  Element Plus 组件必须使用成对闭合标签（如 <el-option></el-option>），禁止自闭合写法。
  Use when the user asks to create-html、生成静态 HTML 原型、CDN 版 Element Plus 页面、mock 演示页，或需要不经过构建工具的前端页面草稿。
---

# create-html：CDN 静态 HTML 原型

## 目标

根据用户**页面/功能描述**，产出**可直接用浏览器打开**的单个 `.html` 文件：

- 技术栈：**Vue 3** + **Element Plus**
- 依赖：**仅 CDN**，禁止 `npm`/`pnpm`/`vite`/`webpack`、禁止 `package.json`、禁止多文件工程化产物（除非用户明确要求拆分）
- 数据：在页面内编写 **mock**（`ref`/`reactive` 或常量），模拟列表、表单、详情等，不请求真实后端
- 标签：**所有 `el-*` 组件必须写成成对闭合标签**，禁止自闭合

## 何时使用

- 用户提到 `create-html`、静态原型、演示页、PRD 可视化草稿
- 需要快速验证布局/交互，且**不能**依赖 Node 构建环境
- 用户要求「单 html」「双击就能看」

## 工作流程

1. **解析描述**：页面类型（列表/表单/详情/仪表盘）、字段、操作按钮、筛选条件、分页、弹窗等。
2. **确定输出路径**：默认与用户指定路径一致；未指定时放在仓库合理目录（如 `原型/`、`docs/demo/` 或用户当前目录），文件名语义化，如 `user-list-prototype.html`。
3. **编写单文件 HTML**：按下方「页面骨架」与「强制规范」生成完整文件。
4. **自检清单**（生成后逐项核对）：
   - [ ] 仅用 CDN，无 Node 构建步骤说明
   - [ ] `createApp` 挂载正常，`#app` 内有完整 UI
   - [ ] mock 数据覆盖描述中的主要场景（空列表、有数据、loading 等按需）
   - [ ] **无任何** `<el-xxx />` 或 `<el-xxx .../>` 自闭合写法
   - [ ] 在浏览器中逻辑自洽（按钮可触发 `ElMessage` 等反馈）

## CDN 依赖（默认）

优先使用 **jsDelivr**（稳定、支持国内访问），版本可固定 minor 以减少漂移：

```html
<link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/element-plus@2/dist/index.css" />
<script src="https://cdn.jsdelivr.net/npm/vue@3/dist/vue.global.prod.js"></script>
<script src="https://cdn.jsdelivr.net/npm/element-plus@2/dist/index.full.min.js"></script>
```

若页面用到图标，追加：

```html
<script src="https://cdn.jsdelivr.net/npm/@element-plus/icons-vue@2/dist/index.iife.min.js"></script>
```

注册方式（`setup` 内或 `createApp` 后）：

```javascript
const { createApp, ref, reactive, computed, onMounted } = Vue;
const app = createApp({ /* ... */ });
app.use(ElementPlus);
// 若使用图标：遍历 ElementPlusIconsVue 注册
if (typeof ElementPlusIconsVue !== 'undefined') {
  for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
    app.component(key, component);
  }
}
app.mount('#app');
```

用户指定其他 CDN（unpkg、bootcdn 等）时可替换，但仍须 **Vue 3 + Element Plus 2.x** 且保持全局构建（`vue.global` + `index.full`）。

## 页面骨架（模板）

```html
<!DOCTYPE html>
<html lang="zh-CN">
<head>
  <meta charset="UTF-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1.0" />
  <title><!-- 页面标题 --></title>
  <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/element-plus@2/dist/index.css" />
  <style>
    /* 仅布局与间距；颜色语义化，避免大面积硬编码品牌色 */
    body { margin: 0; font-family: system-ui, sans-serif; background: #f5f7fa; }
    #app { padding: 16px; }
  </style>
</head>
<body>
  <div id="app">
    <!-- 模板写在此处；el 组件全部成对闭合 -->
  </div>
  <script src="https://cdn.jsdelivr.net/npm/vue@3/dist/vue.global.prod.js"></script>
  <script src="https://cdn.jsdelivr.net/npm/element-plus@2/dist/index.full.min.js"></script>
  <script>
    const { createApp, ref, reactive } = Vue;
    createApp({
      setup() {
        // mock 数据与事件处理
        return { /* 暴露给模板 */ };
      },
    }).use(ElementPlus).mount('#app');
  </script>
</body>
</html>
```

复杂页面可在 `<script>` 中使用 `setup()` + `ref`/`reactive`；避免引入需编译的 JSX/TS/SFC。

## 强制规范

### 1. Element Plus 标签：成对闭合（全限定补全）

**必须**使用开始+结束标签，**禁止**自闭合（包括无子内容的叶子节点）：

| 禁止 | 必须 |
|------|------|
| `<el-option />` | `<el-option></el-option>` |
| `<el-table-column />` | `<el-table-column></el-table-column>` |
| `<el-input />` | `<el-input></el-input>` |
| `<el-button />` | `<el-button></el-button>` |
| `<el-col />` | `<el-col></el-col>` |

带属性的写法同样成对闭合，例如：

```html
<el-option label="启用" value="1"></el-option>
<el-table-column prop="userName" label="用户名" width="160"></el-table-column>
```

**原生 HTML 空元素**（`meta`、`link`、`img`、`br`、`input`）仍可按 HTML 规范自闭合；约束**仅针对 `el-` 前缀组件**。

### 2. 不依赖 Node.js

- 不创建 `package.json`、`vite.config`、`src/` 工程目录
- 交付物说明为：「用浏览器直接打开该 `.html` 文件」
- 禁止在说明中要求用户执行 `npm install` 或 `pnpm dev`

### 3. Mock 数据

- 字段名使用 **camelCase**，与常见前后端约定一致
- 列表页：提供 **10～20 条** 示例数据；含 `id`、状态枚举、时间字符串等
- 表单页：为 `reactive` 表单对象赋初值；校验用 `rules` + `el-form` 的 `ref`
- 操作反馈：删除/保存等用 `ElementPlus.ElMessage` / `ElMessageBox`，不发起真实 HTTP
- 若描述含分页：mock `total` 与 `list`，用 `el-pagination` 绑定本地切换

### 4. UI 与交互

- 布局：`el-container` / `el-row` + `el-col` / `el-card` 组织页面
- 列表：`el-table` + 成对闭合的 `el-table-column`
- 筛选：`el-form` + `el-form-item` + 输入类组件（均成对闭合）
- 中文界面文案；`locale` 默认即可（CDN 完整包已含中文）
- 保持单文件可读：过长 mock 数组可抽成 `const MOCK_TABLE_DATA = [ ... ]` 放在 `setup` 外同一 script 块

### 5. 编码

- 文件 **UTF-8 无 BOM**
- 缩进与引号风格在同一文件内保持一致（默认 2 空格）

## 描述缺失时的最小澄清

若描述过于简略，用**一行**确认其一（勿长篇追问）：

- 页面类型（列表 / 表单 / 详情 / 混合）
- 输出文件路径
- 是否需要弹窗、抽屉、步骤条等特殊组件

用户未回复时，按**列表 + 查询 + 新增弹窗**的常见管理页默认实现，并在 HTML 顶部注释标明假设。

## 示例触发语

- 「用 create-html 做一个用户管理列表，带搜索和分页」
- 「生成一个不依赖 node 的 Element Plus 表单原型」
- 「根据下面 PRD 写个静态 html 演示」

## 反模式（禁止）

- 生成 Vue SFC（`.vue`）或需要 `@vitejs/plugin-vue` 的产物
- 使用 `<el-* />` 自闭合
- 仅写壳子、无 mock 数据或按钮无反馈
- 引用需本地服务的 ES module 裸导入（`import 'element-plus'`）而无 CDN 全局变量
