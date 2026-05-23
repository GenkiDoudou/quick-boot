# 后端项目结构

## Maven 目录

```text
quickboot/
├── pom.xml                 # 父 POM、依赖版本管理
├── quickboot-common/       # 通用库
├── quickboot-core/         # 预留核心（当前无业务代码）
└── quickboot-web/          # Web 应用
    └── src/main/
        ├── java/io/github/genkidoudou/
        │   └── web/        # 业务包根
        └── resources/
            ├── application.yml
            ├── application-dev.yml
            ├── application-prod.yml
            └── db/migration/   # Flyway V1..Vn
```

## Java 包结构（quickboot-web）

```text
io.github.genkidoudou.web
├── auth/                      # 认证
│   ├── AuthController
│   ├── QrcodeImageController
│   └── oauth2/
│       ├── server/            # 授权服务器 /oauth2/*
│       ├── client/            # 联邦登录
│       └── open/              # Open API
├── system/                    # RBAC 与系统配置
│   ├── user, role, menu, dept
│   ├── dict/type, dict/data
│   ├── config, notice
│   └── oauthclient, oauthprovider
├── monitor/                   # 审计与任务
│   ├── operlog, logininfor, online
│   └── job, jobLog
└── tool/
    └── gen/                   # 代码生成
```

## 分层约定

| 层 | 职责 | 命名 |
|----|------|------|
| Controller | HTTP 入参校验、OpenAPI 注解 | `*Controller` |
| Service | 业务逻辑 | `*Service` / `*ServiceImpl` |
| Mapper | MyBatis-Plus 数据访问 | `*Mapper` |
| Domain | 表实体 | `Sys*` 等 |
| DTO | 入参 `*Bo`、出参 `*Vo` | 校验注解在 Bo 上 |

约定摘要（详见 `openspec/project.md`、`sdd/后端代码规范.md`）：

- 修改/删除优先 `@PostMapping`（路径表达语义）
- 业务失败使用项目自定义异常，不用 `IllegalArgumentException`
- 统一响应 `R`，HTTP 常 200，以 JSON `code` 判成败
- `Bo/Entity/Vo` 转换优先 `BeanUtil.copyProperties`

## 数据库迁移

- 路径：`quickboot-web/src/main/resources/db/migration/`
- 命名：`V{版本}__{描述}.sql`
- 涵盖：RBAC 表、操作/登录日志、代码生成元数据、Quartz、OAuth2 表与菜单（V25–V31）

完整版本列表见 [功能模块总览](./modules/index#数据库迁移索引)。

## 相关文档

- [后端概述](./index)
- [功能模块总览](./modules/index)
