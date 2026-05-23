# Docker Compose

参考编排（需自行创建 `deploy/docker-compose.yml` 并替换密码）：

```yaml
services:
  mysql:
    image: mysql:8
    environment:
      MYSQL_ROOT_PASSWORD: ${MYSQL_ROOT_PASSWORD}
      MYSQL_DATABASE: quickboot
    volumes:
      - mysql_data:/var/lib/mysql
    ports:
      - "3306:3306"

  redis:
    image: redis:7-alpine
    ports:
      - "6379:6379"

  quickboot:
    image: quickboot-web:latest
    depends_on: [mysql, redis]
    environment:
      SPRING_PROFILES_ACTIVE: prod
      JASYPT_ENCRYPTOR_PASSWORD: ${JASYPT_PASSWORD}
      QC_SM4_KEY_HEX: ${QC_SM4_KEY_HEX}
      SPRING_DATASOURCE_URL: jdbc:mysql://mysql:3306/quickboot?...
      SPRING_DATA_REDIS_HOST: redis
    ports:
      - "9992:9992"

  quick-ui:
    image: quickboot-ui:latest
    ports:
      - "80:80"
    depends_on: [quickboot]

volumes:
  mysql_data:
```

## 生产要点

- `qc.oauth2.token-store=redis`
- `qc.security.client-sign.enabled=true`
- 首次启动 Flyway 自动建表
- 前端构建时注入 `VITE_APP_CLIENT_SIGN_KEY`

## 相关

- [镜像构建](./docker-build)
- [配置说明](./configuration)
