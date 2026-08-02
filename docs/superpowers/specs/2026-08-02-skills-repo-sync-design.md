# 共享 Skills 仓 → 项目 `.cursor/skills` 增量同步设计

**日期：** 2026-08-02  
**状态：** 已实现（计划：`docs/superpowers/plans/2026-08-02-skills-repo-sync.md`；skills 仓 `E:\workspace\luyanan\skills`）  
**范围：** 将独立仓库 `skills.git`（目录 `skills/` + `tools/`）增量同步到本项目（及同类项目）的 Cursor Skill 目录，不影响本地独有 Skill，且共享 `tools/` 可执行；**不**把该仓作为本仓库的 git 子模块/依赖。

## 1. 背景与目标

独立仓库约定结构：

```text
skills.git/          # 例：https://git.su60.com/luyanan/skills.git
├── skills/          # 各 Skill 目录（含 SKILL.md）
└── tools/           # 可执行工具 / 脚本
```

需求：

1. 把共享 `skills/*` 同步进目标项目的 `.cursor/skills/`，供 Cursor Agent 加载。
2. **不影响**项目内其它（本地独有）Skill。
3. 共享 `tools/` 仍可被 Skill 调用执行。
4. 本项目 git **不**引入中央仓（无 submodule / 无强制 vendor 提交）。
5. 在项目里对已链接共享 Skill 的修改，应直接落在 clone 上，便于在 skills 仓 commit/push 同步到其它项目/机器。

成功标准：

1. 跑同步脚本后，共享名单内的 Skill 出现在 `.cursor/skills/<name>`，且内容与 clone 中 `skills/<name>` 一致（链接同一目标）。
2. 同步前后，不在共享名单内的本地 Skill 目录内容与路径均不变。
3. 通过约定环境变量 `SKILLS_ROOT` 能执行 clone 内 `tools/` 下脚本；本项目已有 `tools/init-agents/` **不被**覆盖或混入。
4. 修改 `.cursor/skills/<共享名>/SKILL.md` 后，在 clone 目录 `git status` 能看到对应变更（证明写入落在 clone，而非项目内副本）。

## 2. 决策摘要

| 项 | 决策 |
|----|------|
| 挂载方式 | 本机独立 clone；**不**进本仓库 git |
| 进 `.cursor/skills` 的方式 | Windows **Directory Junction**（或等价 symlink）；优先方案，非整目录拷贝 |
| 并存策略 | **按名字增量**：仅处理共享名单；名单外目录完全跳过 |
| 同名冲突 | 见 §5：默认「真实目录且非 junction → 跳过并告警」；显式 `-Force` 才改链 |
| tools | 留在 clone 的 `tools/`；Skill 用 `SKILLS_ROOT`；**不**写入本项目 `tools/` |
| 脚本位置 | 放在 **skills 仓**内（随 clone 分发），本项目可选提供一键调用包装 |
| 不做 | submodule、把整个 `.cursor/skills` 换成远程目录、合并进本项目 `tools/`、自动 git commit/push |

## 3. 推荐布局

```text
# 本机独立 clone（路径可配置，下文称 SKILLS_ROOT）
%SKILLS_ROOT%/
├── skills/
│   ├── foo/
│   └── bar/
├── tools/
│   └── ...
└── sync/                    # 同步脚本（实现阶段落地）
    └── Sync-CursorSkills.ps1

# 目标项目（如 quickboot）
<ProjectRoot>/
├── .cursor/skills/
│   ├── quickboot-system-codegen/   # 本地独有：真实目录，脚本跳过
│   ├── foo/  ==junction==>  %SKILLS_ROOT%/skills/foo
│   └── bar/  ==junction==>  %SKILLS_ROOT%/skills/bar
└── tools/
    └── init-agents/                # 项目自有，与共享 tools 隔离
```

`SKILLS_ROOT` 默认解析顺序（实现时可微调，须写进脚本帮助）：

1. 环境变量 `SKILLS_ROOT`
2. 脚本参数 `-SkillsRoot`
3. 可选：用户配置文件（如 `%USERPROFILE%\.config\luyanan-skills\root.txt`）仅存一行路径

## 4. 共享名单

两种等价来源（实现时二选一或同时支持，优先 A）：

- **A（推荐）：** `%SKILLS_ROOT%/skills/` 下**每个一级子目录**均为共享 Skill（目录即名单）。
- **B（可选）：** `%SKILLS_ROOT%/manifest.txt`，每行一个 Skill 名；用于显式排除尚未就绪的目录。

名单解析结果 =「将尝试链接到 `.cursor/skills/<name>` 的集合」。

