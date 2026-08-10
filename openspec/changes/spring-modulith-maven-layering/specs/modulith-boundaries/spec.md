## ADDED Requirements

### Requirement: System module exposes api and internal packages
The `quickboot-module-system` Maven module SHALL organize code so that cross-module-consumable types live under an `api` package tree and module-private types (controllers, persistence, service implementations, entities) live under an `internal` package tree. The module MUST be declared as a Spring Modulith Application Module that opens only the `api` package tree to other application modules.

#### Scenario: Api package is the published surface
- **WHEN** another future application module needs system capabilities
- **THEN** it MUST depend only on types from the system `api` package tree and MUST NOT compile against system `internal` types

#### Scenario: Controllers stay inside the system module
- **WHEN** HTTP endpoints for system features are registered
- **THEN** their controller classes reside in the system module `internal` package tree, not in the app bootstrap module

### Requirement: Modulith structure verification
The application bootstrap module SHALL provide an automated test that loads Application Modules from the application and calls structural verification (equivalent to `ApplicationModules.verify()`). The verification MUST fail the build when illegal cross-module dependencies are detected.

#### Scenario: Verify passes on the layered target
- **WHEN** the Modulith verification test runs against the post-migration module layout
- **THEN** the test passes with no illegal dependencies between application modules

#### Scenario: Illegal internal dependency fails verification
- **WHEN** a dependent module references a type from another module's `internal` package tree
- **THEN** Modulith verification fails

### Requirement: Initial system api facades
The system module `api` package tree SHALL expose read-oriented facades for user lookup and dictionary lookup that delegate to existing internal services without changing HTTP API contracts. First-phase facades MUST NOT require moving all CRUD services into `api`.

#### Scenario: User lookup facade available
- **WHEN** a caller inside the monolith injects the system user lookup api
- **THEN** it can resolve user information by the same identifiers supported by the existing internal user query capabilities

#### Scenario: Dictionary lookup facade available
- **WHEN** a caller inside the monolith injects the system dictionary lookup api
- **THEN** it can resolve dictionary labels/values consistently with the existing dictionary lookup behavior used by Excel/dict features
