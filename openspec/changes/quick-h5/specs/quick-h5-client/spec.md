## ADDED Requirements

### Requirement: Independent uni-app package exists

The repository SHALL provide a standalone package at `quick-h5/` that can be installed with `pnpm install` and exposes scripts to run H5 and WeChat mini-program builds.

#### Scenario: Install and list scripts

- **WHEN** a developer runs `pnpm install` inside `quick-h5/`
- **THEN** dependencies install successfully and package scripts include H5 and mp-weixin develop/build commands

### Requirement: Login with system account

The quick-h5 client SHALL authenticate against the existing backend `POST /login` using OAuth Client Basic for the `quick-h5` client and SHALL store the returned `accessToken` for subsequent requests.

#### Scenario: Successful password login

- **WHEN** a user submits a valid username and password on the login page while the backend is reachable and the `quick-h5` client is enabled
- **THEN** the client receives `R` with `code` 200 and `data.accessToken`, persists the token, and navigates to the home page

#### Scenario: Failed login shows error

- **WHEN** login fails due to invalid credentials or a non-200 business code
- **THEN** the client MUST show an error message and MUST NOT treat the user as logged in

### Requirement: Fetch current user after login

After a successful login, the client MUST call `GET /auth/me` with Bearer token and display user identity on home and/or mine pages.

#### Scenario: Profile loaded

- **WHEN** login succeeds and `/auth/me` returns 200 with user fields
- **THEN** the client stores and displays at least username or nickName

### Requirement: Auth header rules

Unauthenticated requests that require client identity (including `POST /login`) MUST send `Authorization: Basic` using the same credential obfuscation algorithm as `quick-ui`. Authenticated requests MUST send `Authorization: Bearer <accessToken>`.

#### Scenario: Login uses Basic

- **WHEN** the client calls `POST /login`
- **THEN** the request Authorization header is the obfuscated Basic value for `quick-h5` credentials and not a user Bearer token

#### Scenario: Authed API uses Bearer

- **WHEN** the client calls `GET /auth/me` after login
- **THEN** the request Authorization header is `Bearer` plus the stored access token

### Requirement: Session gate and logout

The client MUST redirect unauthenticated users to the login page on launch, and MUST provide a logout action that clears local session and returns to login.

#### Scenario: Cold start without token

- **WHEN** the app launches without a stored access token
- **THEN** the client navigates to the login page

#### Scenario: Logout

- **WHEN** the user chooses logout on the mine page
- **THEN** local token and user fields are cleared and the login page is shown

### Requirement: First-version page scope

The first version MUST include login, home, and mine pages, and MUST NOT include task, category, or quadrant business pages from `bak/h5`.

#### Scenario: No task routes

- **WHEN** inspecting `pages.json` and `src/pages`
- **THEN** there are no registered routes for tasks, categories, or quadrant business features

### Requirement: Dual-end smoke readiness

The package MUST support H5 development server startup and WeChat mini-program build output that can be opened in WeChat DevTools.

#### Scenario: H5 dev server

- **WHEN** a developer runs the H5 dev script with backend available
- **THEN** the login page is reachable in a browser

#### Scenario: mp-weixin output

- **WHEN** a developer runs the mp-weixin dev or build script
- **THEN** an output directory exists that WeChat DevTools can import
