## Purpose

Enable quick-h5 workbench menus and page action buttons to follow the same role-based `sys_menu` authorization model as the PC admin, so visibility is configurable without H5 hardcoding.

## ADDED Requirements

### Requirement: Session carries roles and permissions

The H5 client SHALL persist the current user's `roles` and `permissions` from `GET /auth/me` after login and whenever an existing token is restored at app start. The H5 client SHALL clear `roles` and `permissions` on logout.

#### Scenario: Login stores permissions

- **WHEN** the user successfully logs in
- **THEN** the client MUST store `roles` and `permissions` returned by `/auth/me` for subsequent permission checks

#### Scenario: Token restore refreshes permissions

- **WHEN** the app launches with a stored access token
- **THEN** the client MUST call `/auth/me` and refresh `roles` and `permissions` before relying on button visibility

#### Scenario: Logout clears permissions

- **WHEN** the user logs out
- **THEN** the client MUST clear stored `roles` and `permissions`

### Requirement: Permission check matches PC semantics

The H5 client SHALL provide a permission check that grants access when the user's permissions contain `*:*:*` or contain any of the required permission codes. Pages SHALL hide actions the user is not permitted to perform (not toast-only, not disabled-grey for phase one).

#### Scenario: Super permission sees action

- **WHEN** the user's permissions include `*:*:*`
- **THEN** permission checks for configured action codes MUST succeed

#### Scenario: Missing permission hides add button

- **WHEN** the user lacks `system:user:add`
- **THEN** the user list page MUST NOT show the add entry

#### Scenario: Business guards still apply

- **WHEN** the user has edit/status permission but the target is the protected super admin user or admin role
- **THEN** the existing business prohibition (e.g. cannot disable) MUST still apply

### Requirement: H5 workbench menu from authorized sys_menu

The system SHALL expose an authenticated API that returns workbench groups and items for the current user based on role-authorized `sys_menu` rows. Only directory/menu nodes intended for H5 (menu path starting with `/pages/`) SHALL appear as workbench entries. Button-type (`F`) nodes MUST NOT appear as workbench tiles.

#### Scenario: Role sees only authorized H5 entries

- **WHEN** a user's roles authorize only the H5 user menu entry among H5 pages
- **THEN** the workbench MUST show the user entry and MUST NOT show unauthorized H5 entries (e.g. dept/role)

#### Scenario: Button menus excluded from workbench

- **WHEN** the user has button-type menu permissions (menu type `F`)
- **THEN** those nodes MUST contribute to `/auth/me` permissions only and MUST NOT appear as workbench tiles

#### Scenario: Hidden or disabled menus omitted

- **WHEN** an H5 menu node is hidden or disabled
- **THEN** the workbench API MUST NOT return that node

### Requirement: Workbench renders server menu and navigates by path

The H5 workbench SHALL load menu groups from the H5 workbench API (not from the static mock as the source of truth). Tapping an item with a path SHALL navigate to that uni-app page path.

#### Scenario: Successful workbench load

- **WHEN** the workbench page opens for a logged-in user with authorized H5 menus
- **THEN** the UI MUST render the returned groups and items and navigate using each item's path

#### Scenario: Workbench load failure

- **WHEN** the workbench API fails
- **THEN** the client MUST show an error toast and an empty state and MUST NOT fall back to a full static mock menu that would reveal unauthorized entries

### Requirement: PC routers remain unchanged

The existing PC dynamic route API (`GET /getRouters`) and PC menu management behavior MUST continue to work for non-H5 menu nodes.

#### Scenario: PC getRouters unaffected

- **WHEN** a PC client calls `GET /getRouters`
- **THEN** the response contract for Vue Router assembly MUST remain valid for existing PC menus