## 5. 同名冲突策略

对每个共享名 `N`，检查 `<ProjectRoot>/.cursor/skills/N`：

| 现状 | 默认行为（无 `-Force`） | `-Force` |
|------|-------------------------|----------|
| 不存在 | 创建 junction → `%SKILLS_ROOT%/skills/N` | 同左 |
| 已是指向正确目标的 junction | 跳过（已同步） | 同左 |
| 已是指向错误目标的 junction | 删除旧 junction，重建为正确目标 | 同左 |
| 真实目录（非 junction） | **跳过并告警**，不删除、不覆盖 | 将真实目录重命名为 `N.bak-YYYYMMDD-HHMMSS` 后创建 junction |
| 普通文件或其它异常类型 | 跳过并告警 | 同左（不盲目删除文件） |

说明：此前口头「同名以共享侧为准」在实现上收敛为：**默认保护本地真实目录**；需要覆盖时显式 `-Force` 并先备份。避免误删项目私有 Skill。

## 6. 同步脚本行为

建议入口：`%SKILLS_ROOT%/sync/Sync-CursorSkills.ps1`

必要参数：

- `-ProjectRoot`：目标项目根（含 `.cursor` 的仓库根）
- `-SkillsRoot`：可选，覆盖 `SKILLS_ROOT`
- `-Force`：按 §5 处理真实目录冲突
- `-WhatIf` / 默认 dry-run：**推荐默认只打印计划**，`-Apply` 才真正创建/删除 junction（与 init-agents 的 dry-run 习惯对齐）

步骤概要：

1. 校验 `%SKILLS_ROOT%/skills` 与 `%SKILLS_ROOT%/tools` 存在。
2. 解析共享名单。
3. 确保 `<ProjectRoot>/.cursor/skills` 存在。
4. 对名单内每一项按 §5 处理；**从不**遍历删除「不在名单中」的本地 Skill。
5. 打印摘要：已链接 / 已跳过 / 已备份 / 失败。

Windows 创建 junction 参考（实现细节可封装）：

```powershell
cmd /c mklink /J ".cursor\skills\foo" "%SKILLS_ROOT%\skills\foo"
```

或 `New-Item -ItemType Junction`。不要求管理员权限（junction 通常无需提权；symlink 可能需要）。

## 7. tools 调用约定

- 共享工具路径：`%SKILLS_ROOT%/tools/...`
- Skill 正文须写明：执行前确认 `SKILLS_ROOT` 已设置；示例命令使用该变量，**禁止**假定工具在项目根 `tools/` 下。
- 本项目 `tools/init-agents/` 保持独立，同步脚本**零触及**。

可选便利：在用户配置或项目 `AGENTS.local.md` 中记录本机 `SKILLS_ROOT` 路径（个人文件，宜 gitignore），方便 Agent 读取。

## 8. 日常流程

| 场景 | 操作 |
|------|------|
| 首次 | clone skills 仓 → 设 `SKILLS_ROOT` → 对项目跑同步脚本 `-Apply` |
| 拉取共享更新 | 在 clone 中 `git pull`；若有**新** Skill 目录再跑同步补链接 |
| 修改共享 Skill | 在 Cursor 中编辑 `.cursor/skills/<名>`（写入 clone）→ 在 clone 中 commit/push |
| 其它项目启用 | 对另一 `ProjectRoot` 再跑同一同步脚本 |
| 本地独有 Skill | 直接放在 `.cursor/skills/` 真实目录；不进共享名单则永不被脚本改动 |

## 9. 范围外

- 将 skills 仓注册为本仓库 submodule / subtree。
- 自动向 skills 仓 commit 或 push。
- 非 Windows 的 symlink 细节（若需要可后续加 bash 版；首版仅 PowerShell + junction）。
- 把现有 quickboot 全部 `.cursor/skills` **迁入** skills 仓（可另开变更；本设计只定义同步机制）。

## 10. 实现提示（供后续 writing-plans）

1. 在空的 `skills.git` 中落地目录骨架：`skills/`、`tools/`、`sync/Sync-CursorSkills.ps1`、简短 README（`SKILLS_ROOT`、同步用法）。
2. 脚本实现 §4–§6，带 `-WhatIf` 默认与 `-Apply`。
3. 用 quickboot 做一次验证：链接 1 个测试 Skill，确认本地独有目录未变、编辑落在 clone、`tools` 样例可执行。
4. （可选）在本仓库 `docs` 或 `AGENTS.local` 模板中加一行「如何设置 SKILLS_ROOT」，**不**强制改正式 `AGENTS.md`。
