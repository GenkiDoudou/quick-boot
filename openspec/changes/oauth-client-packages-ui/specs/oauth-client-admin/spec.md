## ADDED Requirements

### Requirement: Paginate OAuth clients without exposing secrets

The system SHALL provide `POST /sys/oauthclient/page` that returns a page of OAuth clients matching optional filters on `clientId` (fuzzy), `clientName` (fuzzy), and `status` (exact). Each record in the page response MUST NOT include a usable `clientSecret` value (null or omitted).

#### Scenario: Page returns records without secret

- **WHEN** an authenticated caller posts a valid `PageRequest` to `/sys/oauthclient/page`
- **THEN** the response contains `records` and `total` for the requested page
- **AND** no record includes a non-blank `clientSecret`

#### Scenario: Filter by status

- **WHEN** the page request `param.status` is set to a concrete status code
- **THEN** only clients with that status are included in `records`

### Requirement: Create OAuth client with auto-generated secret

The system SHALL accept `POST /sys/oauthclient/add` with writable fields excluding `clientSecret`. The server MUST generate a random plaintext `clientSecret`, persist the client, and return a representation that includes the plaintext `clientSecret` exactly once in that create response. Duplicate `clientId` MUST be rejected.

#### Scenario: Successful create returns secret once

- **WHEN** a caller posts a new unique `clientId` and other writable fields to `/sys/oauthclient/add`
- **THEN** the client is stored with a server-generated plaintext secret
- **AND** the response body includes that plaintext `clientSecret`

#### Scenario: Duplicate clientId rejected

- **WHEN** a caller posts `/sys/oauthclient/add` with an existing `clientId`
- **THEN** the system rejects the request without creating a second row

### Requirement: Update OAuth client without changing secret

The system SHALL accept `POST /sys/oauthclient/update` keyed by `clientId` to update writable fields (`clientName`, `apiPathPatterns`, `tokenTimeout`, `checkCaptcha`, `status`, `remark` as applicable). The update MUST NOT change `clientSecret` or `clientId`. After a successful update the cached client for that `clientId` MUST be invalidated.

#### Scenario: Update fields keep secret

- **WHEN** a caller posts `/sys/oauthclient/update` with an existing `clientId` and new `clientName`
- **THEN** the stored `clientName` is updated
- **AND** the stored `clientSecret` is unchanged
- **AND** subsequent `findByClientId` (or Client Basic load) observes the updated fields without stale cache

### Requirement: Remove OAuth client via GET or POST

The system SHALL support deleting a client by `clientId` via both `GET /sys/oauthclient/remove` and `POST /sys/oauthclient/remove`. After removal the client MUST no longer appear in page results and its cache entry MUST be invalidated. POST MAY accept multiple ids for batch delete when the UI requests it.

#### Scenario: Remove by GET

- **WHEN** a caller invokes `GET /sys/oauthclient/remove` with a valid `clientId`
- **THEN** the client is removed (logical or physical per entity rules)
- **AND** it no longer appears in subsequent page queries

#### Scenario: Remove by POST

- **WHEN** a caller posts `/sys/oauthclient/remove` with a valid `clientId`
- **THEN** the client is removed and its cache entry is invalidated

### Requirement: Packages-based admin UI for OAuth clients

The Quick UI admin page for OAuth clients MUST use packages components (`C7JsonTable`, `C7Dialog`, `C7Select`, `C7Switch`, `C7Copy` as applicable) and MUST bind to `/sys/oauthclient` APIs only. The create form MUST NOT require entering `clientSecret`. After a successful create the UI MUST show the returned secret in a dialog with copy support via `C7Copy`. The list MUST display the new entity fields and MUST NOT show grantTypes, scopes, redirectUris, or password-reveal flows.

#### Scenario: List and search with C7JsonTable

- **WHEN** an operator opens the OAuth client admin page
- **THEN** the page loads data through `C7JsonTable` against `POST /sys/oauthclient/page`
- **AND** columns include at least clientId, clientName, apiPathPatterns, tokenTimeout, checkCaptcha, status

#### Scenario: Create shows secret with C7Copy

- **WHEN** an operator submits the create form successfully
- **THEN** a dialog displays the generated `clientId` and `clientSecret`
- **AND** the operator can copy the secret using `C7Copy`

#### Scenario: Edit and delete without legacy flows

- **WHEN** an operator edits or deletes a client from the page
- **THEN** the UI calls `POST /sys/oauthclient/update` or `GET|POST /sys/oauthclient/remove` respectively
- **AND** the page does not call legacy `/system/oauth-clients` or reveal-secret endpoints
