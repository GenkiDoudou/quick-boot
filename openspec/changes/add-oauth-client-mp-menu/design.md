## Context

进程内 Spring Authorization Server 已落地，注册客户端存于 `oauth2_registered_client`，管理 API 为 `/system/oauth-clients`，但实现依赖 `JdbcRegisteredClientRepository` + `JdbcTemplate` 辅助查询；前端 `/getRouters` 为空，历史 `oauthClient` 页面未对接当前 REST。详细产品决策见 `docs/superpowers/specs/2026-07-26-oauth-client-mp-menu-design.md`。

约束：不新建业务表；编解码须与 SAS JDBC 字段格式兼容；password grant 仅 `quick-ui`。

## Goals / Non-Goals

**Goals:**

- MyBatis-Plus Entity/Mapper 映射同一 SAS 表
- `MybatisRegisteredClientRepository` 同时服务 AS 运行时与管理端
- 保留 `/system/oauth-clients`；列表支持可选分页
- `/getRouters` 提供「客户端管理」菜单；前端 API/页面可维护客户端

**Non-Goals:**

- `sys_menu` 持久化菜单
- revealSecret、客户端请求签名
- 独立 `sys_oauth_client` 表或双写
- 修改 JWT/登录主链路语义

## Decisions

1. **唯一数据源 = `oauth2_registered_client`**  
   - 为何：AS 已依赖该表；避免双写。  
   - 备选：独立业务表同步 — 已否决。

2. **`MybatisRegisteredClientRepository` 替换 JDBC 实现**  
   - 为何：管理与运行时同一读写路径。  
   - 备选：管理用 MP、运行时 JDBC — 易不一致。

3. **集合/settings 序列化对齐 SAS JDBC**  
   - grant/methods/uris/scopes：逗号分隔；client_settings/token_settings：SAS JSON。  
   - 为何：与已有种子及可能存在的 JDBC 写入行互通。

4. **API 保持 `/system/oauth-clients`，前端改对接**  
   - 为何：后端 REST 已存在；少改后端契约。

5. **菜单硬编码于 getRouters**  
   - 为何：脚手架无菜单模块；最快可用。  
   - 备选：sys_menu — 延后。

## Risks / Trade-offs

- [编解码与 SAS 不完全一致导致启动后读失败] → 对照 `JdbcRegisteredClientRepository` 字段格式写转换器，并用种子/登录冒烟验证  
- [内存 ticket/菜单硬编码非生产完备] → 文档标明脚手架范围；菜单后续可迁 sys_menu  
- [替换 Repository Bean 影响正在运行的实例] → 本地 H2 重启即可；无迁移脚本  

## Migration Plan

1. 引入 Entity/Mapper/Repository，切换 AS Bean 与种子  
2. 重构管理 Service/Controller 使用 MP  
3. getRouters 菜单 + 前端对接  
4. 回滚：恢复 JDBC Repository Bean（代码回退）；表结构不变  

## Open Questions

- （无）产品决策已在设计规格中确认。  
