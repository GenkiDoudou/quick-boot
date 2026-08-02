## ADDED Requirements

### Requirement: CLI 默认 dry-run 且支持显式写入

`init-agents.ps1` MUST 接受 `-ProjectRoot`、`-Write`、`-ForceSuggested`。未指定 `-Write` 时 MUST NOT 创建或修改任何项目文件，MUST 在终端打印将执行的动作清单并提示需添加 `-Write` 才落盘。

#### Scenario: 本仓库 dry-run 无文件变更

- **WHEN** 用户在已存在 `AGENTS.md` 的项目根执行 `init-agents.ps1` 且未传 `-Write`
- **THEN** 进程退出码为 0，终端列出各产物动作为 `create-suggested` 或 `skip`，且工作区无新增/修改的规范文件

#### Scenario: Write 落盘

- **WHEN** 用户对可识别项目执行 `init-agents.ps1 -Write`
- **THEN** 系统按路径解析规则创建对应正式或旁路文件，终端打印每个产物的最终动作

### Requirement: 正式 AGENTS/DESIGN 永不覆盖

当目标根已存在 `AGENTS.md` 或 `DESIGN.md` 时，脚手架 MUST 将对应内容写入 `AGENTS.suggested.md` / `DESIGN.suggested.md`，MUST NOT 修改正式文件内容。`-ForceSuggested` 为真时，即使正式文件不存在，也 MUST 只写旁路建议文件。

#### Scenario: 已有 AGENTS 时写建议文件

- **WHEN** 项目根已存在 `AGENTS.md` 且用户执行 `-Write`
- **THEN** 创建或覆盖 `AGENTS.suggested.md`，且 `AGENTS.md` 字节内容保持不变

#### Scenario: ForceSuggested 在空项目

- **WHEN** 项目根不存在 `AGENTS.md` 且用户执行 `-ForceSuggested -Write`
- **THEN** 创建 `AGENTS.suggested.md`，且不创建正式 `AGENTS.md`

#### Scenario: 无 AGENTS 时创建正式文件

- **WHEN** 项目根不存在 `AGENTS.md` 且用户执行 `-Write` 且未传 `-ForceSuggested`
- **THEN** 创建正式 `AGENTS.md`

### Requirement: local 与 corrections 存在则跳过

`AGENTS.local.md` 或 `.agents/logs/corrections.md` 已存在时，脚手架 MUST 跳过写入（动作 `skip`）。不存在时，`-Write` MUST 创建对应文件；新建 `AGENTS.local.md` 时，若根目录已有 `.gitignore` 且尚未忽略该文件，MUST 追加忽略规则；若无 `.gitignore`，MUST 仅打印提示且 MUST NOT 强制创建 `.gitignore`。

#### Scenario: 跳过已有 local

- **WHEN** `AGENTS.local.md` 已存在且用户执行 `-Write`
- **THEN** 不修改该文件，终端动作显示 `skip`

#### Scenario: 新建 local 并更新 gitignore

- **WHEN** `AGENTS.local.md` 不存在、根目录存在 `.gitignore` 且其中无 `AGENTS.local.md`，用户执行 `-Write`
- **THEN** 创建 `AGENTS.local.md`，并在 `.gitignore` 中追加对该文件的忽略规则

### Requirement: generation-spec 旁路与目录创建

`.agents/generation-spec.md` 已存在时 MUST 写入 `.agents/generation-spec.suggested.md`；不存在且未 `-ForceSuggested` 时 MUST 写入正式路径。`-Write` 时 MUST 按需创建 `.agents` 与 `.agents/logs` 父目录。所有写出文本 MUST 为 UTF-8 无 BOM。

#### Scenario: 已有 generation-spec

- **WHEN** `.agents/generation-spec.md` 已存在且用户执行 `-Write`
- **THEN** 写入 `.agents/generation-spec.suggested.md`，正式 `generation-spec.md` 不变

### Requirement: 技术栈探测与失败退出

脚手架 MUST 探测前端（`package.json` 及约定子路径）、后端（`pom.xml` 或 Gradle 构建文件）、文档（如 VitePress）及规范线索路径（如 `sdd/`、`openspec/project.md`、`.cursor/rules/`）。当 Frontends、Backends、Docs 均为空时 MUST 以非 0 退出。`-ProjectRoot` MUST 为已存在目录，否则非 0 退出。

#### Scenario: 空目录失败

- **WHEN** 用户对不含 package.json/pom/Gradle 的空目录执行脚本
- **THEN** 进程以非 0 退出且不写入规范文件

#### Scenario: 全栈仓库探测成功

- **WHEN** 用户对本仓库（含 Maven 与 Vue 前端）执行 dry-run
- **THEN** 退出码为 0，终端摘要包含已识别的前端与后端特征

### Requirement: 完整版模板渲染

脚手架 MUST 渲染完整版内容：AGENTS（含固定章节顺序与完整版 Quick Commands）、DESIGN（含随 UI 库变化的 tokens）、generation-spec（按已探测栈输出对应骨架小节）、corrections（表头模板）。Never / Multi-Agent / 协作摘要 MUST 来自模板常量，MUST NOT 从依赖猜测。写入目标 MUST 限制在约定相对路径白名单内，MUST NOT 写入 `node_modules` 或 `.git` 内容，MUST NOT 读取或打印 `.env` 密钥。

#### Scenario: 最小 Vue 项目生成正式全套

- **WHEN** 临时目录仅含声明 vue 与 element-plus 的 `package.json` 且用户执行 `-Write`
- **THEN** 生成 `AGENTS.md`、`DESIGN.md`、`AGENTS.local.md`、`.agents/generation-spec.md`、`.agents/logs/corrections.md`，且 AGENTS/DESIGN 内容反映 Vue 与 Element Plus

#### Scenario: 旁路建议可刷新

- **WHEN** `AGENTS.suggested.md` 已存在且用户再次执行 `-Write`
- **THEN** 允许覆盖该建议文件，且仍不修改正式 `AGENTS.md`
