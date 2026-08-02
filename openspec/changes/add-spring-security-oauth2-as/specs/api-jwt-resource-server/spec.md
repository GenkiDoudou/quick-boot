## ADDED Requirements

### Requirement: Bearer JWT required on protected APIs
Protected business and auth profile APIs MUST require a valid Bearer JWT issued by this system's Authorization Server (matching configured issuer/JWKS). Requests without a token or with an invalid/expired token MUST be rejected with HTTP 401.

#### Scenario: Missing token
- **WHEN** a caller invokes a protected API without an Authorization header
- **THEN** the system responds with HTTP 401

#### Scenario: Valid user token accepted
- **WHEN** a caller invokes a protected API that allows user tokens with a valid user JWT
- **THEN** the system allows the request to proceed under that user principal

### Requirement: Enforce token_kind where required
Endpoints that require an end-user MUST reject client tokens. Endpoints that require a machine client MUST reject user tokens when so configured.

#### Scenario: User-only endpoint rejects client token
- **WHEN** a user-only endpoint receives a valid client JWT
- **THEN** the system denies the request (HTTP 401 or 403)

### Requirement: Consistent JWT claim contract
User access tokens MUST include `token_kind=user` and `sub` equal to the local user id. Client access tokens MUST include `token_kind=client` and identify the client (via `sub` and/or `client_id` claims as implemented consistently with the design).

#### Scenario: User token claim contract
- **WHEN** a user access token is issued after password or social login
- **THEN** the JWT contains `token_kind` = `user` and `sub` = local user id
