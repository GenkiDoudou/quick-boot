## ADDED Requirements

### Requirement: Store OAuth client secrets in plaintext
The system MUST persist `oauth2_registered_client.client_secret` as the plaintext secret value when creating or updating a registered client (and when seeding clients). The system MUST NOT BCrypt-encode client secrets on write. User account passwords MUST continue to be stored and verified using BCrypt via `PasswordEncoder.encode` / BCrypt `matches`.

#### Scenario: Create client stores plaintext secret
- **WHEN** an authenticated administrator creates a client with `clientSecret` equal to a known plaintext value
- **THEN** the row in `oauth2_registered_client` stores that same plaintext value in `client_secret`

#### Scenario: Password login still works with plaintext client secret
- **WHEN** the first-party client `quick-ui` has a plaintext `client_secret` and a user calls `/auth/login` with valid user credentials and that client secret
- **THEN** the Authorization Server issues tokens successfully

### Requirement: Dual-mode PasswordEncoder for users and client secrets
The system MUST provide a `PasswordEncoder` whose `encode` always produces a BCrypt hash, and whose `matches` uses BCrypt when the stored value looks like a BCrypt hash (`$2a$`, `$2b$`, or `$2y$` prefix) and otherwise compares the presented value to the stored value as plaintext equality.

#### Scenario: Match plaintext client secret
- **WHEN** client authentication presents a secret equal to the stored plaintext `client_secret`
- **THEN** `PasswordEncoder.matches` returns true without requiring a BCrypt hash

#### Scenario: Match user password hash
- **WHEN** a user password is verified against a BCrypt `passwordHash`
- **THEN** `PasswordEncoder.matches` uses BCrypt verification and succeeds for the correct password

### Requirement: Reveal client secret with password confirmation
The system MUST expose `POST /system/oauth-clients/{clientId}/reveal-secret` that accepts a body containing the current user's password. The system MUST verify that password against the currently authenticated user's stored password hash. On success the response MUST include `clientId` and plaintext `clientSecret`. List and ordinary get endpoints MUST NOT include `clientSecret`. Wrong password MUST be rejected without returning the secret. Missing client MUST yield not-found.

#### Scenario: Reveal with correct password
- **WHEN** an authenticated user posts the correct account password to reveal a existing client's secret
- **THEN** the response contains that client's `clientId` and plaintext `clientSecret`

#### Scenario: Reveal with wrong password
- **WHEN** an authenticated user posts an incorrect account password to reveal a client's secret
- **THEN** the system rejects the request and does not return `clientSecret`

#### Scenario: List does not include secret
- **WHEN** an authenticated user lists clients via `GET /system/oauth-clients`
- **THEN** each item omits `clientSecret`

### Requirement: Admin UI view secret and field hints
The OAuth client management page MUST provide a view action that prompts for the administrator password, calls the reveal API, and displays Client ID and Client Secret for copying. The create/edit form MUST show a hint control (eye icon with tooltip) before each field explaining Client ID, Client Secret, grant types, redirect URIs, scopes, and consent.

#### Scenario: View secret after password prompt
- **WHEN** the user chooses 查看 on a row, enters the correct password, and confirms
- **THEN** the UI displays the client id and plaintext secret returned by the reveal API

#### Scenario: Form fields show hints
- **WHEN** the user opens the create or edit client dialog
- **THEN** each listed form field has an adjacent hint describing its meaning
