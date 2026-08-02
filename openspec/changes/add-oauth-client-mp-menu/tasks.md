## 1. 数据层与 AS 仓库

- [x] 1.1 新增 `Oauth2RegisteredClient` Entity + `Oauth2RegisteredClientMapper`（映射 `oauth2_registered_client`）
- [x] 1.2 实现字段编解码（集合逗号分隔、settings JSON 对齐 SAS JDBC）
- [x] 1.3 实现 `MybatisRegisteredClientRepository`（findById/findByClientId/save + list/delete）
- [x] 1.4 替换 `AuthorizationServerConfig` 中 JDBC Repository；种子改为经 MP Repository 写入
- [x] 1.5 移除或停用 `OAuthClientQuerySupport` 的 JdbcTemplate 用法

## 2. 管理 API

- [x] 2.1 抽取/重构 `OAuthClientService`：校验（禁外部 password、禁删 quick-ui）、secret 编码、View 映射
- [x] 2.2 `OAuthClientController` 改为走 Service；列表支持可选 `pageNum`/`pageSize`（`records`/`total`）
- [x] 2.3 冒烟：列表含种子客户端；外部配 password 拒绝；删 quick-ui 拒绝；`/auth/login` 仍可用

## 3. 菜单与前端

- [x] 3.1 `/getRouters` 写死系统管理目录 +「客户端管理」→ `system/oauthClient/index`
- [x] 3.2 更新 `quick-ui/src/api/system/oauthClient.js` 对接 `/system/oauth-clients`
- [x] 3.3 精简/恢复 `views/system/oauthClient/index.vue`（grantTypes/redirectUris/scopes/consent）
- [x] 3.4 登录后侧栏可见并完成列表/新增/修改/删除联调

## 4. 收尾

- [x] 4.1 对照 `specs/oauth-client-admin/spec.md` 与设计规格验收清单自检
- [x] 4.2 必要时更新 `quickboot/README.md` 中客户端管理说明一句
