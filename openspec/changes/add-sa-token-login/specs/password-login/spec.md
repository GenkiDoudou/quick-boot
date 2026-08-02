## ADDED Requirements

### Requirement: Password login with captcha secondary check
The system SHALL expose `POST /login` accepting JSON `username`, `password`, and optional `uuid`. When an `ImageCaptchaApplication` bean that supports secondary verification is available, the system MUST require a non-blank `uuid` and MUST call secondary verification before credential checks. When that bean is not available, the system MUST skip captcha checks and allow login without `uuid`.

#### Scenario: Captcha enabled and uuid missing
- **WHEN** captcha secondary verification is available AND `uuid` is blank
- **THEN** the system MUST reject the login with an unauthorized-style error indicating captcha is required

#### Scenario: Captcha enabled and uuid invalid
- **WHEN** captcha secondary verification is available AND secondary verification for `uuid` fails
- **THEN** the system MUST reject the login indicating the captcha is expired or invalid

#### Scenario: Captcha disabled
- **WHEN** captcha secondary verification bean is not available AND username/password are valid for an enabled user
- **THEN** the system MUST allow login without `uuid`

### Requirement: User status and password validation
The system SHALL load the user by username, verify the password with `PasswordCodec.matches`, and reject disabled users (`status` not equal to enabled value `0`). Unknown user and wrong password MUST share the same client-facing error message.

#### Scenario: Wrong password
- **WHEN** the username exists but the password does not match
- **THEN** the system MUST respond with a generic credentials error (same wording as unknown user)

#### Scenario: Disabled user
- **WHEN** the username exists, password matches, and `status` is disabled
- **THEN** the system MUST reject login indicating the account is disabled without counting a lockout failure

#### Scenario: Successful credential check
- **WHEN** captcha rules are satisfied, account is not locked, user is enabled, and password matches
- **THEN** the system MUST clear login failure/lock keys and issue a sa-token session

### Requirement: Sa-token issuance and activity renewal
On successful login the system SHALL call sa-token login for the user id and return `data.accessToken` (and MAY return `tokenName`). The system MUST configure sa-token so that absolute `timeout` and `active-timeout` apply; requests that present a valid token MUST renew activity according to sa-token settings. The system SHALL NOT require a separate refresh-token endpoint in this change.

#### Scenario: Login success response
- **WHEN** login succeeds
- **THEN** the response MUST include HTTP/business success with `data.accessToken` non-empty

#### Scenario: Idle expiry
- **WHEN** a token has not been used within `active-timeout`
- **THEN** subsequent authenticated requests MUST fail as unauthenticated until the user logs in again

### Requirement: Frontend contract alignment
Clients MUST call `POST /login` with field `uuid` for captcha id (not `/auth/login` / `captchaId`). The management UI MUST be updated accordingly in this change.

#### Scenario: UI login submit
- **WHEN** the management UI submits password login after captcha success
- **THEN** it MUST POST to `/login` with `uuid` set to the captcha id returned by validate
