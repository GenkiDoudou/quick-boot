## 1. 数据库与权限



- [x] 1.1 Flyway `V66__ai_prompt_management.sql`：创建 `ai_prompt`、`ai_prompt_content`、`ai_prompt_variable`、`ai_prompt_version`、`ai_prompt_optimize_session`、`ai_prompt_ab_run`（含列注释、索引、唯一约束）

- [x] 1.2 Flyway 新增「提示词管理」菜单（2330）及 `ai:prompt:query|add|edit|remove|optimize` 按钮权限（2331–2335）

- [x] 1.3 Flyway 更新 menu 2320 `remark` 为「AI 大模型、MCP 与提示词管理」



## 2. 领域模型与常量



- [x] 2.1 实现枚举：`AiPromptType`、`AiPromptStatus`、`AiPromptVersionSource`、`AiPromptVarType`、`AiPromptOptimizeStatus`

- [x] 2.2 实现 Entity：`AiPrompt`、`AiPromptContent`、`AiPromptVariable`、`AiPromptVersion`、`AiPromptOptimizeSession`、`AiPromptAbRun`

- [x] 2.3 实现 Mapper 接口（MyBatis-Plus）

- [x] 2.4 实现 Bo/Vo/QueryBo 及 Jakarta Validation



## 3. 变量校验与快照工具



- [x] 3.1 实现 `AiPromptVariableValidator`：提取 `{{var}}` 根键名并与声明表比对

- [x] 3.2 实现快照构建/还原工具：sections + variables ↔ `snapshot_json`

- [x] 3.3 实现按 `promptType` 的必填段校验与 A/B prompt 拼接逻辑

- [x] 3.4 单测：变量校验、必填段、一层样例变量替换



## 4. 提示词 CRUD 与状态机



- [x] 4.1 实现 `AiPromptService`：分页、详情、add/update/remove

- [x] 4.2 实现 `publish`、`archive`、`createDraft` 及版本号递增

- [x] 4.3 实现 `GET /versions`、`GET /version/getInfo`、`GET /version/diff`

- [x] 4.4 实现 `GET /options`（仅 PUBLISHED 且非 ARCHIVED）

- [x] 4.5 非 DRAFT 禁止 update；发布前变量与必填段校验



## 5. AI 优化与 A/B



- [x] 5.1 实现 `AiPromptOptimizeService`：`POST /optimize`（同步 60s、meta-prompt、JSON 解析、session 落库）

- [x] 5.2 实现 `POST /optimize/adopt`（写入 version_id=0，PUBLISHED 转草稿语义）

- [x] 5.3 实现 `AiPromptAbService`：`POST /ab/run`（并行 Chat、总超时 60s）、`POST /ab/score`

- [x] 5.4 经 `AiModelResolver` 解析 Chat 模型；`qc.ai.enabled=false` 时优化/A/B 抛业务异常

- [x] 5.5 单测或集成测：优化 JSON 解析失败、超时、采纳不自动发布



## 6. 提示词管理 API



- [x] 6.1 实现 `AiPromptController`（`/ai/prompt/**`）及 OpenAPI `@Tag`/`@Operation`

- [x] 6.2 权限注解对齐 `ai:prompt:*`

- [x] 6.3 验证 `mvn -pl quickboot-web -am clean compile -DskipTests` 通过



## 7. 前端 API 与路由



- [x] 7.1 新增 `api/ai/prompt.js` 封装全部接口（JSDoc 标注路径与入参）

- [x] 7.2 配置路由 `/ai/prompt`、`/ai/prompt/edit/:promptId?`（隐藏子路由或独立页）



## 8. 前端列表页



- [x] 8.1 新增 `views/ai/prompt/index.vue`（C7JsonTable，对齐 `ai/model/index.vue`）

- [x] 8.2 搜索列：name、code、promptType、domain、category、status

- [x] 8.3 行操作：编辑、发布（DRAFT）、停用（PUBLISHED）、删除；权限 `v-hasPermi`



## 9. 前端编辑页 — 内容 Tab



- [x] 9.1 新增 `views/ai/prompt/edit.vue`（或拆分组件），Tab 骨架：内容 | 优化 | 版本 | A/B

- [x] 9.2 基础信息表单：name、code、promptType、domain、category、tags、optimizeModelId

- [x] 9.3 按 `promptType` 动态段编辑器；变量表增删行

- [x] 9.4 实时 `{{var}}` 校验提示（移植或复用工作流校验逻辑）

- [x] 9.5 按钮：保存草稿、发布、从已发布创建草稿



## 10. 前端编辑页 — 优化 / 版本 / A/B Tab



- [x] 10.1 优化 Tab：优化目标、模型选择、`POST /optimize` loading（≤60s）、Diff 展示、采纳/放弃

- [x] 10.2 版本 Tab：版本列表、快照查看、与当前 Diff、回滚为草稿

- [x] 10.3 A/B Tab：版本 A/B 选择、样例变量表单、`POST /ab/run` 双栏输出、评分提交



## 11. 验证与验收



- [x] 11.1 执行 `pnpm build:prod`（quick-ui）通过

- [ ] 11.2 手工验收：TC_AI_PROMPT_001/010/020/030/050（见设计文档 §11）

- [ ] 11.3 手工验收：`qc.ai.enabled=false` 时 CRUD 可用、optimize 返回明确错误

