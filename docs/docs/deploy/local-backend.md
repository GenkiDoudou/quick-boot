# 本地后端部署

```bash
cd quickboot
mvn clean install -DskipTests
mvn -pl quickboot-web spring-boot:run -Djasypt.encryptor.password=你的密钥
```

- 端口：**9992**
- 数据库（三选一）：
  - **dev**（默认）：远程 MySQL `192.168.50.105:3406/qc2`
  - **dev-embedded**：内嵌 MariaDB4J，数据目录 `data/mariadb4j/`（可整目录拷贝换机），见下文
  - **prod profile**：生产 MySQL + Redis

## 内嵌 MariaDB4J（dev-embedded，无需 Docker / 远程 MySQL）

```bash
cd quickboot
mvn -pl quickboot-web spring-boot:run -Dspring-boot.run.profiles=dev,dev-embedded
```

- JDBC：`127.0.0.1:3307/qc2`，root 无密码
- 数据目录：仓库根 `data/mariadb4j/`（gitignore，换电脑时整目录拷贝）
- 首次空目录：Flyway 自动建表；若要现网演示数据，执行 `scripts/mariadb4j-import-mysql-dump.ps1`
- 详情：`data/mariadb4j/README.md`

## 切换远程 MySQL（可选）

1. 修改 `application-dev.yml` 数据源为 MySQL  
2. 或 `-Dspring-boot.run.profiles=prod` 并提供 Jasypt 与 Redis  

## 相关

- [本地前端](./local-frontend)
- [联调测试](./local-testing)
