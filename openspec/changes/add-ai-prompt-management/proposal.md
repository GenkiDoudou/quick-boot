## Why

QuickBoot 在工作流 LLM 节点、RAG 问答、问题分类、参数抽取等场景中大量使用提示词，但内容分散在节点表单或后端硬编码中，缺少统一台账与版本管理。AI 大模型管理（`add-ai-model-management`）已将「Prompt 模板管理」列为非目标；在 `quickboot-ai` 与 `AiModelRegistry` 就绪后，现需交付独立的提示词库，并支持 AI 优化、版本 Diff 与 A/B 对比，形成库内闭环。

## What Changes

- 在 **`quickboot-ai`** 扩展 `prompt` 子域：Entity/Service/Controller，包路径 `io.github.genkidoudou.web.ai.prompt.*`。
- 新增 MySQL 表 **`ai_prompt`**、**`ai_prompt_content`**、**`ai_prompt_variable`**、**`ai_prompt_version`**、**`ai_prompt_optimize_session`**、**`ai_prompt_ab_run`**（Flyway 新迁移）。
- 新增提示词 CRUD / 发布 / 归档 / 草稿 / 版本 / Diff / AI 优化 / 采纳 / A/B 运行与评分 API，前缀 **`/ai/prompt`**；权限 **`ai:prompt:*`**。
- 支持 5 种 **`promptType`**（LLM/RAG/CLASSIFIER/EXTRACTOR/CUSTOM）多段内容、变量声明与 `{{var}}` 校验、草稿/已发布/归档状态机。
- AI 优化与 A/B 复用 **`AiModelRegistry`** 同步调用 Chat 模型（超时 60s）；`qc.ai.enabled=false` 时 CRUD 可用、优化/A/B 不可用。
- 「AI 能力」菜单下新增 **「提示词管理」**（menu_id 2330+）；前端 `views/ai/prompt/`（C7JsonTable 列表 + 编辑页 Tab：内容 | 优化 | 版本 | A/B）。
- 新增 `api/ai/prompt.js` 封装全部接口。

## Capabilities

### New Capabilities

- `ai-prompt`：提示词模板 CRUD、多段内容、变量校验、草稿/发布/归档、版本快照与 Diff、AI 优化与采纳、A/B 对比与评分、菜单与权限

### Modified Capabilities

（无。本期不与工作流、RAG 等业务模块打通，不修改既有 spec 行为。）

## Impact

- **后端**：`quickboot-ai` 新增 `prompt` 包；Flyway 菜单与表结构；无新 Maven 模块。
- **前端**：`views/ai/prompt/index.vue`、`views/ai/prompt/edit.vue`（或等价路由）、`api/ai/prompt.js`；路由 `/ai/prompt/**`。
- **依赖**：依赖已实现的 `add-ai-model-management`（`AiModelResolver` / `AiModelRegistry`）；`qc.ai.enabled` 控制优化类接口。
- **兼容**：无 BREAKING 变更；工作流/RAG 引用提示词模板为二期预留。
- **参考设计**：`docs/superpowers/specs/2026-06-14-ai-prompt-management-design.md`
