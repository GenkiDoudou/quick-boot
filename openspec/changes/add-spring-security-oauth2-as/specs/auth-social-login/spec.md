## ADDED Requirements

### Requirement: Social login via OAuth2 Client
The system MUST support initiating social login through Spring Security OAuth2 Client using `GET /oauth2/authorization/{registrationId}` and completing the IdP callback. At least one IdP registration MUST be configurable (for example Gitee or GitHub).

#### Scenario: Start social authorization
- **WHEN** an unauthenticated user opens `/oauth2/authorization/{registrationId}` for a configured IdP
- **THEN** the system redirects the user to that IdP authorization endpoint

### Requirement: Bind or auto-create on first social login
When a social identity is not bound to a local user, the system MUST allow the user to either auto-create a local account or bind an existing local account (after verifying that account credentials). The binding MUST be stored uniquely by `(registration_id, external_subject)`.

#### Scenario: Auto-create on first social login
- **WHEN** a social callback yields an unbound external subject and the user confirms auto-create
- **THEN** the system creates a local user, stores the binding, and issues a user JWT

#### Scenario: Bind existing account
- **WHEN** a social callback yields an unbound external subject and the user submits valid existing local credentials to bind
- **THEN** the system stores the binding to that user and issues a user JWT

#### Scenario: Already bound social identity
- **WHEN** a social callback yields an external subject that is already bound
- **THEN** the system authenticates as the bound local user and issues a user JWT without requiring bind choice

#### Scenario: Bind conflict rejected
- **WHEN** a bind attempt targets a social identity that is already bound to another local user
- **THEN** the system rejects the bind and does not overwrite the existing binding

### Requirement: Social completion uses same token issuance
After social authentication resolves to a local user, the system MUST issue the user access token using the same Authorization Server TokenGenerator/signing material as password login user tokens.

#### Scenario: Social token claims match user token rules
- **WHEN** social login completes successfully for a local user
- **THEN** the issued access token is a user token (`token_kind` = `user`, `sub` = that user id)
