## ADDED Requirements

### Requirement: Authorization Server standard endpoints
The system MUST run Spring Authorization Server in-process and expose token/authorize/JWKS endpoints required for OAuth2 clients to obtain tokens, including at least `POST /oauth2/token`, `GET /oauth2/authorize`, and `GET /oauth2/jwks` (or equivalent JWKS URL documented for the deployment).

#### Scenario: JWKS available
- **WHEN** a client requests the JWKS endpoint
- **THEN** the system returns public keys usable to validate issued JWTs

### Requirement: Authorization code for third-party apps
The system MUST support the authorization code grant for registered third-party clients, including redirect URI validation and issuance of user access tokens after user authentication and consent (consent MAY be skipped only for explicitly configured first-party clients).

#### Scenario: Third-party obtains user token via code
- **WHEN** a registered third-party client completes authorization code exchange with a valid code and credentials
- **THEN** the system returns a user access token for the authorizing local user

#### Scenario: Invalid redirect URI rejected
- **WHEN** an authorize request uses a redirect_uri not registered for that client
- **THEN** the system rejects the authorization request

### Requirement: Registered client management
The system MUST persist OAuth2 registered clients and MUST provide management APIs to create/update/list clients used by the Authorization Server. Seed data MUST include a first-party client suitable for the management UI (for example `quick-ui`) that is allowed to use password grant.

#### Scenario: Seed first-party client
- **WHEN** the application starts with empty client store in a fresh database
- **THEN** the first-party management client exists and is allowed password and refresh grants

#### Scenario: External client cannot enable password
- **WHEN** an administrator creates or updates a non-first-party client
- **THEN** the system rejects configurations that enable password grant for that client

### Requirement: Optional client credentials tokens
The system MAY issue client credentials tokens for machine clients. When issued, such tokens MUST be marked as client tokens (`token_kind` = `client`) and MUST NOT satisfy endpoints that require an end-user principal.

#### Scenario: Client credentials token kind
- **WHEN** a confidential machine client successfully requests `grant_type=client_credentials`
- **THEN** the access token is a client token (`token_kind` = `client`)
