## ADDED Requirements

### Requirement: MyBatis-Plus persistence for registered clients
The system MUST persist OAuth2 registered clients in the existing `oauth2_registered_client` table using MyBatis-Plus, and MUST expose a `RegisteredClientRepository` implementation backed by that mapper so the Authorization Server and admin APIs share one data source.

#### Scenario: AS loads first-party client after startup
- **WHEN** the application starts with an empty client table and seed runs
- **THEN** client `quick-ui` is readable via `RegisteredClientRepository.findByClientId("quick-ui")`

#### Scenario: Admin list uses same rows as AS
- **WHEN** an administrator lists clients via the admin API
- **THEN** the returned clients match rows in `oauth2_registered_client` used by the Authorization Server

### Requirement: Admin REST for OAuth clients
The system MUST provide authenticated management APIs under `/system/oauth-clients` to create, update, list, get, and delete registered clients. Responses MUST NOT include `clientSecret`. Non-first-party clients MUST NOT be allowed to enable password grant. The first-party client `quick-ui` MUST NOT be deletable.

#### Scenario: Create external client with authorization_code
- **WHEN** an authenticated user creates a client with grant types that exclude password
- **THEN** the client is stored and returned without a secret field

#### Scenario: Reject password grant for non-first-party
- **WHEN** an authenticated user creates or updates a non-`quick-ui` client including password grant
- **THEN** the system rejects the request with a client error

#### Scenario: Reject delete of first-party client
- **WHEN** an authenticated user attempts to delete client `quick-ui`
- **THEN** the system rejects the request and the client remains

### Requirement: Scaffold menu for client management UI
The system MUST return a RuoYi-shaped dynamic route tree from `GET /getRouters` that includes a menu entry titled 客户端管理 whose component resolves to `system/oauthClient/index`, so the management UI can be opened after login.

#### Scenario: Login then open client management menu
- **WHEN** an authenticated user requests `/getRouters`
- **THEN** the response includes a route usable by the SPA to load the OAuth client management page

### Requirement: Frontend maintenance against REST API
The management UI MUST call `/system/oauth-clients` for list/create/update/delete and MUST present fields aligned with the admin view model (clientId, grantTypes, redirectUris, scopes, requireAuthorizationConsent), without requiring legacy-only fields such as signVerify.

#### Scenario: Page lists clients after login
- **WHEN** the user opens the 客户端管理 page with a valid Bearer token
- **THEN** the table loads data from `GET /system/oauth-clients`
