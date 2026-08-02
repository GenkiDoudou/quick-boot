---
name: quickboot-system-codegen
description: >-
  按 quickboot-system 现网 SysUser / SysOauthClient 分层约定，生成或补齐
  Entity、Mapper、Service、Controller（及可选 DTO/VO、Mapper XML、DDL）。
  Use when the user asks to 生成增删改查、CRUD、entity/mapper/service/controller、
  参考 sys_user / sys_oauth_client、system 模块脚手架、或在 quickboot-system 落表对应代码。
---

# quickboot-system 代码生成规范

## 何时使用

用户要在 **`quickboot/quickboot-system`** 为某张业务表生成后端代码，或要求「参考 sys_user / sys_oauth_client」时，**必须**遵循本 Skill，禁止另起包名/分层习惯。

## 参考样例（只读对齐）

| 层 | SysUser | SysOauthClient |
|----|---------|----------------|
| Entity | `...system.entity.SysUser` | `...system.entity.SysOauthClient` |
| Mapper | `...system.mapper.SysUserMapper` | `...system.mapper.SysOauthClientMapper` |
| Service | `ISysUserService` + `impl.SysUserServiceImpl` | `ISysOauthClientService` + `impl` |
| Mapper XML | **无**（默认不用） | **无** |
| Controller | 现网暂缺；完整 CRUD 参考文末模板 + 前端 API | 前端期望 `/system/oauth-clients` REST |

公共基类/包装：

- `io.github.genkidoudou.core.entity.BaseEntity`（`delFlag` / 审计字段）
- `io.github.genkidoudou.common.api.R` / `PageRequest` / `PageInfo`
- 业务可预期失败：`WarningException` + `ErrorCodes`；严重：`ErrorException`

## 包与命名（强制）

根包：`io.github.genkidoudou.system`

| 产物 | 包 | 命名 |
|------|----|------|
| Entity | `.entity` | `Sys{Biz}`，表名 `sys_{biz}` |
| Mapper | `.mapper` | `Sys{Biz}Mapper`，`@Mapper` + `extends BaseMapper<E>` |
| Service 接口 | `.service` | `ISys{Biz}Service`（**I 前缀**；**不**强制继承 MP `IService`） |
| Service 实现 | `.service.impl` | `Sys{Biz}ServiceImpl` + `extends ServiceImpl<M,E>` + `@Service` |
| Controller | `.controller` | `Sys{Biz}Controller` 或 `{Biz}Controller` |
| DTO/VO/BO | `.dto`（按需） | `{Biz}QueryBo` / `CreateBo` / `UpdateBo` / `{Biz}Vo` |
| Support（跨模块 SPI） | `.support` | 实现 `quickboot-common` 接口，如 `OauthServiceSupportImpl` |
| Mapper XML（仅复杂 SQL） | `resources/mapper/**/*.xml` | 与 `mybatis-plus.mapper-locations` 一致 |

类名、表名、字段：Java 驼峰 ↔ 库下划线；逻辑删除字段用基类 `delFlag`，勿在子实体重复 `@TableLogic`（除非脱离 BaseEntity）。

## 生成前确认（一次问清）

1. **表名 / 主键**：雪花 `Long` + `IdType.ASSIGN_ID`，还是业务 `String` 主键（如 `client_id`）？
2. **只要持久层**（Entity+Mapper+Service）还是 **含管理端 Controller**？
3. **API 风格**（有 Controller 时）：
   - **A. REST 资源**（推荐，对齐 `oauthClient.js`）：`/system/{resources}` + GET/POST/PUT/DELETE
   - **B. Ruoyi 动作路径**（对齐历史用户管理）：`/system/{biz}/list|create|update|remove`，写操作用 POST
4. 是否需要 **缓存**（参考 OauthClient：`@CacheConfig(cacheNames = "sys-{biz}#3600")` + `@Cacheable`）？
5. 是否已有 DDL；若无，是否同步写 `schema-*.sql` / `data-*.sql`（H2+Druid：用 MySQL 方言，`CONSTRAINT ... UNIQUE`，本地可关 wall/merge-sql）？

未确认前不要猜前端路径。

## 分层规则

### 1. Entity

```java
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("sys_xxx")
public class SysXxx extends BaseEntity implements Serializable {
  @Serial
  private static final long serialVersionUID = 1L;

  @TableId(value = "xxx_id", type = IdType.ASSIGN_ID) // 或业务主键 @TableId("client_id")
  private Long xxxId;
  // 业务字段…
  private String status; // 常用：CommonEnums.STATUS_ENABLE = "0"
}
```

- 继承 `BaseEntity`，不要重复审计/`delFlag` 字段。
- 仅映射真实列；与 DDL 不一致时先改 DDL 或标明差异。

### 2. Mapper

```java
@Mapper
public interface SysXxxMapper extends BaseMapper<SysXxx> { }
```

- **默认无 XML、无自定义方法**；复杂联表/报表再加 XML 或 `@Select`。
- 扫描包：`@MapperScan("io.github.genkidoudou.system.mapper")`（勿扫到整个 `system`）。

