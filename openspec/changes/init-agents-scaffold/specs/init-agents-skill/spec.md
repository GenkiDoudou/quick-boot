## ADDED Requirements

### Requirement: Skill 指导安全使用脚手架

仓库 MUST 提供 Cursor Skill，路径为 `.cursor/skills/init-agents-scaffold/SKILL.md`。当用户意图涉及 init-agents、生成/同步 AGENTS 脚手架、合并 `AGENTS.suggested` 时，Agent MUST 按该 Skill 执行：先运行脚本 dry-run，经用户确认后再使用 `-Write`。

#### Scenario: 用户请求初始化 AI 规范

- **WHEN** 用户要求生成或同步项目 AI 规范文件
- **THEN** Agent 先执行 `init-agents.ps1` dry-run 并向用户展示计划，在用户确认前不执行 `-Write`

### Requirement: Skill 禁止静默覆盖正式 AGENTS

Skill MUST 明确禁止 Agent 在未运行脚手架或不经用户确认的情况下，凭猜测整文件重写正式 `AGENTS.md`。若存在 `*.suggested.md`，Agent MUST 指引或协助对比正式文件与建议文件，且合并时 MUST 优先只吸收事实层（技术栈、命令、目录），MUST NOT 覆盖用户手写策略段落，除非用户明确要求。

#### Scenario: 合并 suggested

- **WHEN** 用户要求把 `AGENTS.suggested.md` 合并进已有 `AGENTS.md`
- **THEN** Agent 展示差异摘要，仅在用户确认后修改正式文件，且默认保留既有协作/Never 手写内容

### Requirement: Skill 覆盖完整版进化入口

Skill MUST 说明完整版能力入口：对 AI 说「分析项目规范」以蒸馏隐性习惯（可更新 `.agents/` 或提出补丁，MUST NOT 静默覆盖 `AGENTS.md`）；纠正错误后追加 `.agents/logs/corrections.md`；将 correction 升级为 Never 前 MUST 征得用户确认。

#### Scenario: 记录 correction

- **WHEN** 用户确认某次 AI 错误应记入 corrections
- **THEN** Agent 在 `corrections.md` 追加一行记录，且不自动改写 Never 规则，除非用户另行确认升级
