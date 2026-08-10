## ADDED Requirements

### Requirement: List Jimu reports for menu binding
系统 SHALL 提供登录态可访问的报表目录接口，供菜单管理绑定积木报表资源。

#### Scenario: List reports
- **WHEN** 已登录用户请求 `GET /report/jimu/catalog/reports`
- **THEN** 返回可用于菜单绑定的报表条目列表（至少含标识与显示名），且未登录请求被拒绝

### Requirement: List JimuBI pages for menu binding
系统 SHALL 提供登录态可访问的 BI 页面目录接口，供菜单管理绑定大屏资源。

#### Scenario: List BI pages
- **WHEN** 已登录用户请求 `GET /report/jimu/catalog/bi-pages`
- **THEN** 返回可用于菜单绑定的 BI 页面条目列表（至少含标识与显示名），且未登录请求被拒绝

### Requirement: Menu form open modes
前端菜单新增/编辑 SHALL 支持打开方式「报表」「BI」，选择目录资源后写入对应 query（报表：`/jmreport/view/{id}`；BI：`/drag/view?pageId=`）。

#### Scenario: Bind report resource to menu query
- **WHEN** 用户在菜单表单选择打开方式为报表并选定一条目录报表后保存
- **THEN** 菜单 `query` 为该报表的预览路径形式 `/jmreport/view/{id}`

#### Scenario: Bind BI resource to menu query
- **WHEN** 用户在菜单表单选择打开方式为 BI 并选定一条目录页面后保存
- **THEN** 菜单 `query` 为该页面的预览路径形式 `/drag/view?pageId=`

### Requirement: OAuth client allows report API
系统 SHALL 确保 `quick-ui` OAuth 客户端的 `api_path_patterns` 包含 `/report/**`，以便目录 API 可被客户端签名策略放行（与 bak 一致）。

#### Scenario: OAuth path pattern includes report
- **WHEN** 查询 Flyway 迁移后的 `quick-ui` 客户端配置
- **THEN** `api_path_patterns` 含 `/report/**`
