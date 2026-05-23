# 系统配置模块（部门 / 字典 / 参数 / 公告）

## 部门管理

| 项 | 值 |
|----|-----|
| Controller | `SysDeptController` |
| 路径 | `/system/dept` |
| 前端 | `views/system/dept/` |

| 接口 | 说明 |
|------|------|
| GET `/list` | 部门列表（树形） |
| GET `/treeselect` | 下拉树 |
| GET `/{deptId}` | 详情 |
| POST `/update` | 新增/修改 |
| POST `/remove/{deptId}` | 删除 |

用户、数据权限依赖部门树；列表页常用 `handleTree` 与 `C7TreeSelect`。

---

## 字典管理

### 字典类型

| 路径 | `/system/dict/type` |
| 前端 | `views/system/dict/type/index.vue` |

主要接口：`/list`、`/{dictId}`、`/update`、`/remove/{dictId}`、`/export`、`/refresh`、`/import`。

### 字典数据

| 路径 | `/system/dict/data` |
| 前端 | `views/system/dict/data/index.vue` |

- `GET /type/{dictType}`：按类型取字典项（前端 `useDict` 缓存）
- 支持导入导出、模板下载

前端展示：`C7DictTag`、表格列 `columnType: 'tag'`。

---

## 参数配置

| 项 | 值 |
|----|-----|
| Controller | `SysConfigController` |
| 路径 | `/system/config` |
| 前端 | `views/system/config/index.vue`（**列表页模板参考**） |

| 接口 | 说明 |
|------|------|
| GET `/list` | 分页 |
| GET `/{configId}`、`/configKey/{configKey}` | 按 ID 或 key 查询 |
| POST `/create`、`/update`、`/remove` | CRUD |
| POST `/refreshCache` | 刷新参数缓存 |
| POST `/export` | 导出 |

系统内置参数（如代码生成开关）由 Flyway 种子数据写入。

---

## 通知公告

| 项 | 值 |
|----|-----|
| Controller | `SysNoticeController` |
| 路径 | `/system/notice` |
| 前端 | `views/system/notice/index.vue` |

| 接口 | 说明 |
|------|------|
| GET `/list`、`/{noticeId}` | 查询 |
| POST `/create`、`/update`、`/remove` | 维护 |

富文本内容经 **OWASP HTML Sanitizer** 过滤后存储，防 XSS。

## 相关文档

- [前端列表页模板](../../frontend/list-page-template)
- [系统接口](../api/system)
