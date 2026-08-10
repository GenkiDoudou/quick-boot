## Why

bak 已集成积木报表（JimuReport）与 JimuBI 两个工作台，现网 Modulith 化后尚未迁入；运营与开发缺少可嵌入侧栏的报表设计 / BI 设计能力，需按新域模板独立落地，而非塞进 `module-system`。

## What Changes

- 新建 Maven / Modulith 模块 **`quickboot-module-report`**（包根 `io.github.genkidoudou.report`，`api` / `internal`）。
- 迁入 bak 完整积木适配：Token / 分享 Filter、主库数据源同步、Redis 修补、目录 API；`JimuAuthBridge` 实现放在 `app`。
- Flyway：**V14** 表结构 + 菜单 3000–3002 + OAuth `/report/**`；**V15** 官方演示数据；**V16** 2.5.0 增量。
- 配置：`qc.jimu.*`、Sa-Token / client-sign 等积木路径放行、Druid wall 与 Flyway placeholder 关闭；**不迁** Jimu AI 与密钥。
- system：`getRouters` 对齐 bak `resolveFrameLink`（`base-url + query` → InnerLink `meta.link`）。
- 前端：菜单「报表 / BI」打开方式；InnerLink iframe 带 `token`；接通已有 `api/report/jimu.js`。
- **BREAKING（相对 bak）**：包名改为 `io.github.genkidoudou.report.*`；catalog 从 web 迁入 report 模块；AuthBridge 实现改在 app。

权威产品设计：`docs/superpowers/specs/2026-08-08-jimureport-migration-design.md`。

## Capabilities

### New Capabilities

- `maven-module-report`: `quickboot-module-report` 脚手架、Modulith 边界、Jeecg starter 依赖、`app` 装配与基包注册。
- `report-jimu-workbench`: 报表工作台 iframe 入口、积木鉴权桥、主库同步、分享/Token Filter、Sa-Token 放行。
- `report-jimubi-workbench`: BI 工作台 iframe 入口与 JimuBI 适配（含 Drag Redis 修补）。
- `report-jimu-catalog`: 报表 / BI 目录 API，供菜单绑定 query。

### Modified Capabilities

- （无既有主 specs 能力需改写；菜单外链解析与 Sa-Token 排除为现网实现扩展，不另立 system delta。）

## Impact

- 后端：新建 `quickboot-module-report`；父 POM 版本/仓库/`modules`；`quickboot-app` 依赖与 `JimuAuthBridge` 实现；`ApplicationModuleSourceFactory` 追加 `io.github.genkidoudou.report`；`SysPermissionServiceImpl` / `SaTokenWebConfig`；Flyway V14–V16；yml。
- 前端：`quick-ui` 菜单表单积木选项；InnerLink / IframeToggle token；复用 `api/report/jimu.js`。
- 依赖：`jimureport-spring-boot4-starter` 2.5.0、`jimubi-spring-boot4-starter` 2.5.0、`jimureport-echarts-starter` 2.3.0；Jeecg 私服或 install 脚本。
- 库表：积木官方表（约 44+）+ 演示数据；菜单 3000–3002；OAuth `api_path_patterns` 含 `/report/**`。
