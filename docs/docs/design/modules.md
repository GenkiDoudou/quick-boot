# 功能模块（设计）

## 域划分

| 域 | 模块 |
|----|------|
| auth | 登录、路由、OAuth2 |
| system | RBAC、配置、字典、公告 |
| monitor | 日志、在线、定时任务 |
| tool | 代码生成 |

## 依赖关系

```text
auth (Sa-Token)
  └── system (用户/角色/菜单)
  └── monitor (审计，AOP 横切)
  └── tool/gen (依赖已有表结构)
```

## 前端映射

每个菜单 `component` 对应 `quick-ui/src/views/**/index.vue`，详见 [业务页面总览](../frontend/modules/index)。

## 实现索引

[后端模块总览](../backend/modules/index)
