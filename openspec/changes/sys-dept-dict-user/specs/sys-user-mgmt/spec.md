## ADDED Requirements

### Requirement: User management CRUD without exposing password
The system SHALL manage users under `/sys/user` with OauthClient-style page/add/update/remove. List, detail, and export responses MUST NOT include `password`. `userName` MUST be unique. Creating a user MUST set a default encoded password when none is provided. Updating with a blank password MUST keep the existing hash. Deleting or disabling the built-in admin user (`userId=1` or `userName=admin`) MUST be rejected.

#### Scenario: Create user
- **WHEN** an authorized user creates an account with a new `userName`
- **THEN** the user is stored with an encoded default password and the id is returned without password in the response payload used by clients

#### Scenario: Protect admin from delete
- **WHEN** a client attempts to delete the built-in admin user
- **THEN** the operation fails

### Requirement: User status, reset password, and auth roles
The system SHALL provide `POST /sys/user/changeStatus`, `POST /sys/user/resetPwd`, `GET /sys/user/authRole/{userId}`, and `POST /sys/user/authRole` to replace the user's role bindings in full. Reset password MUST store an encoded password. Role changes MUST clear permission caches for that user as existing role services do.

#### Scenario: Reset password
- **WHEN** an authorized admin resets a user's password
- **THEN** the stored credential is replaced with the encoded new password

#### Scenario: Save user roles
- **WHEN** an authorized admin posts a full role id list for a user
- **THEN** `sys_user_role` reflects exactly that set

### Requirement: User sync import export
The system SHALL support sync Excel export/import for users. Export MUST omit password. Import MUST NOT require a password column; new users get the default password; updates with `updateSupport` MUST NOT clear passwords. Uniqueness key is `userName`.

#### Scenario: Import new users
- **WHEN** an xlsx with new `userName` values is imported
- **THEN** users are created with default passwords and success counts are returned

#### Scenario: Export never includes password
- **WHEN** users are exported
- **THEN** the xlsx has no password column or values
