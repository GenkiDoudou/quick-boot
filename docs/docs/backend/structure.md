# 后端模块结构

## Maven 模块

| 模块 | 职责 |
|------|------|
| `quickboot-common` | 统一响应 `R`、异常、校验、缓存、Excel、脱敏、防火墙、操作日志等公共能力 |
| `quickboot-core` | 跨模块共享基础（如 `BaseEntity`） |
| `quickboot-module-system` | 系统域：用户/角色/菜单/部门/字典/配置/日志/OAuth 客户端等 |
| `quickboot-module-monitor` | 监控相关（操作/登录日志、链路、慢 SQL、部署记录等，以现网模块为准） |
| `quickboot-module-tool` | 工具能力（如代码生成） |
| `quickboot-module-quartz` | 定时任务 |
| `quickboot-module-report` | 报表（如积木报表集成） |
| `quickboot-app` | 启动组装、Flyway、Modulith 校验；**不写业务 Controller** |

## 分层约定（摘要）

业务域内典型路径：`entity` → `mapper` → `service` → `controller`，DTO/VO 按现网包结构放置。  
编码细则以仓库根 `code_formater.md` 为准；协作流程见 `AGENTS.md`。

## 配置与迁移

- 资源：`quickboot-app/src/main/resources/`
- Flyway：`db/migration`（版本脚本驱动 schema / 种子数据）
- 对外端口与业务开关：`application.yml` + profile + 可选 `.env.properties`
