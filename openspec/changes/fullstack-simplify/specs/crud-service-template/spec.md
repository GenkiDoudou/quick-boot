## ADDED Requirements

### Requirement: CrudServiceImpl template in common
The system SHALL provide `CrudServiceImpl<M, T, V>` in `quickboot-common` extending `BaseServiceImpl`, with template methods for page query, get by id, save, update, remove, and export where applicable. Subclasses MUST implement query assembly via `applyQuery(LambdaQueryWrapper<T>, V query)`. Entity type `T` MUST remain internal to the service implementation and MUST NOT appear in public `ISysXxxService` method signatures for Tier-1 domains.

#### Scenario: Tier-1 service uses CrudServiceImpl
- **WHEN** `SysConfigServiceImpl` extends `CrudServiceImpl` and implements `ISysConfigService`
- **THEN** page/add/update/remove delegate to template methods with Config-specific `applyQuery` only

### Requirement: Integration test base
The system SHALL provide `QuickbootIntegrationTestBase` in `quickboot-app` test sources with Spring Boot test context bootstrap sufficient for CRUD smoke tests. At least one integration test (`SysConfigCrudIT`) MUST verify page and add against Tier-1 Vo-only API.

#### Scenario: Config CRUD integration test passes
- **WHEN** `SysConfigCrudIT` runs against embedded or test profile datasource
- **THEN** POST `/sys/config/page` and POST `/sys/config/add` return success without exposing Entity types in JSON
