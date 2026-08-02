## ADDED Requirements

### Requirement: Backend modules form a login-capable baseline
The repository root `quickboot/` Maven multi-module project MUST build with modules `quickboot-common`, `quickboot-core`, `quickboot-system`, and `quickboot-web` only. Modules `quickboot-report` and `quickboot-tools` MUST NOT be present in the parent POM modules list.

#### Scenario: Parent POM excludes removed modules
- **WHEN** a developer inspects `quickboot/pom.xml` modules
- **THEN** only common, core, system, and web modules are listed
- **AND** report and tools modules are absent

#### Scenario: Clean install succeeds
- **WHEN** a developer runs `mvn clean install -DskipTests` in `quickboot/`
- **THEN** the build completes successfully

### Requirement: Password login API remains available
The backend MUST expose password login and session APIs compatible with quick-ui, including login, captcha-config (or equivalent), getInfo, getRouters, and logout. `AuthLoginService` (or its extracted equivalent) MUST remain available for password authentication even if historically packaged under an `oauthclient` namespace.

#### Scenario: Login issues access token
- **WHEN** a valid username and password are submitted to the login endpoint with captcha requirements satisfied or captcha disabled
- **THEN** the response includes an access token usable by the frontend

#### Scenario: Authenticated session info
- **WHEN** a client calls getInfo with a valid token
- **THEN** the system returns the current user profile and permissions payload expected by quick-ui

#### Scenario: Routers for shell navigation
- **WHEN** a client calls getRouters with a valid token
- **THEN** the system returns a router tree that allows entering the layout shell (at least a home/welcome route when business menus are empty)

### Requirement: Non-login backend capabilities are out of baseline
The baseline MUST NOT include OAuth2 authorization-server/client federation controllers, report bridges, code-generation tooling modules, or system/monitor business management APIs as committed baseline features.

#### Scenario: OAuth2 server package absent from web auth
- **WHEN** a developer inspects `quickboot-web` auth sources
- **THEN** `auth.oauth2` server/client federation packages are not part of the baseline tree (except any minimal classes strictly required for password login if not yet extracted)

### Requirement: Minimal SQL and local config are provided
The change MUST provide minimal DDL/seed SQL covering user, role, menu, department and their relations plus a default admin account, and a trimmed local configuration example for MySQL (and Redis if required by Sa-Token persistence). Full historical dump SQL files MUST NOT be required to bootstrap the baseline.

#### Scenario: Seed admin can authenticate
- **WHEN** the minimal SQL is applied to a fresh database and the app starts with the example config
- **THEN** the seeded admin account can complete password login
