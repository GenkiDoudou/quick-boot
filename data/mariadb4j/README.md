# MariaDB4J 本地数据目录

本目录由 **dev-embedded** profile 使用（`qc.embedded-mariadb.data-dir=./data/mariadb4j`）。

## 启动

```bash
cd quickboot
mvn -pl quickboot-web spring-boot:run -Dspring-boot.run.profiles=dev,dev-embedded
```

数据目录实际路径：仓库根 `data/mariadb4j/`（配置为 `../data/mariadb4j`，相对 `quickboot/` 工作目录）。

## 换电脑 / 备份

1. **停止**后端（释放文件锁）
2. **整目录拷贝** `data/mariadb4j/` 到新机器相同路径（仓库根下）
3. 新机器同样使用 `dev,dev-embedded` 启动

> 注意：这是 **MariaDB 数据文件**，不是 MySQL 8 的数据目录，二者不可互换。

## 首次使用（空目录）

- 空目录时 Flyway 会自动执行 `db/migration` 建表（与远程 MySQL 相同脚本）
- 若需要与现网一致的演示数据，可一次性执行仓库根目录：
  `scripts/mariadb4j-import-mysql-dump.ps1`（会将 MySQL dump 转为 MariaDB 兼容后导入）

## 重置

删除本目录下除本 README 外的全部内容后重启（会重新 Flyway 建库）。
