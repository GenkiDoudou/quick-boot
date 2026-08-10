## ADDED Requirements

### Requirement: Maven module graph and naming
The backend Maven reactor SHALL include `quickboot-common`, `quickboot-core`, `quickboot-module-system`, and `quickboot-app` (replacing prior `quickboot-system` and `quickboot-web` artifact roles). Dependency direction MUST be `app → module-* → core → common`. The project MUST NOT introduce a merged `platform` artifact that replaces both `common` and `core` in this change.

#### Scenario: Allowed dependency direction
- **WHEN** Maven module dependencies are declared for the migrated reactor
- **THEN** `quickboot-app` may depend on business modules and shared modules, business modules may depend on `core`, and `core` may depend on `common`

#### Scenario: Shared modules do not depend on business modules
- **WHEN** `quickboot-common` or `quickboot-core` POM dependencies are inspected
- **THEN** neither depends on `quickboot-module-system` or any other `module-*` business artifact

#### Scenario: Common and core remain separate
- **WHEN** the change is applied
- **THEN** both `quickboot-common` and `quickboot-core` remain distinct Maven modules

### Requirement: Shared module responsibility boundary
`quickboot-common` MUST remain a business-agnostic utility/infrastructure module. `quickboot-core` MUST remain the cross-project shared module (for example base entity and project-level abstractions). Neither module SHALL contain business table entities, business mappers, or business domain services that belong to a `module-*` application module.

#### Scenario: No business persistence in shared modules
- **WHEN** shared modules are audited after migration
- **THEN** business domain entities/mappers/services for system features reside in `quickboot-module-system`, not in `common` or `core`

### Requirement: App bootstrap responsibility
`quickboot-app` SHALL host the Spring Boot application entrypoint, component scanning / mapper scanning configuration needed to assemble modules, and Modulith verification tests. It MUST NOT host business REST controllers for system features.

#### Scenario: Entrypoint lives in app
- **WHEN** the application is started
- **THEN** the main Spring Boot application class resides in `quickboot-app`

#### Scenario: No system business controllers in app
- **WHEN** system HTTP controllers are located after migration
- **THEN** they are packaged under `quickboot-module-system`, not under `quickboot-app`

### Requirement: New domain module template documentation
The change SHALL document a reusable template for adding a new business domain module (Maven module name pattern `quickboot-module-<domain>`, package layout `api`/`internal`, allowed dependencies on `core`/`common` and other modules' `api` only).

#### Scenario: Template describes one-to-one mapping
- **WHEN** a developer follows the template to add a new domain
- **THEN** the template specifies one Maven module corresponding to one Modulith Application Module with `api`/`internal` separation
