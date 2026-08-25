# 后端快速上手

命令均在仓库根目录下执行。依赖见 [环境搭建](/docs/guide/installation)。

## 构建与启动

```bash
cd quickboot
mvn -pl quickboot-app -am install -DskipTests
mvn -pl quickboot-app spring-boot:run
```

或：

```bash
mvn clean install -DskipTests
mvn -pl quickboot-app -am spring-boot:run
```

## 启动后自检

- 端口：**9993**
- 健康检查：`GET http://127.0.0.1:9993/actuator/health`（dev 通常可匿名）
- OpenAPI / Swagger：以启动日志中的 SpringDoc 路径为准
- JDBC：默认 `jdbc:mariadb://127.0.0.1:3307/quickboot`（用户 `root`，密码空）

## 嵌入式依赖

| 组件 | 默认 | 关闭方式 |
|------|------|----------|
| MariaDB | 开启，数据目录 `./data/mariadb` | `qc.dev.embedded-mariadb.enabled=false` |
| Redis | 开启，`127.0.0.1:6379` | `qc.dev.embedded-redis.enabled=false` |

关闭后需自行配置外部库与 Redis。

## 配置与密钥

- 主配置：`quickboot-app/src/main/resources/application.yml` 及 profile 文件
- 本地可变项示例：`.env.properties.example`（勿提交真实密钥）
- 生产：目标机 jar 同级 `.env.properties`，见 `deploy/env/README.md`

若启用 Jasypt，启动时需提供加密主密钥（以当前 `application*.yml` 与 README 为准）。

## 默认账号与客户端

- 用户：`admin` / `admin123`
- 管理端 client：`quick-ui` / `quick-ui-secret`
- H5 client：`quick-h5` / `quick-h5-secret`

更多认证说明见仓库 `quickboot/README.md`。
