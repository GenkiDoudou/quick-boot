## Context

仓库根目录几乎为空；完整 Spring Boot + Vue3 + VitePress 工程在 `bak/`（gitignore）。已确认设计文档：`docs/superpowers/specs/2026-07-25-minimal-migrate-from-bak-design.md`。本 change 将该设计落地为 OpenSpec 可执行产物，并指导从 bak **拷贝后裁剪**出最小可登录基线。

约束：不改 `bak/`；不重写架构/包名；不以整库业务模块为目标；本地须能启动并完成账号密码登录联调。

## Goals / Non-Goals

**Goals:**

- 根目录存在可编译启动的 `quickboot/`（登录链路）
- 根目录存在可 `pnpm dev` 的 `quick-ui/`（登录 + layout + C7 packages）
- 根目录存在可浏览 guide 的 `docs/` VitePress 站
- 提供最小 SQL 与精简配置示例

**Non-Goals:**

- OAuth2 授权服务器/联邦登录、报表、代码生成、监控与系统管理 CRUD 页
- 重构既有模块分层或更换技术栈
- 自动 git commit/push
- 删除或修改 `bak/` 内容

## Decisions

### D1: 整树拷贝后裁剪（相对白名单精确拷贝）

- **选择**：先拷贝三棵工程树（排除构建产物），再按模块/包/页面删除。
- **理由**：登录链路隐式依赖多（user/menu/role/dept/online/logininfor、Sa-Token、验证码等），白名单易漏；裁剪以编译/启动报错驱动修补更可控。
- **备选**：白名单精确拷贝（更干净但联调成本高）；整拷后配置禁用（不符合「只要基本」）。

### D2: 后端模块保留 common/core/system/web，删除 report/tools

- **选择**：父 pom 去掉 report/tools；system 按包级白名单保留登录必需包。
- **理由**：与现有 Maven 结构一致，避免拆模块重写。
- **特例**：`AuthLoginService` 位于 `oauthclient` 包名下但是账号密码登录核心——必须保留其实现及直接依赖；可暂留登录所需类，或抽到 user/auth 后再删 OAuth 管理 CRUD。禁止因目录名含 oauth 误删。

### D3: 前端保留 packages 全量 + 删除业务 views/api

- **选择**：`src/packages` 全部保留；删除 system/monitor/tool/oauth 业务页；保留 `views/dev` 便于组件冒烟。
- **理由**：用户明确要「登录和组件」；业务页不属于最小基线。

### D4: Docs 仅 guide + 保护已有 specs

- **选择**：侧边栏只挂 Guide；删除 dump/深文档；拷贝时保护 `docs/superpowers/specs/2026-07-25-*.md`。
- **理由**：避免覆盖已确认设计；文档站保持可运行即可。

### D5: 最小 SQL 从 bak 抽取，不整份 dump

- **选择**：抽取 user/role/menu/dept 及关联 + 默认管理员；配置示例可关闭验证码便于本地冒烟。
- **理由**：dump 体积大且含无关业务表。

## Risks / Trade-offs

- [登录隐藏依赖（operlog 注解、config 表等）] → 编译/启动失败时最小修补：删调用或保留只读最小依赖
- [动态路由依赖后端菜单为空] → 初始化 SQL 写入首页路由，或前端兜底欢迎页
- [验证码/Redis 阻塞冒烟] → 示例配置默认关闭验证码或文档标明依赖
- [docs 整拷覆盖 specs] → 先备份/保护 specs 路径再拷贝，或拷完写回
- [裁剪后 system 仍偏「胖」] → 接受「能登录优先于极致瘦身」；后续 change 再继续削

## Migration Plan

1. 保护 `docs/superpowers/specs/` 后，从 bak 拷贝三棵树到根（排除 node_modules/target/dist/cache）
2. 后端删模块与包 → 修 pom → `mvn clean install -DskipTests`
3. 前端删业务页/api → 修路由引用 → `pnpm i` + 登录冒烟
4. docs 精简 guide-only → `pnpm i` + 打开 guide
5. 落最小 SQL + 精简 yml
6. 对照成功标准验收

**回滚**：删除根目录 `quickboot/`、`quick-ui/`，并将 `docs/` 恢复为仅保留 specs（或从 bak 再拷）；`bak/` 始终可作权威源。

## Open Questions

- 无阻塞项（设计文档已确认）。实施中若 `AuthLoginService` 抽包成本过高，默认采用「暂留登录所需 oauthclient 类」策略。
