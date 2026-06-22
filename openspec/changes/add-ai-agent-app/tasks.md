## 1. 模块与基础设施



- [x] 1.1 父 `pom.xml` 注册 `quickboot-ai-app`；`quickboot-web` 引入依赖

- [x] 1.2 模块骨架：config/controller/service/mapper/domain/dto/runtime/tool

- [x] 1.3 `AiAppProperties`（`qc.ai-app.*`）与 `@ConditionalOnProperty(enabled=true)`

- [x] 1.4 `application.yml` 增加 `qc.ai-app` 配置示例



## 2. 数据库与权限



- [x] 2.1 Flyway：`ai_app`、`ai_app_session`、`ai_app_message`、`ai_app_publish`（含列注释与索引）

- [x] 2.2 Flyway：AI 应用菜单 + `aiapp:*` 按钮权限种子



## 3. 应用 CRUD 与发布（P0a）



- [x] 3.1 Entity/Mapper/Bo/Vo：`AiApp`、查询与详情

- [x] 3.2 `AiAppController`：list/getInfo/add/update/delete

- [x] 3.3 `AiAppService.publish`：快照 `published_config_json`、status 流转

- [x] 3.4 配置 JSON 校验：agent 必填 `chatModelId`；workflow 必填 `workflowId`



## 4. 会话与消息（P0c 基础）



- [x] 4.1 Entity/Mapper：`AiAppSession`、`AiAppMessage`

- [x] 4.2 `AiAppSessionController`：list/add/delete；`user_key` 隔离

- [x] 4.3 `AiAppMessageController`：按 session 分页列表



## 5. 智能体运行时（P0a 预览 + P0b Tool）



- [x] 5.1 `AiAppChatService`：加载配置、历史截断、SSE `delta/done/error`

- [x] 5.2 编排预览：草稿 config + 右侧调试（无发布要求）

- [x] 5.3 `KnowledgeSearchToolCallback`：`search_knowledge` 注册与 citations metadata

- [x] 5.4 `WorkflowToolCallback`：按 binding 执行已发布工作流，入参 `query`

- [x] 5.5 Tool 循环（上限 5 次）与 `tool_call` SSE 事件

- [x] 5.6 `AiAppVariableService`：变量注入 prompt + 轮次后抽取更新 `variables_json`



## 6. 高级编排运行时（P0c）



- [x] 6.1 `AiAppWorkflowChatService`：消息 → WorkflowEngine（已发布版本）

- [x] 6.2 对接 `WfStreamController` 或等价 SSE 桥接到应用聊天协议

- [x] 6.3 message metadata 写入 `runId`



## 7. 发布与嵌入（P0d）



- [x] 7.1 `AiAppPublish` CRUD：embed_token 生成、`allowed_origins`、菜单元数据

- [x] 7.2 公开 API `/ai/embed/{token}/chat/stream`：token/Origin 校验、仅 published

- [x] 7.3 访客 `user_key` 会话隔离



## 8. 千问联网（P0d）



- [x] 8.1 识别千问/DashScope 模型；`webSearch=true` 时注入 ChatOptions 扩展参数

- [ ] 8.2 非千问模型忽略开关的单测或集成验证



## 9. 前端 API 与列表（P0a）



- [x] 9.1 `quick-ui/src/api/ai/app/` 封装 CRUD、发布、会话、消息

- [x] 9.2 `/ai/app/list` C7JsonTable：创建、筛选、演示/发布入口



## 10. 智能体三栏编排（P0a–P0b）



- [x] 10.1 `/ai/app/agent/:id` 三栏布局（左提示词 / 中技能 / 右预览）

- [x] 10.2 技能区：模型、知识库、流程 binding 表、变量表、开场白、预设问题、快捷指令

- [x] 10.3 提示词优化器入口（复用现有 AI prompt 能力）

- [x] 10.4 右侧 SSE 预览调试



## 11. 高级编排页（P0c）



- [x] 11.1 `/ai/app/workflow/:id`：工作流选择 + 开场白 + 预览



## 12. 演示聊天与嵌入 UI（P0c–P0d）



- [x] 12.1 `/ai/app/chat/:appId`：多会话列表 + 消息区 + 开场白/预设问题/快捷指令

- [x] 12.2 千问联网开关；tool 状态与 citations 展示

- [x] 12.3 发布弹窗：嵌入代码、域名白名单、菜单元数据

- [x] 12.4 `/ai/embed/:token` 独立嵌入页



## 13. 验证



- [ ] 13.1 `mvn -pl quickboot-web -am test`（ai-app 相关单测/集成测）

- [x] 13.2 `pnpm build:prod`

- [ ] 13.3 手工：TC_AI_APP_001–010（见 design spec §11）

