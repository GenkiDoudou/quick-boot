## 1. 目录与探测模块

- [x] 1.1 创建 `tools/init-agents/`、`lib/`、`templates/` 目录结构
- [x] 1.2 实现 `lib/Detect-Stack.ps1`：前端 package.json、后端 pom/Gradle、docs/VitePress、SpecHints、Monorepo 标记
- [x] 1.3 在本仓库根对 Detect-Stack 做一次手工核对（摘要含 Vue/Maven 等关键事实）

## 2. 模板与渲染

- [x] 2.1 编写完整版五件套模板：`AGENTS.md.tmpl`、`DESIGN.md.tmpl`、`AGENTS.local.md.tmpl`、`generation-spec.md.tmpl`、`corrections.md.tmpl`
- [x] 2.2 实现 `lib/New-AgentsDocs.ps1`：占位替换、前端/后端/文档段落开关、UI 主色优先级

## 3. 写入与 CLI

- [x] 3.1 实现 `lib/Write-AgentsDocs.ps1`：正式/旁路/skip 路径解析、`-ForceSuggested`、UTF-8 无 BOM、gitignore 追加、父目录创建
- [x] 3.2 实现入口 `init-agents.ps1`：`-ProjectRoot`/`-Write`/`-ForceSuggested`、探测失败非 0、终端动作清单与 dry-run 提示
- [x] 3.3 编写 `tools/init-agents/README.md`（用法、写入策略、非目标）

## 4. Skill

- [x] 4.1 新增 `.cursor/skills/init-agents-scaffold/SKILL.md`（dry-run→write、合并 suggested、分析规范、corrections、禁止静默覆盖）

## 5. 验证

- [x] 5.1 本仓库 dry-run：退出 0、AGENTS/DESIGN 计划为 create-suggested、无文件变更
- [x] 5.2 本仓库 `-Write`：生成 suggested 等旁路文件，`AGENTS.md` 内容不变
- [x] 5.3 临时最小 Vue+Element Plus 目录 `-Write`：生成正式五件套（含 local）
- [x] 5.4 临时目录 `-ForceSuggested -Write`：仅 suggested、无正式 AGENTS
- [x] 5.5 空目录执行：非 0 退出
