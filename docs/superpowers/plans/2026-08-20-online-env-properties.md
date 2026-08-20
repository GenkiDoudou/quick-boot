# Online .env.properties Implementation Plan

> **For agentic workers:** 按任务顺序落地；配置跟机器走，Jenkins 不写密钥。

**Goal:** 生产用目标机 `.env.properties` 承载 DB/Redis 等可变配置；仓库只留 example 与 yml 占位。

**Architecture:** `spring.config.import: optional:file:./.env.properties`；`application-prod.yml` 引用 `${DB_*}` / `${REDIS_*}`；关闭嵌入式组件；Jenkins 只发 jar。

**Tech Stack:** Spring Boot 4、Jenkins Pipeline、`deploy/app.sh`

---

## 文件

| 文件 | 动作 |
|------|------|
| `application-prod.yml` | 占位 + 关嵌入式 |
| `.env.properties.example` | 新建骨架 |
| `.env.properties`（resources 空文件） | 删除，勿提交密钥 |
| `.gitignore` | 忽略 `**/.env.properties` |
| `deploy/env/README.md` | 文档 |
| `Jenkinsfile.quickboot` | 注释说明不写 env |

## Tasks

- [x] 设计已批准：`docs/superpowers/specs/2026-08-20-online-env-properties-design.md`
- [x] 改 prod yml / example / gitignore / README / Jenkinsfile 注释
