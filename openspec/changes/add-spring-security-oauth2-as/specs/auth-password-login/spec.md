## ADDED Requirements

### Requirement: Password login issues user JWT via AS
The system MUST authenticate local username/password for the first-party client and issue a user access token (JWT) through the Authorization Server token issuance path (password extension grant and/or the same TokenGenerator used by that grant). The system MUST expose a JSON facade `POST /auth/login` that returns access token, optional refresh token, token type, and expires-in for the management UI.

#### Scenario: Successful password login
- **WHEN** a caller submits valid username and password via `POST /auth/login` for the first-party client
- **THEN** the system returns HTTP 200 with a Bearer access token whose claims identify a user token (`token_kind` = `user`, `sub` = local user id)

#### Scenario: Invalid credentials
- **WHEN** a caller submits invalid username or password via `POST /auth/login`
- **THEN** the system rejects the login without issuing an access token

### Requirement: Refresh user token
The system MUST allow refreshing a user access token using a previously issued refresh token via `POST /auth/refresh` and/or `POST /oauth2/token` with `grant_type=refresh_token`.

#### Scenario: Successful refresh
- **WHEN** a caller presents a valid refresh token for a user session
- **THEN** the system returns a new access token that remains a user token

### Requirement: Current user endpoint requires user JWT
The system MUST expose `GET /auth/me` that returns the authenticated local user profile and MUST require a valid user access token.

#### Scenario: Authenticated me
- **WHEN** a caller invokes `GET /auth/me` with a valid user Bearer JWT
- **THEN** the system returns the corresponding local user identity

#### Scenario: Client token rejected on me
- **WHEN** a caller invokes `GET /auth/me` with a client (`token_kind` = `client`) Bearer JWT
- **THEN** the system denies access

### Requirement: Password grant restricted to first-party clients
The system MUST NOT allow non-first-party registered clients to obtain tokens using the password grant.

#### Scenario: External client password denied
- **WHEN** a non-first-party client requests `POST /oauth2/token` with `grant_type=password`
- **THEN** the system rejects the token request
