# init-agents 脚手架设计

**日期：** 2026-08-02  
**状态：** 已评审设计（待实现计划）  
**范围：** 可复用的 PowerShell 初始化脚手架 + 辅助 Cursor Skill；探测项目事实并生成 AI 规范文件（完整版）。

## 1. 背景与目标

参考公众号文章「一行命令，让 AI 从此懂你项目的规范」的思路：通过扫描项目配置自动生成 `AGENTS.md` / `DESIGN.md` 等，降低 AI 编码「不对味」的成本。

与原文差异（本设计强制要求）：

- 面向**可复用脚手架**（不限本仓库），支持 `-ProjectRoot`。
- **禁止**在已存在正式 `AGENTS.md` / `DESIGN.md` 时直接覆盖；改为写入 `*.suggested.md`。
- 默认 **dry-run**，显式 `-Write` 才落盘。
- **脚本为主、Skill 为辅**：探测与渲染由脚本完成；Skill 负责用法、diff 合并指引与隐性规范蒸馏入口。
- 完整版产物包含 `.agents/generation-spec.md` 与 `.agents/logs/corrections.md`。

成功标准：

1. 在无 `AGENTS.md` 的空/新项目上 `-Write` 后得到完整版文件集，且内容反映探测到的技术栈。
2. 在已有 `AGENTS.md` 的仓库（如本仓库）上 `-Write` **不修改**现有 `AGENTS.md`，仅生成 `AGENTS.suggested.md`（及同类旁路文件）。
3. 不带 `-Write` 时零文件变更，终端打印将执行的动作清单。
4. 无 `package.json` 且无 `pom.xml` / Gradle 构建文件时以非 0 退出。

## 2. 决策摘要

| 项 | 决策 |
|----|------|
| 复用范围 | 通用脚手架，可拷贝或 `-ProjectRoot` 指向任意项目 |
| AGENTS 写入 | 存在 `AGENTS.md` → `AGENTS.suggested.md`；否则 → `AGENTS.md` |
| DESIGN 写入 | 存在 `DESIGN.md` → `DESIGN.suggested.md`；否则 → `DESIGN.md` |
| 个人偏好 | `AGENTS.local.md` 已存在则跳过；新建时提示加入 `.gitignore` |
| corrections | 已存在则跳过（保留历史）；不存在则写空模板 |
| generation-spec | 已存在正式文件 → `generation-spec.suggested.md`；否则正式文件 |
| 运行时 | 仅 PowerShell（`.ps1`） |
| 落盘策略 | 默认 dry-run；`-Write` 落盘；`-ForceSuggested` 强制只写 `*.suggested*` |
| Skill | 辅助文档型 Skill，不替代脚本探测 |
| 不做 | 自动 git commit、调用 LLM、bash 双份、自动把 corrections 升级进 Never |

## 3. 目录布局

```text
tools/init-agents/
  init-agents.ps1              # CLI 入口
  lib/
    Detect-Stack.ps1           # 探测，返回结构化结果（hashtable/PSCustomObject）
    New-AgentsDocs.ps1         # 按模板渲染 + 解析目标路径
    Write-AgentsDocs.ps1       # dry-run / 实际写入 / 旁路命名
  templates/
    AGENTS.md.tmpl
    DESIGN.md.tmpl
    AGENTS.local.md.tmpl
    generation-spec.md.tmpl
    corrections.md.tmpl
  README.md

.cursor/skills/init-agents-scaffold/SKILL.md
```

本仓库实现落在上述路径；其他项目可整体复制 `tools/init-agents/`。

## 4. CLI 契约

```powershell
.\tools\init-agents\init-agents.ps1
.\tools\init-agents\init-agents.ps1 -Write
.\tools\init-agents\init-agents.ps1 -ProjectRoot D:\other-app -Write
.\tools\init-agents\init-agents.ps1 -ForceSuggested -Write
```

参数：

| 参数 | 默认 | 说明 |
|------|------|------|
| `-ProjectRoot` | 当前工作目录 | 目标项目根 |
| `-Write` | `$false` | 为 true 时落盘；否则仅打印计划 |
| `-ForceSuggested` | `$false` | 即使目标正式文件不存在，也只写旁路建议文件 |

退出码：

- `0`：探测成功且（dry-run 或写入）完成
- `非 0`：`-ProjectRoot` 无效，或未识别到任何前端/后端项目特征

