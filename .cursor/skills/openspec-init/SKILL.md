---
name: openspec-init
description: >-
  （项目内 Skill）检查 OpenSpec CLI 是否已安装并完成安装；将工作流切换为扩展全量模式；在本仓库根目录执行 openspec init / openspec update。
  Use when the user mentions OpenSpec 初始化、全量工作流、opsx 命令、openspec profile，或需要先配置 OpenSpec 再开发。
---

# OpenSpec 初始化（安装 + 全量工作流）

## 前置条件

- OpenSpec CLI 官方要求 **Node.js ≥ 20.19.0**。若 `node -v` 不满足，应先升级 Node，否则全局安装或非交互脚本可能报错或仅能勉强运行。

## 检查是否已安装

1. 在项目或任意目录执行：`openspec --version`。
2. 若命令不存在：
   - **Windows PowerShell**：`Get-Command openspec -ErrorAction SilentlyContinue`
   - **macOS/Linux**：`command -v openspec`

任一方式能解析到 `openspec` 且 `--version` 有输出即视为已安装。

## 未安装时：安装 OpenSpec

推荐全局安装（与官方文档一致）：

```bash
npm install -g @fission-ai/openspec@latest
```

也可用 pnpm / yarn / bun（见官方文档）。安装后重新打开终端或确认 PATH 中包含 npm 全局 `bin`。

安装后再执行 `openspec --version` 做验收。

## 基础前提：两种核心运行模式（参考）

OpenSpec 分为默认快速模式和扩展全量模式，新安装默认启用快速模式，可根据开发需求切换，这是选择工作流模式的基础。

1. **默认快速路径（`core` profile）**适合简单开发场景，仅提供 4 个核心命令，流程极简：`/opsx:propose` → `/opsx:apply` → `/opsx:archive`。核心命令：`/opsx:propose`（创建变更+规划制品）、`/opsx:explore`（梳理思路）、`/opsx:apply`（实现任务）、`/opsx:archive`（完成变更归档）。
2. **扩展/全量工作流（自定义开启）**适合复杂开发、团队协作场景，包含脚手架、校验、批量操作等专属命令。开启方式：执行 `openspec config profile`，必要时配合 `openspec config set`，然后在项目目录执行 `openspec update`。新增核心命令示例：`/opsx:new`、`/opsx:continue`、`/opsx:ff`、`/opsx:verify`、`/opsx:bulk-archive` 等。

说明：CLI 中非交互预设目前通常仅有 `openspec config profile core`（切回极简）；扩展全量需通过交互勾选工作流或通过 `openspec config set` 设为 `profile: custom` 并写明完整 `workflows` 列表。

## 切换为扩展全量工作流

目标：在用户本机 OpenSpec **全局配置**上启用 **全部** OPSX 工作流（包含 `core` 四步 + 扩展命令对应的工作流条目），再在**当前仓库根目录**生成/刷新指令文件。

### 方式 A — 交互式（推荐给人类操作）

在项目根或非项目目录均可执行：

```bash
openspec config profile
```

在向导中选择 **仅工作流（Change workflows only）** 或 **同时改交付与工作流**，在工作流勾选列表中勾选**全部**项（至少应包含：`propose`、`explore`、`apply`、`archive` 以及 `new`、`continue`、`ff`、`verify`、`sync`、`bulk-archive`、`onboard`）。保存后 CLI 通常会提示在项目内运行 `openspec update`。

### 方式 B — 非交互（适合 Agent / CI）

以下为与当前 OpenSpec CLI 一致的“全量工作流”数组（与 `openspec config get workflows` 的 JSON 形式一致）；在 **PowerShell** 中整条作为一行执行：

```powershell
openspec config set profile custom
openspec config set workflows '["propose","explore","new","continue","apply","ff","sync","archive","bulk-archive","verify","onboard"]'
```

在 **bash** 中：

```bash
openspec config set profile custom
openspec config set workflows '["propose","explore","new","continue","apply","ff","sync","archive","bulk-archive","verify","onboard"]'
```

验收：

```bash
openspec config list
```

确认 `profile` 为 `custom`，且 `workflows` 含上述全部项。

### 应用到当前项目（必须）

在**当前工作区对应的仓库根目录**执行（通常为 Cursor 打开的文件夹根、或存在 `.git` 的目录；与 `.cursor/skills/` 同级时即为包含 `.cursor` 的那一层）。已初始化过 OpenSpec 时执行：

```bash
openspec update
```

若仓库尚未初始化，则先在工作区根目录执行：

```bash
openspec init
```

若在配置完全量工作流**之后**才首次 init，并希望本次 init 使用当前全局所选工作流，可使用：

```bash
openspec init --profile custom
```

（文档说明：`custom` 使用 `openspec config profile` / `config set` 中当前选定的 workflows。）

## Agent 执行检查清单

- [ ] `node -v` ≥ 20.19
- [ ] `openspec --version` 成功；失败则 `npm install -g @fission-ai/openspec@latest`
- [ ] 将全局工作流设为全量（交互 `openspec config profile` **或** `config set profile` + `config set workflows`）
- [ ] **仓库根目录**（与工作区/OpenSpec 绑定目录一致）执行 `openspec init`（若未初始化）与/或 `openspec update`
- [ ] 向用户说明：后续在对话中可使用扩展命令（如 `/opsx:new`、`/opsx:continue` 等），具体以项目内生成的指令为准
