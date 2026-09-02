## ADDED Requirements

### Requirement: Formal menu routes endpoint
The system SHALL expose `GET /api/menu/routes` returning the dynamic menu route tree for the current authenticated user in the same JSON shape previously provided by `/getRouters`. Implementation MUST live in `MenuRouteController` under system module, delegating to `ISysPermissionService.buildRouters()` or equivalent existing logic.

#### Scenario: Authenticated user fetches routes
- **WHEN** logged-in user calls GET `/api/menu/routes`
- **THEN** response is `R<List<Map<String,Object>>>` compatible with quick-ui `permission` store consumption

### Requirement: Deprecated getRouters compatibility
During a compatibility window of at least one release, `GET /getRouters` MAY forward to the same handler as `/api/menu/routes` and MUST be marked deprecated. `ScaffoldCompatController` MUST be removed after forward is in place.

#### Scenario: Legacy getRouters forward
- **WHEN** client calls GET `/getRouters` during compat window
- **THEN** response matches `/api/menu/routes` payload

### Requirement: Frontend menu API update
quick-ui SHALL add `getMenuRoutes()` in `api/menu.js` calling `/api/menu/routes`. `permission.js` and permission store MUST use the new function as primary entry; legacy path MAY be fallback during compat only.

#### Scenario: Permission guard loads routes
- **WHEN** user passes auth guard first navigation
- **THEN** routes are loaded via `getMenuRoutes()` and dynamic routes register successfully