终端输出必须包含：探测摘要（框架/UI/包管理器/模块等）、每个产物的动作（`create` / `create-suggested` / `skip`）、dry-run 时明确提示「未写入，添加 -Write 以落盘」。

## 5. 产物与路径解析规则

所有路径相对 `-ProjectRoot`。

| 逻辑产物 | 正式路径 | 旁路路径 | 已存在正式文件时 | 不存在正式文件时 | `-ForceSuggested` |
|----------|----------|----------|------------------|------------------|-------------------|
| AGENTS | `AGENTS.md` | `AGENTS.suggested.md` | 写旁路 | 写正式 | 写旁路 |
| DESIGN | `DESIGN.md` | `DESIGN.suggested.md` | 写旁路 | 写正式 | 写旁路 |
| local | `AGENTS.local.md` | — | **skip** | 写正式 | 写正式（若仍不存在） |
| generation-spec | `.agents/generation-spec.md` | `.agents/generation-spec.suggested.md` | 写旁路 | 写正式 | 写旁路 |
| corrections | `.agents/logs/corrections.md` | — | **skip** | 写正式空模板 | 若仍不存在则写正式 |

写入旁路或正式文件前，若父目录不存在（如 `.agents/logs`），在 `-Write` 时创建。

新建 `AGENTS.local.md` 时：若根目录存在 `.gitignore` 且尚未忽略该文件，则追加忽略规则；若不存在 `.gitignore`，仅打印提示，不强制创建 `.gitignore`（避免误伤无 git 项目）。dry-run 时只打印「将追加 gitignore」而不改文件。

编码：所有写出的文本文件使用 **UTF-8 无 BOM**。

## 6. 探测（事实层）

`Detect-Stack` 产出单一对象，字段至少包括：

- `ProjectName`
- `IsMonorepo`（bool 语义可用脚本内部 bool；写入文档时用文字）
- `Frontends[]`：路径、Framework、FrameworkVersion、UiLib、CssScheme、PkgMgr、Lang、Scripts（dev/build/lint）、PathAliases
- `Backends[]`：路径、BuildTool（Maven/Gradle）、JavaVersion、SpringBootVersion、Modules[]、SuggestedCommands
- `Docs[]`：路径、Tool（如 VitePress）
- `SpecHints[]`：存在则记录相对路径，如 `sdd/`、`openspec/project.md`、`.cursor/rules/`
- `PrimaryUiLib` / `PrimaryColor` / `BorderRadius`：供 DESIGN 模板（多前端时取第一个检测到 UI 库的前端，或显式优先级：根 package → 名含 ui 的包 → 其余）

探测规则（有则填，无则 `Unknown` 或省略章节）：

1. **前端**：扫描 `-ProjectRoot/package.json`，以及常见子目录一层 `*/package.json`（含 `packages/*`、`apps/*`）。用依赖名识别 React/Vue/Next/Nuxt/Angular/Svelte、Umi、Element Plus/Ant Design 等、Less/Sass/Tailwind/CSS-in-JS；用 lockfile 识别 npm/yarn/pnpm/bun；读 scripts 与 tsconfig/jsconfig paths。
2. **后端**：存在 `pom.xml` 则 Maven（解析 parent/modules、`java.version` / `maven.compiler.release`、`spring-boot` 相关坐标若可从属性或依赖文本得到）；存在 `build.gradle` / `build.gradle.kts` 则标记 Gradle（版本字段尽力而为，失败则 Unknown）。
3. **文档**：`docs/package.json` 含 `vitepress` 则记录 VitePress。
4. **Monorepo**：pnpm-workspace、package.json workspaces、lerna、或后端多 module。
5. **规范线索**：路径存在即加入 `SpecHints`（不解析文件内容）。

识别失败条件：上述扫描后 `Frontends`、`Backends`、`Docs` 均为空 → 非 0 退出。

## 7. 模板与渲染（策略层）

模板使用简单占位替换（如 `{{ProjectName}}`、`{{FrameworkSummary}}`、`{{NeverRulesBlock}}`），不引入外部模板引擎依赖。

段落开关（布尔由探测结果决定，在渲染前拼好字符串块）：

- 检测到前端 → 启用前端组件/API/CSS Never 等段落
- 检测到后端（Java/Spring）→ 启用分层、Controller 约定（优先 `@PostMapping`、Validation、OpenAPI 注解）、禁止实体布尔等**可复用默认段落**（与本仓库 AGENTS 对齐的通用子集，不写死 quickboot 包名）
- 检测到 VitePress → 文档命令小节
- `SpecHints` 非空 → 「必读规范路径」列表

