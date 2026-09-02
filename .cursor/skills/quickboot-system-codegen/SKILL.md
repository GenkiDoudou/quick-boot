---
name: quickboot-system-codegen
description: >-
  按 quickboot-system 现网分层与 fullstack-simplify 约定，生成或补齐
  Entity、Mapper、ISysXxxService、SysXxxServiceImpl（CrudServiceImpl）、Vo-only Controller，
  以及可选前端 createCrudApi / schema 页 / Flyway 菜单。
  Use when the user asks to 生成增删改查、CRUD、entity/mapper/service/controller、
  参考 sys_config / sys_user / sys_oauth_client、system 模块脚手架、或在 quickboot-system 落表对应代码。
---

# quickboot-system 代码生成规范

> 与 OpenSpec change [`fullstack-simplify`](../../../openspec/changes/fullstack-simplify/) /
> 方案文档 [`docs/docs/guide/fullstack-simplify-plan.md`](../../../docs/docs/guide/fullstack-simplify-plan.md) 对齐。  
> **tool 模块 Freemarker 模板**（权威产出）：`quickboot-module-tool/src/main/resources/vm/quickboot/`。

## 何时使用

用户要在 **`quickboot/quickboot-module-system`**（或 Gen 工具）为某张业务表生成代码，或要求「参考 sys_config / sys_user」时，**必须**遵循本 Skill。

## 参考样例（只读对齐）

| 层 | Tier-1（推荐）SysConfig | Tier-2 SysUser / SysOauthClient |
|----|-------------------------|----------------------------------|
| Entity | `...internal.entity.SysConfig` | 同左分层 |
| Mapper | `...internal.mapper.*Mapper` | 同左 |
| Service | `ISysConfigService` + `SysConfigServiceImpl extends CrudServiceImpl` | 复杂域可暂用 `BaseServiceImpl`，**公开接口仅 Vo** |
| Vo | `...internal.vo.SysXxxVo` | 同左；Controller **禁止** Entity |
| Controller | 薄：鉴权 + 委托 + `R.ok`；`POST /page` | 保留业务 slot，签名仍仅 Vo |
| 前端 | `createCrudApi` + `_schemas` + `useCrudPage` | 复杂页可手写 |

公共基类/包装：

- `io.github.genkidoudou.core.entity.BaseEntity`
- `io.github.genkidoudou.common.mybatisplus.CrudServiceImpl` / `PageRequest` / `PageInfo` / `R`
- 业务失败：`WarningException` + `ErrorCodes`

## 包与命名（强制）

根包：`io.github.genkidoudou.system`（实现落在 `internal.*`）

| 产物 | 包 | 命名 |
|------|----|------|
| Entity | `.internal.entity` | `Sys{Biz}`，表名 `sys_{biz}` |
| Mapper | `.internal.mapper` | `Sys{Biz}Mapper` |
| Service 接口 | `.internal.service` | `ISys{Biz}Service`（**I 前缀**；**无 Entity 返回类型**） |
| Service 实现 | `.internal.service.impl` | `Sys{Biz}ServiceImpl extends CrudServiceImpl<M,E,V>` |
| Vo | `.internal.vo` | `Sys{Biz}Vo`（列表/表单/查询共用） |
| Controller | `.internal.controller` | `Sys{Biz}Controller` |
| 跨模块 Facade | `.api` | View/Query 接口，不暴露 Entity |

## 生成前确认（一次问清）

1. **表名 / 主键**：雪花 `Long` + `IdType.ASSIGN_ID`，还是业务 `String` 主键？
2. **Tier**：简单表走 Tier-1（CrudServiceImpl）；User/Role 等复杂域保留业务 slot。
3. **是否含前端**：`createCrudApi` + `views/_schemas/{module}/{biz}.schema.js` + `index.vue`？
4. **是否含 Flyway 菜单**：ADD-only `V__{table}_menu.sql`（需选 parent_menu_id）？

## 分层规则（fullstack-simplify 后）

### Service

```java
@Service
public class SysXxxServiceImpl
    extends CrudServiceImpl<SysXxxMapper, SysXxx, SysXxxVo>
    implements ISysXxxService {

  @Override
  protected Class<SysXxxVo> voClass() { return SysXxxVo.class; }

  @Override
  public void applyQuery(LambdaQueryWrapper<SysXxx> q, SysXxxVo param) { /* ... */ }

  @Override
  public PageInfo<SysXxxVo> page(PageRequest<SysXxxVo> req) { return crudPage(req); }
}
```

- **公开接口禁止返回 Entity**；登录/鉴权等内部用 Mapper 或包内私有方法。
- Entity 转换仅在 Service 内部 `toVo` / `toEntity`。

### Controller

统一动作路径（与现网 Config 一致）：

```text
POST /{module}/{biz}/page
GET  /{module}/{biz}/{id}
POST /{module}/{biz}/add
POST /{module}/{biz}/update
POST /{module}/{biz}/remove   // body: id 列表
POST /{module}/{biz}/export
```

- 签名仅 `SysXxxVo` / `PageRequest<SysXxxVo>` / `R<>`
- `@SaCheckPermission` + OpenAPI `@Tag` / `@Operation`

### 前端（可选）

- `src/api/{module}/{biz}.js` → `createCrudApi('/{module}/{biz}', { export: true })`
- `src/views/_schemas/{module}/{biz}.schema.js` → columns / formInitial / formRules
- `src/views/{module}/{biz}/index.vue` → `useCrudPage` + schema

### Flyway 菜单（可选）

- 模板输出 `sql/flyway/V__{table}_menu.sql`，人工改成下一 `Vxx__` 版本后放入 `quickboot-app/.../db/migration/`
- **仅 ADD**；用 `WHERE NOT EXISTS` 防重复

## Gen 工具模板清单

| 模板 | 产出 |
|------|------|
| `domain.java.ftl` | Entity |
| `mapper.java.ftl` / `mapper.xml.ftl` | Mapper |
| `service.java.ftl` | `I{Class}Service` |
| `serviceImpl.java.ftl` | `CrudServiceImpl` 实现 |
| `vo.java.ftl` | `internal.vo` |
| `controller.java.ftl` | Vo-only Controller |
| `api.js.ftl` | createCrudApi |
| `schema.js.ftl` | 前端 schema |
| `index.vue.ftl` | schema 驱动页 |
| `menu.sql.ftl` | Flyway 菜单 INSERT |

渲染入口：`GenTemplateRenderer#renderAll`。

## 禁止事项

- Controller / 公开 Service **暴露 Entity**
- 新代码放入废弃的 `io.github.genkidoudou.web.system.*`
- 未要求时大范围重构无关模块
- Flyway **改表 / DROP**（本仓库策略：仅 ADD）

## 自检

- [ ] `ISysXxxService` 无 Entity 签名
- [ ] `ServiceImpl extends CrudServiceImpl`（简单域）
- [ ] Controller 仅 Vo + `POST /page`
- [ ] 前端（若生成）`createCrudApi` + schema 可 `pnpm build`
- [ ] `mvn -pl quickboot-module-system -am -DskipTests compile`
