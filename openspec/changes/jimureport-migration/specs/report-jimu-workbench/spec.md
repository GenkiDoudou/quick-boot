## ADDED Requirements

### Requirement: Report workbench menu entry
系统 SHALL 提供「数据可视化 / 报表工作台」菜单（种子 `menu_id=3001`，权限 `report:jimu:list`），以 InnerLink iframe 打开 JimuReport 列表页。

#### Scenario: Router meta.link for report list
- **WHEN** 已授权管理员请求动态路由且 `qc.jimu.enabled=true`
- **THEN** 报表工作台路由 `component` 为 `InnerLink`，且 `meta.link` 等于 `qc.jimu.base-url` 与菜单 `query`（`/jmreport/list`）的拼接（或已是绝对 URL 的直通结果）

### Requirement: JimuReport token and share security
系统 SHALL 在积木报表路径上排除 Sa-Token 登录拦截，并通过积木 Token 服务与 Header/分享 Filter 完成鉴权与分享访问。

#### Scenario: Sa-Token excludes jmreport paths
- **WHEN** `qc.jimu.enabled=true`
- **THEN** Sa-Token 拦截器排除路径至少包含 `/jmreport/**`、`/jimureport/**`（以及配置中的同类积木路径）

#### Scenario: Token service registered
- **WHEN** 积木自动配置生效
- **THEN** 官方 Token 服务接口由项目实现类提供，可校验登录态或合法分享态

### Requirement: Primary datasource sync
系统 SHALL 在启动后将积木主数据源记录与当前 `spring.datasource` 对齐（可通过配置关闭）。

#### Scenario: Sync on startup
- **WHEN** `qc.jimu.primary-datasource.sync-on-startup=true` 且应用就绪
- **THEN** `JimuPrimaryDataSourceSynchronizer`（已注册为 Spring Bean）执行同步逻辑

### Requirement: Flyway schema menus and demo
系统 SHALL 通过 Flyway 创建积木表结构、菜单与 OAuth `/report/**`，并加载官方演示数据与 2.5.0 增量脚本。

#### Scenario: Migrations applied
- **WHEN** 在空库或未执行积木迁移的环境启动应用
- **THEN** 依次应用 V14（结构+菜单+OAuth）、V15（演示）、V16（2.5 增量），且 Flyway 未因演示 JSON 中的 `${…}` 占位符解析失败

### Requirement: No Jimu AI secrets in repo
仓库与默认配置 SHALL NOT 包含 `jeecg.jmreport.ai` 的 API Key 或等价密钥。

#### Scenario: Config review
- **WHEN** 检查本变更引入的 yml / 示例配置
- **THEN** 不存在可提交的 AI api-key 明文
