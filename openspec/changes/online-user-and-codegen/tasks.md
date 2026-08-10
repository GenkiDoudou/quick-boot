## 1. 数据与权限（Flyway）

- [x] 1.1 新增 Flyway（下一可用 `V*`）：监控下「在线用户」菜单 + 按钮权限（`monitor:online:list|forceLogout`）与管理员授权；菜单 ID 避开已占用段
- [x] 1.2 同迁移或后续迁移：`gen_table` / `gen_table_column`（自 bak DDL 适配）+ `tool/gen` 菜单按钮（`tool:gen:*`）与管理员授权

## 2. 在线用户（module-system）

- [x] 2.1 实现 `OnlineSessionRecorder`：登录成功后写入 Token-Session 展示字段；挂钩登录成功路径（`LoginHelper.loginByDevice` 之后）
- [x] 2.2 实现 online service/controller：`GET /monitor/online/list`、`POST /monitor/online/forceLogout`；`@IgnoreLogger`；权限校验；token 扫描/解析对齐 bak
- [x] 2.3 列表分页：支持 C7 `{current,size,param}` 与扁平 query 映射（对齐 job-log）
- [x] 2.4 前端：`api/monitor/online.js` + `views/monitor/online`；对齐现网 monitor 交互与权限指令

## 3. Maven / Modulith：module-tool

- [x] 3.1 新建 `quickboot/quickboot-module-tool`（POM 依赖 `quickboot-core`；不依赖 `module-system`）
- [x] 3.2 父 POM 注册模块；`quickboot-app` 增加依赖；`package-info`（`@ApplicationModule` / `@NamedInterface("api")`）；`ApplicationModuleSourceFactory` 追加 `io.github.genkidoudou.tool`
- [x] 3.3 确认组件扫描 / MapperScan 覆盖 `io.github.genkidoudou.tool`

## 4. 代码生成后端

- [x] 4.1 迁入 entity/mapper/dto/service/controller（包改 `io.github.genkidoudou.tool.internal`）；路径 `/tool/gen`；`R<T>` + springdoc + 权限字
- [x] 4.2 迁入 FreeMarker 模板与渲染/库表内省/Zip/写盘支持类；配置绑定 `qc.gen.*`；写盘路径防穿越；建表 SQL 仅 `CREATE TABLE` + 语句数上限
- [x] 4.3 调整 ftl 生成物包路径与分层，对齐现网 `SysUser` / C7 前端骨架约定
- [x] 4.4 覆盖 bak 能力：配置分页/候选/defaults/详情/保存、导入、建表、预览、删除、同步、Zip、写盘

## 5. 代码生成前端

- [x] 5.1 `quick-ui/src/api/tool/gen.js`（或等价路径）对接 `/tool/gen`
- [x] 5.2 迁并对齐 `views/tool/gen`（index / edit + 导入 / 建表 / 预览弹窗）；权限指令与分页约定

## 6. 验证

- [x] 6.1 `mvn -pl quickboot-module-tool,quickboot-module-system,quickboot-app -am test`（或等价）编译 + Modulith `verify()` 通过
- [ ] 6.2 冒烟在线：登录后列表可见；筛选；强退后旧 token 401；无权限拒绝
- [ ] 6.3 冒烟生成：导入 → 编辑 → 预览 → 同步 → Zip/写盘 → 删除；非法建表被拒；菜单权限可用
