## Why

AI 编程助手缺少项目事实与约定时，生成代码经常偏离技术栈与目录习惯。手动维护完整 `AGENTS.md` / `DESIGN.md` 成本高；需要可复用的初始化脚手架，在秒级探测后产出完整版 AI 规范文件，且在已有厚规范仓库中绝不覆盖正式文件。

## What Changes

- 新增 `tools/init-agents/` PowerShell 脚手架：探测前后端/文档栈，渲染完整版模板（AGENTS、DESIGN、AGENTS.local、`.agents/generation-spec`、`.agents/logs/corrections`）。
- CLI 默认 dry-run；`-Write` 落盘；`-ForceSuggested` 强制旁路建议文件；支持 `-ProjectRoot` 指向任意项目。
- 写入策略：正式 `AGENTS.md` / `DESIGN.md` / 已存在 `generation-spec` 存在时写 `*.suggested.md`；`AGENTS.local.md` 与已存在 `corrections.md` 跳过不覆盖。
- 新增 Cursor Skill `init-agents-scaffold`：指导 dry-run→write、合并 suggested、分析隐性规范与 corrections 流程；不替代脚本探测。
- 新增 `tools/init-agents/README.md` 使用说明。
- 无 **BREAKING** 变更；不修改现有业务代码与权威 `AGENTS.md` 内容。

## Capabilities

### New Capabilities

- `init-agents-cli`: PowerShell 探测、模板渲染、路径解析与安全写入（含 dry-run / suggested 旁路 / 退出码约定）。
- `init-agents-skill`: Cursor Skill 对脚手架的使用、合并与进化指引（禁止静默覆盖正式 AGENTS）。

### Modified Capabilities

- （无）当前 `openspec/specs/` 无既有能力需改需求。

## Impact

- **新增路径**：`tools/init-agents/**`、`.cursor/skills/init-agents-scaffold/SKILL.md`。
- **运行时**：仅 PowerShell；无新 Maven/npm 依赖；不调用 LLM API。
- **本仓库使用时**：`-Write` 只产生 suggested/旁路文件，不影响现有 `AGENTS.md` 权威地位。
- **可复用**：其他项目可拷贝 `tools/init-agents/` 或通过 `-ProjectRoot` 调用。
- **设计来源**：`docs/superpowers/specs/2026-08-02-init-agents-scaffold-design.md`。
