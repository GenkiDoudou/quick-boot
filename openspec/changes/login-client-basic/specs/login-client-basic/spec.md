## ADDED Requirements

### Requirement: Validate OAuth client via Basic when no user JWT
The system MUST require `Authorization: Basic` with obfuscated `clientId:clientSecret` for HTTP requests that are not excluded and do not present a valid user Bearer JWT. The system MUST deobfuscate credentials using the agreed reversible algorithm, load the `RegisteredClient`, and verify the secret with `PasswordEncoder.matches`. Failures MUST return a uniform client-invalid error without distinguishing unknown client vs wrong secret. Requests with a valid user Bearer JWT MUST skip this client check. Excluded paths (actuator, h2-console, static assets) MUST skip this check.

#### Scenario: Missing Basic without JWT is rejected
- **WHEN** a client calls `/auth/login` without a user Bearer token and without Basic Authorization
- **THEN** the system rejects the request as client invalid

#### Scenario: Wrong client secret is rejected
- **WHEN** a client sends Basic credentials that deobfuscate to a known clientId but an incorrect secret
- **THEN** the system rejects the request as client invalid

#### Scenario: Valid user Bearer skips Basic
- **WHEN** a client calls a protected API with a valid user Bearer JWT and without Basic
- **THEN** the request is not rejected for missing client credentials

### Requirement: Login uses validated client not hardcoded first-party id
The system MUST accept `/auth/login` with body fields `username` and `password` only. The system MUST issue tokens using the `RegisteredClient` already validated by the Basic filter for that request. The client MUST include the password grant type. The system MUST NOT hardcode `quick-ui` (or `FIRST_PARTY_CLIENT_ID`) when selecting the client for password login token issuance.

#### Scenario: Login with valid Basic and user credentials
- **WHEN** a request presents valid Basic client credentials and a correct username/password
- **THEN** the system returns user tokens associated with that registered client

#### Scenario: Login rejects client without password grant
- **WHEN** Basic credentials validate a client that does not allow password grant
- **THEN** the system rejects the login even if username/password are correct

### Requirement: Frontend injects obfuscated Basic without user token
The management UI MUST store client id/secret via environment variables, obfuscate them with the same algorithm as the backend, and attach `Authorization: Basic` on requests that do not carry a user Bearer token. The login API call body MUST contain only username and password.

#### Scenario: Login request carries Basic from interceptor
- **WHEN** the user submits the login form while logged out
- **THEN** the HTTP request includes obfuscated Basic Authorization and a body with username and password only
