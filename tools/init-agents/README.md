# init-agents

PowerShell 脚手架：探测前后端/文档技术栈，生成 AI 规范文件（中文，UTF-8 无 BOM）。

## 用法

```powershell
# 默认直接落盘
.\tools\init-agents\init-agents.ps1

# 仅预览（不写文件）
.\tools\init-agents\init-agents.ps1 -DryRun

.\tools\init-agents\init-agents.ps1 -ProjectRoot D:\other-app
.\tools\init-agents\init-agents.ps1 -ForceSuggested
```

## 写入策略（每次落盘都会覆盖目标文件）

| 目标 | 行为 |
|------|------|
| `AGENTS.md` | 若已存在正式文件 → **覆盖** `AGENTS.suggested.md`（永不直接改正式 AGENTS.md）；否则创建/覆盖正式 `AGENTS.md` |
| `code_formater.md` | 同上（正式文件存在则写 `code_formater.suggested.md`）；编码事实源模板 |
| `DESIGN.md` | **每次覆盖**正式 `DESIGN.md`（`-ForceSuggested` 时改为写 `DESIGN.suggested.md`） |
| `AGENTS.local.md` | **每次覆盖** |
| `.agents/generation-spec.md` | **每次覆盖**（`-ForceSuggested` 时写 `*.suggested.md`） |
| `.agents/logs/corrections.md` | **每次覆盖** |

默认写入。产出 UTF-8 无 BOM；含中文的脚本源文件为 UTF-8 带 BOM（兼容 Windows PowerShell 5.1）。

## 非目标

无 bash 版、无自动 commit、无 LLM 调用、无 corrections 自动升级 Never。

详见：`.cursor/skills/init-agents-scaffold/SKILL.md`