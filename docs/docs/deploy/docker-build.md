# 镜像构建

仓库当前**未内置 Dockerfile**，可按下列参考在 CI 中构建。

## 后端镜像（示例）

```dockerfile
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY quickboot/quickboot-web/target/quickboot-web-*.jar app.jar
ENV JAVA_OPTS="-Xms512m -Xmx1024m"
EXPOSE 9992
ENTRYPOINT ["sh","-c","java $JAVA_OPTS -jar app.jar --spring.profiles.active=prod"]
```

构建前：

```bash
cd quickboot
mvn -pl quickboot-web -am package -DskipTests
```

启动需传入 `JASYPT_PASSWORD`、`QC_SM4_KEY_HEX`、数据库与 Redis 环境变量。

## 前端镜像（示例）

```dockerfile
FROM node:20-alpine AS build
WORKDIR /app
COPY quick-ui/package.json quick-ui/pnpm-lock.yaml ./
RUN corepack enable && pnpm i --frozen-lockfile
COPY quick-ui/ .
ARG VITE_APP_BASE_API=/prod-api
ARG VITE_APP_CLIENT_ID=quick-ui
ARG VITE_APP_CLIENT_SIGN_KEY
RUN pnpm build:prod

FROM nginx:alpine
COPY --from=build /app/dist /usr/share/nginx/html
COPY deploy/nginx/default.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
```

生产密钥通过 **构建参数** 注入，勿写入镜像层历史。

## 相关

- [Docker Compose](./docker-compose)
- [Nginx 配置](./nginx)
