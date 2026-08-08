# 积木报表 / JimuBI 迁移设计

日期：2026-08-08  
状态：已定稿（待实现计划）  
来源：`bak/quickboot/quickboot-report` + `bak/quickboot-web`（jimu catalog / AuthBridge）+ Flyway V3–V5 + `bak/quick-ui` InnerLink  
对齐：`2026-08-08-spring-modulith-maven-layering-design.md` 新域模板

## 1. 背景与目标

现网已完成 Modulith（`common` / `core` / `module-system` / `module-quartz` / `app`）。bak 中「数据可视化」下的两个工作台——**报表工作台**（JimuReport）与 **BI工作台**（JimuBI）——尚未迁入。

**目标**

1. 新建 `quickboot-module-report`，包根 `io.github.genkidoudou.report`，按 `api` / `internal` 边界落地。
2. 迁入与 bak 对齐的完整集成：starter 适配、Token/分享 Filter、主库数据源同步、目录 API、鉴权桥、yml 放行。
3. Flyway 含官方表结构、菜单、OAuth `/report/**`、**官方演示数据**、2.5.0 增量。
4. 前端恢复菜单「报表 / BI」打开方式；InnerLink iframe 带 token；目录 API 可供菜单绑定。

**非目标**

- 不迁 `jeecg.jmreport.ai` 配置与任何 API Key。
- 不做自研非 iframe 报表列表页。
- 不迁慢 SQL「JIMU」标签等周边监控能力。
- 不将积木塞入 `module-system` / `module-tool`。
- 不与在线用户 / 代码生成同一变更交付。

## 2. 已确认决策

| 项 | 选择 |
|----|------|
| 完整度 | 与 bak 对齐的完整集成（非仅菜单壳） |
| Maven / Modulith | 新建 `quickboot-module-report`，包根 `io.github.genkidoudou.report` |
| Flyway 演示数据 | **包含**官方演示（bak V4 等价） |
| 前端 | 菜单积木选项 + InnerLink/token + 目录 API |
| Jimu AI | 不迁 |
| 交付节奏 | 本变更单独做积木迁移 |
| 实现路径 | Modulith 迁码（方案 1） |
| 流程 | 设计文档 → OpenSpec / 实现计划 → 编码 |

## 3. 架构

```text
quickboot-app                         装配 JimuAuthBridge 实现；Sa-Token 排除积木路径
    ├── quickboot-module-system       getRouters：resolveFrameLink（qc.jimu.base-url + query）
    ├── quickboot-module-quartz
    └── quickboot-module-report       ← 新建
              └── quickboot-core → quickboot-common
```

依赖方向：`app → module-* → core → common`。`module-report` **默认不依赖** `module-system` 的 `internal`。

| 项 | 约定 |
|----|------|
| Artifact / 目录 | `quickboot-module-report`；父 POM `<modules>`；`quickboot-app` 增加依赖 |
| Modulith | `@ApplicationModule(displayName = "report")`；开放 `api` |
| 基包注册 | `ApplicationModuleSourceFactory` 追加 `io.github.genkidoudou.report` |
| 第三方 | `jimureport-spring-boot4-starter` **2.5.0**、`jimubi-spring-boot4-starter` **2.5.0**、`jimureport-echarts-starter` **2.3.0**；Jeecg Maven 仓库；必要时保留 `scripts/install-jimureport-maven-deps.ps1`（html2image / tinypinyin） |
| 跨模块 | `JimuAuthBridge` 定义在 `report.api`；**实现放在 `app`**（注入 system 服务），避免 report → system.internal |
| 开关 | `qc.jimu.enabled`；关闭时不注册积木适配 Bean / 可不加载菜单能力说明写在实现计划 |

## 4. 包结构与迁入映射

```text
io.github.genkidoudou.report/
  package-info.java                 @ApplicationModule
  api/
    package-info.java               @NamedInterface("api")
    JimuAuthBridge.java             用户/角色/权限/字典桥接契约
  internal/
    config/                         JimuProperties、JimuReportAutoConfiguration、
                                    JimuDragRedisConfiguration、FixedDragRedisUtil、
                                    JimuPrimaryDataSourceSynchronizer（须注册为 Bean）
    security/                       JimuTokenHeaderBridgeFilter、JimuShareAccessFilter、
                                    JimuShareUriMatcher
    token/                          JimuReportTokenServiceImpl、JimuDragExternalServiceImpl
    catalog/                        JimuCatalogController、Service、Vo
                                    （从 bak web.jimu 迁入，路径仍为 /report/jimu/catalog/*）
```

