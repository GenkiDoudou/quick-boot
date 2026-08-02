## Context

仓库已有较厚的根目录 `AGENTS.md`（协作流程、Karpathy、规范引用等），不宜被「整文件覆盖」式初始化脚本破坏。同时希望沉淀一套可拷贝到其他项目的脚手架，参考「一行命令生成 AI 规范」的思路，用 PowerShell 探测事实层并渲染完整版文档。

权威产品设计见：`docs/superpowers/specs/2026-08-02-init-agents-scaffold-design.md`。本 OpenSpec design 将其落实为可实施的技术决策，供 `tasks.md` 拆解。

约束：UTF-8 无 BOM；Windows 优先；不引入外部模板引擎或 LLM 调用；写入路径白名单。

## Goals / Non-Goals

**Goals:**

- 可复用 CLI：`-ProjectRoot` / 默认 dry-run / `-Write` / `-ForceSuggested`。
- 完整版五件套模板 + 探测驱动段落开关（前端 / Java 后端 / VitePress / SpecHints）。
- 安全写入：正式 AGENTS/DESIGN 永不覆盖；corrections 与 local 存在则 skip。
- 辅助 Skill：用法、合并 suggested、隐性规范与 corrections 指引。

**Non-Goals:**

- bash 双份、自动 git commit、调用 LLM、自动把 corrections 升级进 Never。
- 替换本仓库现有 `AGENTS.md` 的权威内容。
- 解析 `.env` 或扫描 `node_modules` 内容。

## Decisions

### D1：脚本为主、Skill 为辅

- **选择**：探测与渲染全部在 `tools/init-agents/*.ps1`；Skill 只指导流程。
- **替代**：纯 Skill（无确定性、无 dry-run）；纯脚本无 Skill（完整版进化入口弱）。
- **理由**：初始化要可重复、可 CI；进化与合并需要对话式指引。

### D2：旁路 suggested 而非分区原地合并

- **选择**：有正式文件则写 `*.suggested.md`；不做 `<!-- agents:generated -->` 原地刷新。
- **替代**：分区合并写入 AGENTS.md（实现复杂，误伤手写区风险高）。
- **理由**：与「厚 AGENTS 仓库」共存成本最低；合并由人/Agent 显式完成。

### D3：简单占位模板，无外部引擎

- **选择**：`{{Placeholder}}` 字符串替换；复杂块在渲染前拼好再注入。
- **替代**：Handlebars/Mustache npm 包（增加运行时依赖）。
- **理由**：零依赖，PowerShell 单机可跑。

### D4：模块拆分

| 模块 | 职责 |
|------|------|
| `Detect-Stack.ps1` | 扫描 package/pom/gradle/docs/spec 线索 → PSCustomObject |
| `New-AgentsDocs.ps1` | 读模板、按探测结果渲染五份内容 |
| `Write-AgentsDocs.ps1` | 解析正式/旁路路径、dry-run 计划、UTF-8 无 BOM 写入、gitignore 追加 |
| `init-agents.ps1` | 参数、编排、退出码、终端摘要 |

### D5：探测范围与失败条件

- 前端：根与一层子目录 / `packages|apps/*` 的 `package.json`。
- 后端：`pom.xml` 或 Gradle 构建文件。
- 文档：`docs/package.json` + vitepress。
- `Frontends`、`Backends`、`Docs` 皆空 → 非 0 退出。

### D6：UI 主色优先级

多前端时：根 `package.json` → 包名含 `ui` → 其余第一个含 UI 库者，写入 DESIGN tokens。

## Risks / Trade-offs

| 风险 | 缓解 |
|------|------|
| grep/正则解析 pom/package 不精确 | 版本 Unknown 可接受；README 说明可手改 suggested |
| 反复 `-Write` 覆盖旧 suggested | 文档声明建议文件可刷新；正式文件永不覆盖 |
| 通用模板 Never 与目标项目冲突 | 模板保持通用子集；Skill 强调合并时只取事实层 |
| 本仓库执行产生新 untracked suggested | 预期行为；可加入本地忽略或用完即删，不强制 |

## Migration Plan

1. 合入 `tools/init-agents/` 与 Skill，不影响运行中业务。
2. 本仓库验证：dry-run → `-Write` 仅 suggested。
3. 回滚：删除新增目录/文件即可，无数据迁移。

## Open Questions

- 无（已在产品设计中关闭：仅 ps1、完整版、suggested 策略、dry-run 默认）。
