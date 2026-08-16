## ADDED Requirements

### Requirement: Seeded quick-h5 OAuth client

The backend database migrations MUST insert an OAuth client with `client_id` equal to `quick-h5` that is enabled and usable for Client Basic authentication on login APIs.

#### Scenario: Client row present after migrate

- **WHEN** Flyway migrations are applied on a fresh or upgraded database
- **THEN** a `sys_oauth_client` row exists with `client_id='quick-h5'`, `status='0'` (enabled), and a non-empty `client_secret`

### Requirement: Captcha disabled for first-version H5 client

The seeded `quick-h5` client MUST set `check_captcha` to `'0'` so first-version mobile login can complete without captcha unless a later change opts in.

#### Scenario: Captcha flag off

- **WHEN** reading the seeded `quick-h5` client
- **THEN** `check_captcha` equals `'0'`

### Requirement: Path access pattern

The seeded `quick-h5` client MUST allow API path patterns sufficient for login and `/auth/me` (at minimum covering the paths used by the first-version client).

#### Scenario: Broad or explicit patterns

- **WHEN** the client authenticates with Basic and calls `POST /login`
- **THEN** the backend accepts the client (does not reject solely due to path pattern mismatch)

### Requirement: Credentials documented for development

Development credentials for `quick-h5` MUST be documented for local use (environment sample and/or README) and MUST NOT introduce production secrets into the repository.

#### Scenario: Env sample matches seed

- **WHEN** a developer opens `quick-h5` development env sample
- **THEN** `VITE_OAUTH_CLIENT_ID` is `quick-h5` and the secret matches the development seed value used in migration