**从 bak 迁入（行为保留，包名按上表改写）**

| 来源 | 迁入位置 |
|------|----------|
| `bak/.../quickboot-report/**` | `module-report` |
| `bak/.../web/jimu/*`（目录 API） | `module-report/internal/catalog` |
| `bak/.../bridge/JimuAuthBridgeImpl` | `quickboot-app`（实现 `report.api.JimuAuthBridge`） |
| Flyway V3 / V4 / V5 | `quickboot-app` 新版本号（见 §6） |
| `qc.jimu`、Druid wall、放行路径等 yml | `application.yml` / `application-dev.yml` |
| 前端 `api/report/jimu.js`、菜单打开方式、InnerLink/token | `quick-ui`（现网已有部分残留，按 bak 补齐） |

**删除或不迁**

- `jeecg.jmreport.ai` 整段与密钥。
- bak `quickboot-tools` 对 report 的空依赖。
- 慢 SQL / 客户端轨迹等非积木核心能力。

**改造要点**

- `JimuPrimaryDataSourceSynchronizer`：补 `@Component` 或在 AutoConfiguration 中 `@Bean`（bak README 声称会同步，源码曾未注册）。
- `SaTokenWebConfig`：在 `qc.jimu.enabled=true` 时排除 `/jmreport/**`、`/drag/**`、`/jimubi/**`、`/jimureport/**`（及 `JimuProperties.security.excludeSaTokenPaths`）。
- `SysPermissionServiceImpl`（system）：对齐 bak `resolveFrameLink`——`is_frame=1` 时 `meta.link = qc.jimu.base-url + query`（若 path/query 已是绝对 URL 则直接用）；相对 path（如 `jimu-report`）不再要求 path 本身为 `http(s)`。
- 编码与分层对齐 `code_formater.md`（`R<T>`、OpenAPI、布尔 `0`/`1` 等）；积木官方表字段不强制改造成项目实体风格。

## 5. 两个工作台（行为契约）

| menu_id（种子） | 名称 | path | component | query | perms |
|-----------------|------|------|-----------|--------|-------|
| 3000 | 数据可视化 | `/visual` | Layout | — | — |
| 3001 | 报表工作台 | `jimu-report` | InnerLink | `/jmreport/list` | `report:jimu:list` |
| 3002 | BI工作台 | `jimu-bi` | InnerLink | `/drag/list` | `report:jimubi:list` |

运行时：

1. 后端 `getRouters` 产出 `component=InnerLink`，`meta.link = {qc.jimu.base-url}{query}`（例：`http://localhost:9993/jmreport/list`，端口以现网为准）。
2. 前端 `IframeToggle` / InnerLink 加载 iframe，URL **追加登录 `token=`**（对齐 bak）。
3. 积木路径不走 Sa-Token 拦截器；鉴权由 `JimuReportTokenServiceImpl`（Bearer / query token / 分享态）完成。
4. `JimuTokenHeaderBridgeFilter` 将登录态透传到积木识别的 Header/Cookie。

**目录 API（菜单绑定）**

| 接口 | 说明 | 权限建议 |
|------|------|----------|
| `GET /report/jimu/catalog/reports` | 报表目录 | 登录 + 菜单管理相关（对齐 bak，实现时与现网 menu 权限一致） |
| `GET /report/jimu/catalog/bi-pages` | BI 页面目录 | 同上 |

菜单保存：打开方式 `report` / `bi` 时，写入 query 为 `/jmreport/view/{id}` 或 `/drag/view?pageId=`。

**OAuth：** 更新 `quick-ui` 客户端 `api_path_patterns` 包含 `/report/**`（Flyway）。

## 6. Flyway

现网已有 `V1`…`V13`。积木脚本建议：

