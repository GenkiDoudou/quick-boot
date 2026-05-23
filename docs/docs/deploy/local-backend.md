# 本地后端部署

```bash
cd quickboot
mvn clean install -DskipTests
mvn -pl quickboot-web spring-boot:run -Djasypt.encryptor.password=你的密钥
```

- 端口：**9992**
- 数据库：dev 使用 H2 文件 `./data/qcc`，Flyway 自动迁移
- Swagger：`/swagger-ui.html`
- H2 Console：仅 dev 开启，勿暴露公网

## 切换 MySQL（可选）

1. 修改 `application-dev.yml` 数据源为 MySQL  
2. 或 `-Dspring-boot.run.profiles=prod` 并提供 Jasypt 与 Redis  

## 相关

- [本地前端](./local-frontend)
- [联调测试](./local-testing)
