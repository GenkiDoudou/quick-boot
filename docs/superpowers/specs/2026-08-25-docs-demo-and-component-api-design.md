# 文档站：在线演示入口与三端组件 API 手册

日期：2026-08-25  
状态：已批准（对话确认）  
范围：`docs/`（VitePress 内容与导航）；不改三端业务运行时代码

关联：在 `2026-08-25-docs-sites-and-deploy-design.md`（实用向三端文档）之上扩展。

## 1. 已确认决策

| 项 | 选择 |
|----|------|
| 组件文档深度 | C：API 手册级（Props/Events/Slots 或 Java 公开 API 详表） |
| 演示入口 | C：独立「在线演示」页 + 顶栏入口 |
| 演示 URL | 文档 `https://qc.126w.com/docs`；后台 `https://qc.126w.com`；H5 `https://qc.126w.com/h5` |
| 实现路径 | 方案 1：演示页 + `backend/frontend/h5` 下 components 分区全量落盘 |

## 2. 目标与非目标

**目标**

- 提供可导航的在线演示页（三端链接）。
- 为 `quickboot-common` 各包、`quick-ui/src/packages` 各 C7、`quick-h5/src/components/qb` 各组件补充手册级文档页与索引。

**非目标**

- 从源码自动生成文档的流水线。
- 文档站内嵌可运行 playground。
- 修改 quickboot / quick-ui / quick-h5 业务行为。
- 将 `superpowers/` 编入站点。

## 3. 信息架构

### 3.1 在线演示

- 路径：`docs/docs/guide/demo.md`
- 顶栏增加「在线演示」
- `introduction` / `quick-start` 互链至该页
- 页内列出上述三个 `qc.126w.com` URL 及简短说明

### 3.2 文档树

```text
docs/docs/
  backend/components/
    index.md
    <package>.md          # 与 common 下包目录对应，约 18
  frontend/components/
    index.md
    c7-*.md               # 与 packages 下 C7 对应，约 25；含 C7MessageBox
  h5/components/
    index.md
    qb-*.md               # Qb 组件 + qb-card-column 工具
```

### 3.3 导航

- nav：指南 | 后端 | 管理端 | 移动端 | 在线演示
- sidebar：三端保留原实用四页；各组增加「组件/能力」折叠（索引 + 子页）
- 更新 `capabilities-outline.md` 组件手册状态

## 4. 单页结构与内容来源

### 4.1 页面结构

| 类型 | 章节 |
|------|------|
| C7 / Qb | 用途、导入、Props、Events、Slots（若有）、示例、源码路径 |
| common | 用途、主要类型/注解/工具、公开 API、配置键（若有）、示例、源码包路径 |

### 4.2 来源优先级

1. 源码注释与 `defineProps` / `defineEmits` / public API  
2. `packages/index.js` 与导出名  
3. `*Properties` 与现网配置键  
4. `views/dev/*E2E`、H5 业务用法（示例）  
5. 缺失处标注透传或「见源码」，禁止臆造

### 4.3 命名

- C7：`c7-button.md` 等 kebab  
- Qb：`qb-dict-tag.md`；工具 `qb-card-column.md`  
- common：包名小写文件（如 `excel.md`）

## 5. 验收标准

1. `cd docs && pnpm build` 成功；演示页与全部组件子页可导航打开。  
2. 演示三链接为已确认的 `qc.126w.com` 地址。  
3. common / C7 / Qb 均有独立页且含 API 级表格。  
4. 索引可跳到全部子页；大纲已更新。  
5. 无业务运行时代码改动。

## 6. 风险

- Props 表很长 → 以源码为准不擅自删项。  
- common 包交叉 → 按 Java 包目录拆页并互链。  
- 工作量大 → 实现时按包/组件批量从源码提炼，保证无空壳页。
