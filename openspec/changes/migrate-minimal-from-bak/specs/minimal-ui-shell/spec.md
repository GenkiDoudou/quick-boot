## ADDED Requirements

### Requirement: UI project exists with login and layout shell
The repository root `quick-ui/` MUST start with `pnpm i` and `pnpm dev`, expose a login page, and after successful login enter the application layout shell. A home/welcome view MAY be empty of business CRUD pages.

#### Scenario: Login page loads
- **WHEN** a developer starts the UI dev server and opens the app unauthenticated
- **THEN** the login page is shown

#### Scenario: Post-login shell
- **WHEN** the user completes password login successfully
- **THEN** the UI enters the layout shell without requiring system/monitor/tool management pages

### Requirement: C7 component packages are retained
The UI MUST retain `src/packages` C7 component library sources so components remain available for local development and demonstration. `src/views/dev` demonstration pages MAY be retained to exercise packages.

#### Scenario: Packages directory present
- **WHEN** a developer inspects `quick-ui/src/packages`
- **THEN** the C7 component packages are present

### Requirement: Business management views are excluded
The baseline UI MUST NOT include system, monitor, tool, or oauth business management view trees, nor their corresponding API client modules under `src/api` for those domains.

#### Scenario: System views absent
- **WHEN** a developer inspects `quick-ui/src/views`
- **THEN** `system`, `monitor`, `tool`, and `oauth` business view directories are absent

#### Scenario: Business API clients absent
- **WHEN** a developer inspects `quick-ui/src/api`
- **THEN** system/monitor/tool/oauth/report/export/import API modules are absent while login and menu APIs remain

### Requirement: Auth plumbing remains
The UI MUST retain router, store, permission guard, and request utilities required for token-based login against the backend login APIs.

#### Scenario: Permission guard redirects anonymous users
- **WHEN** an anonymous user navigates to a protected route
- **THEN** the UI redirects to the login page
