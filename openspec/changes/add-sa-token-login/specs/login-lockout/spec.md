## ADDED Requirements

### Requirement: Configurable consecutive failure lockout
The system SHALL track consecutive failed password logins per username in Redis. Configuration MUST support `qc.login.max-retry` (default 5) and `qc.login.lock-minutes` (default 10). After `max-retry` failures the system MUST set a lock key with TTL of `lock-minutes` and MUST reject further login attempts for that username until the lock expires or a successful login clears it.

#### Scenario: Reach max retry
- **WHEN** a username accumulates `max-retry` failed credential attempts
- **THEN** the system MUST create `login:lock:{username}` with TTL equal to `lock-minutes` and reject subsequent attempts while locked

#### Scenario: Locked account even with correct password
- **WHEN** `login:lock:{username}` exists
- **THEN** the system MUST reject login before accepting credentials as success, including when the password would otherwise be correct

#### Scenario: Successful login clears counters
- **WHEN** login succeeds
- **THEN** the system MUST delete failure count and lock keys for that username

### Requirement: Failure counting semantics
Only failed credential attempts (unknown user or wrong password) SHALL increment the failure counter. Captcha failures and disabled-account rejections MUST NOT increment the lockout failure counter. The client-facing message for unknown user and wrong password MUST be identical.

#### Scenario: Captcha failure does not lock
- **WHEN** secondary captcha verification fails
- **THEN** the system MUST NOT increment `login:fail:{username}` solely due to that failure

#### Scenario: Disabled account does not lock
- **WHEN** password is correct but account status is disabled
- **THEN** the system MUST NOT increment the failure counter for that attempt
