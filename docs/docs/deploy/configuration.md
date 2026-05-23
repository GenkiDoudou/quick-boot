# 配置说明

## 后端 Profile

| 文件 | Profile | 用途 |
|------|---------|------|
| `application.yml` | 公共 | 端口、Flyway、MyBatis-Plus、`qc.*` |
| `application-dev.yml` | dev | H2、H2 Console、开发 SM4 密钥 |
| `application-prod.yml` | prod | MySQL ENC、Redis Token |

启动指定 Profile：

```bash
mvn -pl quickboot-web spring-boot:run -Dspring-boot.run.profiles=prod
```

## 关键配置项

```yaml
qc:
  oauth2:
    token-store: local   # prod 改为 redis
  security:
    client-sign:
      enabled: true
    web:
      anonymous-paths: [...]
  monitor:
    operlog:
      enabled: true
```

完整项见 `quickboot-web/src/main/resources/application.yml`。

## 前端环境变量

| 变量 | 说明 |
|------|------|
| `VITE_APP_BASE_API` | API 前缀，开发多为 `/dev-api` |
| `VITE_APP_CLIENT_ID` | OAuth Client |
| `VITE_APP_CLIENT_SIGN_KEY` | HMAC 密钥（生产 CI 注入） |

文件：`.env.development`、`.env.production`。

## Jasypt

```bash
-Djasypt.encryptor.password=<主密钥>
```

生产数据库密码等使用 `ENC(...)` 包裹。

## 相关

- [OAuth2 生产清单](../backend/modules/oauth2#生产配置清单)
- [本地联调](./local-testing)
