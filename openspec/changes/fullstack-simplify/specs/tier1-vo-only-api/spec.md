## ADDED Requirements

### Requirement: Tier-1 API must not expose Entity types
For Tier-1 domains (Config, DictType, DictData, FileClassify, DeployRecord read-only, Logininfor read-only, OperLog read-only), Controllers and public `ISysXxxService` interfaces SHALL use `SysXxxVo` only. OpenAPI schemas for these endpoints MUST NOT reference Entity class names. Service implementations MAY map Vo↔Entity internally via `toVo`/`toEntity`.

#### Scenario: Config controller is Vo-only
- **WHEN** OpenAPI documents `/sys/config/page`
- **THEN** request and response schemas reference `SysConfigVo` only, not `SysConfig`

### Requirement: Tier-1 migration to CrudServiceImpl
Each Tier-1 `SysXxxServiceImpl` listed in the simplification plan MUST extend `CrudServiceImpl` and implement Vo-only public API. Controllers for Tier-1 MUST be thin (authorization, delegate, `R.ok`) targeting ≤80 lines where no special UI rules apply.

#### Scenario: Dict type service migration
- **WHEN** `SysDictTypeServiceImpl` is migrated
- **THEN** it extends `CrudServiceImpl` and `ISysDictTypeService` methods return `PageInfo<SysDictTypeVo>` without Entity in signatures

### Requirement: Tier-2 retains dual model without Entity API exposure
Tier-2 domains (User, Role, Menu, Dept, OauthClient, File) MAY retain separate Vo shapes for complex forms but MUST NOT expose Entity types in Controller or public Service signatures.

#### Scenario: User API unchanged exposure rule
- **WHEN** user management endpoints are refactored for skeleton only
- **THEN** responses remain `SysUserVo` and never `SysUser` Entity in OpenAPI
