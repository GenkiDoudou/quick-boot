# 线上可变配置维护设计

**日期：** 2026-08-20  
**状态：** 已批准

## 目标

用低复杂度方式维护线上 DB / Redis 等可变配置：密钥不进 Git、不进 Jenkins 每次构建参数；发版与改密解耦。

## 方案

**配置跟目标机走；Jenkins 主路径只发 jar。**

```text
${DEPLOY_DIR}/          # 如 /opt/quickboot/app 或 /home/quickboot2/app
  quickboot-app.jar     # Jenkins 覆盖
  app.sh
  .env.properties       # 运维常驻维护；Jenkins 默认不写
```

应用已支持：`spring.config.import: optional:file:./.env.properties`（相对进程工作目录，即 `app.sh` 所在目录）。

## 职责划分

| 位置 | 内容 |
|------|------|
| Git：`application-prod.yml` | 生产行为 + `${DB_URL}` 等占位；关闭嵌入式 DB/Redis |
| Git：`.env.properties.example` | 键名骨架，无真实密钥 |
| 目标机：`.env.properties` | 唯一真相；改配置后 `./app.sh restart` |
| Jenkins | 构建、上传 jar、`app.sh deploy`；**不**生成/覆盖 `.env.properties` |

## 非目标

- 不在 Job 文本域粘贴整份 env
- 不上 Vault / 配置中心（机器增多后再议）
- 不改 `application-dev.yml` 本地嵌入式开发路径

## 运维流程

1. **首装：** 拷贝 example → `.env.properties`，填真实值，再首次 deploy jar  
2. **发版：** Jenkins 只更新 jar 并重启  
3. **改密/换库：** SSH 改 `.env.properties` → `./app.sh restart`