完整版 AGENTS 模板章节（顺序固定）：

1. 技术栈声明  
2. Project Structure  
3. Build & Dev Commands  
4. Coding Style & Naming  
5. Commit & Git Convention  
6. Component & API Conventions（前端）/ Backend Layering（后端，可并列）  
7. Never Rules  
8. Multi-Agent Safety  
9. Quick Commands（完整版指令表，至少：分析项目规范、CR 代码、生成变量名、合并 suggested、记录 correction）  
10. Error Handling  
11. Spec Path Index（`SpecHints`）

DESIGN 模板：Design Tokens 表（主色随 UI 库）、间距 8px 网格、断点、动效、图标约定。

`generation-spec.md`：按已探测栈给出代码生成骨架说明（Vue SFC / React / Java Controller-Service-Mapper 三选多，有则输出对应小节）。

`corrections.md`：Markdown 表格表头 + 一行示例说明如何填写；不预填真实错误。

**明确：** Never / Multi-Agent / 修复协作类条文来自模板常量，不从依赖「猜测」。本仓库特有的超长协作流程（OpenSpec opsx、两阶段修 bug 全文）**不**强制打进通用模板；通用模板保留简短「问题修复先方案后改码」与 Karpathy 式「先假设/成功标准/最小计划」摘要，并在 Skill 中说明：已有厚 `AGENTS.md` 的项目应人工把 suggested 中的事实层合并进现有文件。

## 8. Skill 职责（辅）

路径：`.cursor/skills/init-agents-scaffold/SKILL.md`

触发场景：用户提到 init-agents、生成 AGENTS 脚手架、同步项目 AI 规范、合并 AGENTS.suggested 等。

Skill 必须指导 Agent：

1. 先运行脚本 dry-run，再经用户确认后 `-Write`。  
2. 若产生 `*.suggested.md`，用 diff 对照正式文件，**只合并事实层**，不覆盖手写策略。  
3. 完整版「分析项目规范」：扫描源码隐性习惯，可更新 `.agents/` 或建议补丁，不静默覆盖 `AGENTS.md`。  
4. corrections：纠正 AI 错误后追加一行；重要条目再提议升级为 Never（需用户确认）。

Skill **禁止**自己用猜测重写整份 `AGENTS.md` 而不跑脚本或不经用户确认。

## 9. 错误处理与安全

- 路径遍历：`-ProjectRoot` 必须解析为已存在目录。  
- 写入仅限约定相对路径列表，禁止写 `node_modules`、`.git` 内容。  
- 覆盖策略：正式 `AGENTS.md` / `DESIGN.md` / 已存在 `corrections.md` **永不覆盖**。旁路 `*.suggested.md` 在 `-Write` 时可覆盖旧建议（每次刷新建议合法）。  
- 不读取或打印 `.env` 密钥内容。

## 10. 验证计划

1. **Dry-run（本仓库）**：在 quickboot 根执行无 `-Write`，确认退出 0、计划中 AGENTS/DESIGN 为 `create-suggested`、无文件 diff。  
2. **Write（本仓库）**：`-Write` 后出现 `AGENTS.suggested.md` 等，且原 `AGENTS.md` 字节不变。  
3. **临时空项目**：仅含最小 `package.json`（vue + element-plus）的临时目录，`-Write` 后得到正式 `AGENTS.md` / `DESIGN.md` / `.agents/**` / `AGENTS.local.md`。  
4. **ForceSuggested**：空项目 `-ForceSuggested -Write` 只出现 suggested，无正式 AGENTS。  
5. **失败路径**：空目录执行 → 非 0。

## 11. 实现顺序（供后续 writing-plans 拆任务）

1. `Detect-Stack.ps1` + 对本仓库探测的手工核对  
2. 模板文件（完整版五件套）  
3. 路径解析 + `Write-AgentsDocs`（dry-run / write / force-suggested）  
4. `init-agents.ps1` 入口与 `README.md`  
5. `init-agents-scaffold` Skill  
6. 按第 10 节验证

## 12. 非目标（再次确认）

- 不实现 bash 版  
- 不自动 commit / push  
- 不调用任何 LLM API  
- 不自动把 corrections 合并进 Never  
- 不替换本仓库现有厚 `AGENTS.md` 的权威地位
