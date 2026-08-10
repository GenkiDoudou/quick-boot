## ADDED Requirements

### Requirement: Manage gen table configuration
系统 SHALL 提供代码生成配置管理 API（路径前缀 `/tool/gen`），支持配置分页、库表候选、全局 defaults、详情与保存配置。

#### Scenario: Page gen configs
- **WHEN** 客户端请求生成配置分页列表
- **THEN** 系统返回 `R` 包装的分页 `gen_table` 配置数据

#### Scenario: Save configuration
- **WHEN** 管理员保存某表的生成配置（含列配置）
- **THEN** 系统持久化至 `gen_table` / `gen_table_column` 并在后续预览/生成中生效

### Requirement: Import and create tables
系统 SHALL 支持从数据库导入表结构到生成配置，以及在受控条件下执行建表 SQL。

#### Scenario: Import tables
- **WHEN** 管理员选择库中已有表执行导入
- **THEN** 系统写入对应 `gen_table` / `gen_table_column` 初始配置

#### Scenario: Create table accepts CREATE TABLE only
- **WHEN** 管理员提交合法且仅含 `CREATE TABLE` 的建表 SQL（语句数不超过配置上限）
- **THEN** 系统执行建表并可供后续导入/生成使用

#### Scenario: Illegal create SQL rejected
- **WHEN** 建表 SQL 含非 `CREATE TABLE` 语句、语句数超限或其它非法内容
- **THEN** 系统拒绝执行并不修改库表结构

### Requirement: Preview sync delete and generate
系统 SHALL 支持预览生成结果、同步库表结构、删除配置、批量 Zip 下载与自定义路径写盘。

#### Scenario: Preview code
- **WHEN** 管理员对已配置表发起预览
- **THEN** 系统返回各模板渲染后的代码文本（树表/主子表若未开放则提示未开放且不产生错误生成物）

#### Scenario: Sync columns from DB
- **WHEN** 管理员对已导入表发起同步
- **THEN** 系统按当前库表结构更新列配置（保留合理手工配置策略与 bak 语义一致）

#### Scenario: Download zip
- **WHEN** 管理员批量生成并下载 Zip
- **THEN** 响应为可下载的 Zip；包内路径/分层对齐现网约定（entity/service/controller 与 C7 前端骨架等）

#### Scenario: Write to disk with path guard
- **WHEN** 写盘已启用且目标路径在允许根路径内
- **THEN** 系统写出生成文件；若路径越界（目录穿越）则拒绝写盘

#### Scenario: Delete gen config
- **WHEN** 管理员删除生成配置
- **THEN** 对应 `gen_table` / `gen_table_column` 记录被移除

### Requirement: FreeMarker templates and qc.gen config
系统 SHALL 使用 FreeMarker 模板生成代码，配置前缀为 `qc.gen.*`（如 author、package-name、module-name、zip-file-name、create-table-max-statements、写盘根路径等）。

#### Scenario: Config prefix
- **WHEN** 应用读取代码生成配置
- **THEN** 使用 `qc.gen.*` 键，而非 bak 的 `quickboot.gen.*`

#### Scenario: Templates loaded from module-tool
- **WHEN** 执行预览或生成
- **THEN** 模板自 `quickboot-module-tool` 资源加载并可成功渲染

### Requirement: Gen permissions
代码生成操作 SHALL 受权限控制：`tool:gen:list|import|create|edit|remove|preview|code`（与菜单种子一致）。

#### Scenario: Unauthorized gen denied
- **WHEN** 无相应权限的用户调用受保护的 `/tool/gen` 接口
- **THEN** 系统拒绝该请求
