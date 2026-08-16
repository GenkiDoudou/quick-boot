## Purpose

Defines H5 list delete behavior for user, role, config, OAuth client, and scheduled job modules, including confirmation, permissions, and forbidden-delete rules.

## ADDED Requirements

### Requirement: List delete with confirmation and remove permission

The H5 client SHALL offer a delete action on user, role, config, OAuth client, and job lists when the user has the corresponding `*:remove` permission. Tapping delete MUST show a confirmation dialog before calling the existing remove API, and on success MUST refresh the list.

#### Scenario: Authorized delete succeeds

- **WHEN** a user with remove permission confirms delete on an eligible row
- **THEN** the client calls the remove API and refreshes the list

#### Scenario: Builtin or protected row blocked

- **WHEN** the user attempts to delete a protected row (super user id 1, admin role id 1, or builtin config)
- **THEN** the client shows a blocking toast and does not call the remove API