### 3. Service

接口只声明业务方法；实现类：

```java
@Slf4j
@Service
@RequiredArgsConstructor
public class SysXxxServiceImpl extends ServiceImpl<SysXxxMapper, SysXxx>
    implements ISysXxxService {

  @Override
  public SysXxx findByXxx(String key) {
    if (StrUtil.isBlank(key)) {
      return null;
    }
    return this.getOne(new LambdaQueryWrapper<SysXxx>()
        .eq(SysXxx::getXxx, key), false);
  }
}
```

- 单表条件优先 `LambdaQueryWrapper`，禁止字符串列名散落。
- `getOne(wrapper, false)`：多行时不抛异常（与 SysUser 一致）。
- 写操作需要事务时加 `@Transactional(rollbackFor = Exception.class)`。
- 缓存名建议：`sys-{biz}#秒数`（如 `sys-oauthClient#3600`）；更新/删除后记得 `@CacheEvict`。

### 4. Controller（需要管理端时）

统一：

- `@RestController` + `@RequiredArgsConstructor` + `@RequestMapping("/system/...")`
- 返回 `R<T>` / `R<PageInfo<T>>`
- 参数校验：`@Validated`；分组可用 `AddGroup` / `UpdateGroup`（若模块已有）
- 权限：项目接入 Sa-Token 后用 `@SaCheckPermission("system:xxx:list")` 等；未接入则先不加，并在注释标明 TODO
- 不存在资源：`throw new WarningException(ErrorCodes.Common.INVALID_PARAM, "...")`

**风格 A — REST（oauth-clients）：**

```text
GET    /system/oauth-clients          列表/分页
GET    /system/oauth-clients/{id}     详情
POST   /system/oauth-clients          新增
PUT    /system/oauth-clients/{id}     修改
DELETE /system/oauth-clients/{id}     删除
```

**风格 B — 动作路径（历史用户）：**

```text
GET  /system/user/list
GET  /system/user/{id}
POST /system/user/create
POST /system/user/update
POST /system/user/remove          // body: id 列表
```

分页：入参可用 `PageRequest<QueryBo>` 或 QueryBo 内含 current/size；出参 `PageInfo.from(IPage)`。

### 5. DTO / VO（有 Controller 时建议拆）

| 类型 | 用途 | 注意 |
|------|------|------|
| QueryBo | 列表筛选 | 不含密码等敏感回显 |
| CreateBo / UpdateBo | 写入口 | 校验注解；Update 带主键 |
| Vo / DetailVo | 出参 | Secret 默认脱敏或不返回 |
| Entity | 仅持久化 | Controller **不要** 直接暴露带密钥的 Entity |

跨模块只读视图可放 `quickboot-common`（如 `OauthClientVo`），由 `.support` 组装。

### 6. Mapper XML（可选）

仅当 Lambda 表达不清时新增：

- 路径：`quickboot-system/src/main/resources/mapper/SysXxxMapper.xml`（或仓库统一 `mapper/`）
- `namespace` = Mapper 全限定名
- 简单 CRUD **禁止** 为用 XML 而用 XML

### 7. DDL（可选同步）

- 表字符、主键、`del_flag`/`create_time` 等与 `BaseEntity` 一致。
- H2 `MODE=MySQL` + Druid：避免 `CREATE INDEX IF NOT EXISTS`、`GENERATED BY DEFAULT AS IDENTITY`、`UNIQUE KEY`（StatFilter/h2）；改用 `AUTO_INCREMENT` + `CONSTRAINT uk_x UNIQUE (...)`。
- 种子数据：`spring.sql.init.data-locations`。

## 生成清单（按序落盘）

```text
1. Entity
2. Mapper 接口
3. ISysXxxService + SysXxxServiceImpl（含必要 findBy / page / create / update / remove）
4. （可选）dto 包
5. （可选）Controller
6. （可选）DDL / data SQL
7. （可选）Mapper XML
8. 若需被 common 调用：Support 实现 + 注册
```

每步只改与需求相关的文件；不要顺手重构无关模块。

## 禁止事项

- 不要把新代码放到已废弃的 `io.github.genkidoudou.web.system.*` 包（bak 仅作历史参考）。
- 不要 `@MapperScan("io.github.genkidoudou.system")` 扫到 Service 接口。
- 不要引入 Spring Security 过滤器链；鉴权按现网 Sa-Token / 自研 Client Basic。
- 不要在未要求时生成前端页面或 OpenSpec 全文。

## 自检

- [ ] 包名、类名符合上表
- [ ] Entity 继承 BaseEntity，主键策略与表一致
- [ ] Mapper 仅接口 + BaseMapper；无多余 XML
- [ ] Service 接口 I 前缀；实现继承 ServiceImpl
- [ ] Controller（若有）统一 `R`，路径风格与用户确认一致
- [ ] 敏感字段不出列表 Vo；缓存写路径有失效
- [ ] 能编译：`mvn -pl quickboot-system -am -DskipTests compile`
