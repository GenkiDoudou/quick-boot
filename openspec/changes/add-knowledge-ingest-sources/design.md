## Context

- P0 已实现 `quickboot-knowledge`：四页管理端、文件上传、`DocumentIngestionService`（Tika → TokenTextSplitter → PGVector）、异步入库任务。
- 已定稿产品说明：`docs/superpowers/specs/2026-06-07-knowledge-ingest-sources-design.md`（澄清：1B、2A、3A、4D、5C、6B、7A）。
- 可复用：`IngestTaskDispatcher.afterCommit`、`KnowledgeVectorSupport`、Sa-Token 权限、C7JsonTable、`FileTemplate`/`sys_file`。

## Goals / Non-Goals

**Goals:**

- 四种文档来源：FILE、MANUAL、WEB、LIBRARY 统一进入同一入库流水线。
- 两种分段模式：AUTO（TokenTextSplitter）、CUSTOM（分隔符 + Token 合并/切分 + 重叠）。
- 预处理三开关（自动/自定义共用）：归一化空白、去 URL、去邮箱。
- 知识库级默认策略 + 单次添加可覆盖；策略快照写入 `kb_document`，`reindex` 读快照。
- 独立文档库模块（目录 + 文件），与直接上传到 KB 的 `knowledge` 分类分离。
- 网页 URL 后端抓取，含 SSRF 防护。

**Non-Goals:**

- 文档库在线编辑、版本 diff、定时爬站。
- 分段预览 UI、按库默认强制 reindex。
- 部门/库级 ACL、多模态 OCR。
- 修改语义检索/RAG 核心算法（除相似度阈值配置延续 P0）。

## Decisions

### D1：统一流水线 + 来源适配器（方案 A）

- **选择**：`DocumentSourceAdapter` 接口四种实现，产出 `List<Document>`（Spring AI）+ 归档 `file_id`；后续 `TextPreprocessor` → `ChunkStrategy` → 向量化唯一路径。
- **理由**：避免复制 ingest 后半段；与 P0 任务/向量/metadata 模型一致。
- **备选**：各来源独立 Service — 拒绝，维护成本高。

### D2：策略快照落在 `kb_document`

- **选择**：创建文档时合并 KB 默认与请求 `SegmentConfigBo`，写入文档表；`reindex` 仅读文档字段。
- **理由**：重建索引结果可复现；库级后续改默认不影响已入库文档。
- **备选**：快照存 JSON 于 `kb_ingest_task` — 拒绝，reindex 无 task 历史时不便。

### D3：文档库物理存储

- **选择**：`kb_doc_library_file.file_id` → `sys_file`，`classify=knowledge-library`；选入 KB 时**共享 file_id**（不复制 blob），`kb_document.library_file_id` 溯源。
- **理由**：节省存储；设计已确认。
- **备选**：入库时 copy 文件 — 拒绝。

### D4：CUSTOM 分块 — `DelimiterTokenChunkSplitter`

- **选择**：先按 `SINGLE_NEWLINE` / `DOUBLE_NEWLINE` 切段，再按 Token 上限合并/二次切分（必要时对超长段再跑 `TokenTextSplitter`），重叠与 AUTO 同语义。
- **理由**：与澄清 6B（Token 单位）一致；Spring AI 无开箱分隔符+Token 组合器。
- **备选**：纯字符长度 — 与 Embedding token 不对齐。

### D5：网页抓取 — `WebContentFetcher`

- **选择**：Java `HttpClient` + 重定向上限 3 + DNS 后 SSRF 黑名单；正文 Tika HTML；超限/超时 configurable。
- **配置**：`qc.knowledge.web-fetch.*`；`allowed-hosts` 可选白名单（空则仅黑名单）。
- **备选**：无 SSRF 校验 — 拒绝。

### D6：Flyway V59

- 扩展 `kb_knowledge_base`、`kb_document`；新建 `kb_doc_library_folder`、`kb_doc_library_file`；菜单 2295+；回填历史行。
- `kb_document.file_id` 改 NULLABLE；`source_type` 默认 `FILE`。

### D7：前端交互

- 文档管理：「添加文档」Dialog/Drawer 三步（来源 Tab → 分段 → 提交）。
- 新页 `knowledge/library/index.vue`：左树右表。
- 知识库表单：默认分段/预处理字段（无「继承」文案）。

### D8：文件分类

- `knowledge`：直接上传到 KB（现有）。
- `knowledge-library`：文档库专用；`qc.file.classifies` 新增项，扩展名与 50MB 限制与库配置对齐。

## Risks / Trade-offs

| 风险 | 缓解 |
|------|------|
| 网页正文提取质量差 | Tika + 明确 `error_msg`；后续可读性增强 |
| CUSTOM Token 估算偏差 | 超长段 fallback `TokenTextSplitter` |
| SSRF 绕过 | DNS 解析后校验 IP；重定向每跳复检 |
| 一次交付面大 | tasks 分阶段：DB → ingest 核心 → library API → 前端 |
| 大文本手动录入 | 512KB 上限 + 校验 |

## Migration Plan

1. 部署 V59 迁移（加列、新表、回填、菜单）。
2. 部署后端（兼容旧客户端：无 `SegmentConfigBo` 时用 KB 默认）。
3. 部署前端新向导与文档库页。
4. 回滚：保留新列可空；旧版前端仍可用 `/upload`；禁用 web-fetch 配置。

## Open Questions

- （无阻塞项）相似度默认阈值是否在同期调整 — 本变更不强制，可独立配置项。
