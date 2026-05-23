# 代码生成

基于 **MyBatis-Plus Generator** + **FreeMarker** 模板，从数据库表生成前后端骨架代码。

| 项 | 值 |
|----|-----|
| Controller | `GenController` |
| 路径 | `/tool/gen` |
| 前端 | `views/tool/gen/` |
| 元数据表 | `gen_table`、`gen_table_column` |

## 接口

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/list` | 已导入生成表列表 |
| GET | `/db/list` | 库中待导入物理表 |
| GET | `/defaults` | 默认包名、作者等 |
| GET | `/{tableId}` | 表配置详情 |
| POST | `/update` | 保存列与生成选项 |
| POST | `/importTable` | 从库导入表 |
| POST | `/createTable` | 在线建表 |
| GET | `/preview/{tableId}` | 预览生成代码 |
| POST | `/remove/{tableId}` | 删除配置 |
| POST | `/synchDb/{tableName}` | 同步表结构 |
| POST | `/batchGenCode` | 批量生成 ZIP |
| POST | `/genCode/{tableName}` | 生成到指定路径 |

## 配置项（`quickboot.gen`）

```yaml
quickboot:
  gen:
    author: # 作者名
    package-name: io.github.genkidoudou.web
    module-name: system
    auto-remove-pre: true
    table-prefix: sys_
    zip-name: quickboot.zip
```

## 使用流程

1. **导入表**：从 `/db/list` 选择物理表 → `importTable`
2. **编辑配置**：设置模块名、包路径、字段 Java 类型、是否插入/列表/查询
3. **预览**：`preview/{tableId}` 检查模板输出
4. **生成**：下载 ZIP 或 `genCode` 写入工程（需谨慎覆盖）

## 模板位置

`quickboot-web/src/main/resources/vm/`（含 `java`、`vue`、`xml` 等 FreeMarker 模板）。

前端列表页模板与 `views/system/config/index.vue` 对齐。

## 相关文档

- [后端开发规范](../development-guide)
- [前端列表页模板](../../frontend/list-page-template)
