# 项目协作指南（AGENTS.md）

本仓库是一个 **Spring Boot 3（后端）+ Vue 3 + Vite + Element Plus（前端）+ VitePress（文档）** 的全栈项目集合。请优先遵循仓库内已有规则文件：`.cursorrules` 与 `.cursor/rules/*`（若存在）。

## 文件编码规范（强制）

- 新建或修改任何文本文件时，**一律使用 UTF-8 无 BOM 编码**（强制）。
- 严禁使用 GBK/ANSI/UTF-16 等会导致跨环境乱码的编码。
- 严禁在源码文件头写入 BOM（`\ufeff`）；若编译报 `非法字符: '\ufeff'`，必须先移除 BOM 再提交。
- 若发现历史文件存在乱码或编码不一致，优先转换为 UTF-8 无 BOM 后再继续修改。
- 在 PowerShell 中写文件时，优先使用无 BOM 写法（如 `new UTF8Encoding($false)`）避免再次引入 `\ufeff`。

## `/brainstorming` 与需求澄清（本仓库约定）

当用户通过 **`/brainstorming`** 启动头脑风暴或设计类流程、且需要向用户澄清需求时：

- **在同一条回复中一次性列出**全部待澄清问题；**不要**采用「每次只问一个问题、等用户回复后再问下一题」的节奏。
- 每一道澄清题须使用**下列版式**（**按序号**逐题重复；选项字母与数量可按题意增减；**每题**须标出**一个**推荐项）：

```text
1. 问题: ……
选项:
A …… 
B …… 
C ……（推荐）
D: ……

2. 问题: ……
选项:  
A …… 
B ……（推荐）
```

说明：

- **`N. 问题:`**（`N` 为从 1 开始的序号）后接完整问句或澄清点描述。
- **`选项`** 单独一行；`选项` 与 **A** 之间至少一个空格；同一行内依次写 **A / B / C / D…** 及对应短语，选项之间用空格分隔；推荐项写在**该选项短语末尾**，用 **（推荐）** 或 **`(推荐)`** 标注均可。
- 若某题只需二选一，可仅写 **A … B …（推荐）** 等，不必凑满四个选项。
- 全部问题列完后，**必须**再给出**一行推荐汇总**（题号与上文序号对应；冒号后为该题推荐选项字母；多组之间用空格分隔），格式示例：

```text
推荐 1: A ,2: C , 3: B
```

必要时可在「推荐」行之后用一两句总述推荐理由。

若其他文档或技能（例如 brainstorming）写明「一次一问」，在本仓库执行 **`/brainstorming`** 时**以本节为准**（仅澄清问题的组织方式；设计定稿、未批准前不写实现代码等既有硬约束不变）。

## 目录结构（高层）

- `quickboot/`：后端（Maven 多模块）
  - `quickboot-common/`：通用能力（工具类、通用响应、异常、安全组件等）
  - `quickboot-core/`：核心配置/基础设施能力
  - `quickboot-web/`：Web 启动模块（Controller/Service/Mapper、Spring Boot 启动类、资源配置）
- `quick-ui/`：前端（Vue 3 + Vite，使用 `pnpm`）
- `docs/`：文档站点（VitePress）
- `原始需求/`：由分析/梳理产出的“原始需求”文档（不参与构建）

## 环境与依赖

- 后端：JDK **17+**、Maven **3.6+**、MySQL **8+**（或按实际配置）
- 前端/文档：Node.js（建议 **18/20**）、`pnpm`（`quick-ui/package.json` 标注 `pnpm@9.0.0`）

## 常用命令

### 后端（`quickboot/`）

```bash
cd quickboot
mvn clean install -DskipTests

# 启动 web 模块（端口默认 9991）
mvn -pl quickboot-web spring-boot:run
```

### 前端（`quick-ui/`）

```bash
cd quick-ui
pnpm i
pnpm dev
```

### 文档（`docs/`）

```bash
cd docs
pnpm i
pnpm dev
```

## 代码与改动约定（面向协作/维护）

- **避免误扫/误改大目录**：不要对 `**/node_modules/**`、`bak/` 做递归检索或批量改动。
- **后端分层**：尽量保持 `controller / service / mapper / entity / dto / vo` 分层清晰。
- **前端组织**：API 放在 `quick-ui/src/api/`，通用组件放在 `quick-ui/src/components/`。

### 生成代码时必须引用的规范文档（AI / OpenSpec / 脚手架）

在**新建或生成**对应产物前，须**用读取工具打开并通读**下列规范**源文件**（路径相对仓库根目录）；禁止把规范正文抄进规则文件代替阅读：

- **项目级约束（技术栈、架构、流程、协作规范）**：`openspec/project.md`
- **后端（Java / Maven 等）**：`sdd/后端代码规范.md`
- **前端（Vue / TypeScript 等）**：`sdd/前端代码规范.md`
- **数据库（DDL、Flyway 迁移、表结构设计说明等）**：`sdd/数据库设计规范.md`

执行要求补充：
- 生成代码时必须落实 `openspec/project.md` 中的项目背景、技术栈、分层方式、验证流程与协作约束。
- 若 `openspec/project.md` 与其他规范文档存在冲突，优先级顺序为：`openspec/project.md` > 口头需求；并与用户确认例外。

### 前端设计规范补充（DESIGN.md）

在新建、生成或改造任何前端页面/组件（Vue、TS、样式）前，必须先读取仓库根目录 `DESIGN.md`，并按其中设计系统要求实现视觉与交互。

执行要求：
- 禁止将 `DESIGN.md` 正文复制到本文件或其他规则文件中替代阅读。
- 生成代码时必须落实 `DESIGN.md` 中的颜色、字体、层级、间距、状态样式与动效约束。
- 当现有页面改造为新风格时，优先抽取页面级 design token 并保持同一页面内一致。
- 若口头需求与 `DESIGN.md` 冲突，默认以 `DESIGN.md` 为准，并向用户确认例外。

### 生成前后端代码时的注释要求（AI / 脚手架 / 批量产出）

- 后端 public 类型及 public/protected 成员需具备 JavaDoc。
- 前端默认导出组件/composable/模块工具函数需具备 JSDoc。
- 注释语言以简体中文为主，重点解释“为什么”和边界条件。

### 后端接口生成补充约束（Controller / Service）

- 默认不使用 `@PutMapping` / `@DeleteMapping`，统一优先 `@PostMapping` 表达修改/删除语义。
- 参数必须接入 Jakarta Validation。
- 对外 REST 接口至少包含 `@Tag`、`@Operation`、关键参数 `@Parameter`。
- 禁止抛出 `IllegalArgumentException` 作为业务失败信号，应使用项目自定义异常。

## 提交/验证建议（尽量局部、可复现）

- 后端优先模块级测试，再视情况全量。
- 前端至少执行一次 `pnpm build:prod`。
- 提交前核对依赖声明与代码引用一致。