| 版本 | 内容 | 来源 |
|------|------|------|
| **V14__jimureport.sql** | 官方表结构 + 菜单 3000–3002 + 角色授权 + OAuth `/report/**` + 历史 path 修复 | bak `V3__jimureport.sql` |
| **V15__jimureport_demo.sql** | 官方演示 INSERT（约 9MB） | bak `V4__jimureport_demo.sql`（**本期必迁**） |
| **V16__jimureport_2_5_upgrade.sql** | `chat2bi_table_meta` + 演示数据集 URL 修复 | bak `V5__jimureport_2_5_upgrade.sql` |

约束：

- `spring.flyway.placeholder-replacement: false`（演示 JSON 含 `${…}`，避免被 Flyway 当占位符）。
- Druid wall：`select-where-alway-true-check: false`（MiniDao 常用 `where 1=1`）。
- 本地 H2：官方 DDL 以 MySQL 方言为主；若 H2 不兼容，实现阶段在计划中明确「dev 用 MySQL」或提供 H2 可跑的裁剪策略（设计上优先保证 MySQL/MariaDB 与 bak 一致；H2 为尽力而为）。
- 启动后 `JimuPrimaryDataSourceSynchronizer` 将 `jimu_report_data_source` 与当前 `spring.datasource` 对齐。

## 7. 配置要点

```yaml
qc:
  jimu:
    enabled: true
    base-url: http://localhost:<app-port>   # iframe 绝对地址，须指向本后端
    redis:
      enabled: false   # dev 可按 bak 开 true
    primary-datasource:
      sync-on-startup: true
      name: QuickBoot主库
      code: qc2
    share:
      enabled: true
```

另需在 client-sign / firewall / XSS 等 exclude 列表中放行积木四路径（与 bak 一致）。**禁止**提交 AI api-key。

## 8. 前端

| 项 | 说明 |
|----|------|
| `src/api/report/jimu.js` | 已存在；接通后端后可用 |
| 菜单 `add-or-update` | 恢复打开方式：普通 / 报表 / BI；选资源写 query |
| InnerLink / IframeToggle / AppMain / TagsView | 保证 `meta.link` iframe 登记；URL 带 `token` |
| permission 路由 | `InnerLink` 组件映射；根级 InnerLink 包 Layout（现网若已有则复用） |

无独立「报表业务 Vue 页」；两个工作台均为 iframe 官方 UI。

## 9. 验收标准

1. 管理员可见「数据可视化 → 报表工作台 / BI工作台」，iframe 可打开列表与设计器。
2. 菜单管理可选积木报表 / BI 资源并生成正确 query；侧栏打开对应预览。
3. `GET /report/jimu/catalog/reports` 与 `…/bi-pages` 在登录态可用。
4. 演示数据迁移后库中有官方样例报表/大屏（V15）。
5. `ModularityTests` / `verify()` 通过；`qc.jimu.enabled=false` 时应用仍可启动（积木 Bean 不加载或降级，细节写实现计划）。
6. 仓库无 AI 密钥；Jeecg 依赖可解析（文档说明私服或 install 脚本）。

## 10. 风险与缓解

| 风险 | 缓解 |
|------|------|
| H2 与官方 MySQL DDL 不兼容 | 优先 MySQL 验收；H2 失败则文档标明 |
| Jeecg 私服 / 传递依赖缺失 | parent 配仓库 + install 脚本 |
| 现网菜单外链逻辑与 bak 不一致 | 显式改 `resolveFrameLink`，单测或手工验收 iframe link |
| V15 体积大 | 单独文件；CI/clone 可接受；不拆出本期范围 |
| Modulith 边界被 AuthBridge 破坏 | 实现仅在 app，report 只依赖 bridge 接口 |

## 11. 实现顺序（摘要）

1. 父 POM：版本、仓库、`module-report` 骨架 + app 依赖 + Modulith 基包。  
2. 迁入 report 适配代码 + catalog；app 实现 AuthBridge；注册同步器。  
3. Flyway V14–V16 + yml（含放行与 Druid/Flyway 开关）。  
4. system：`resolveFrameLink`；Sa-Token 排除。  
5. 前端菜单与 iframe token。  
6. 编译、`ModularityTests`、手工打开两个工作台。
