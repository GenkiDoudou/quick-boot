## Purpose

Aligns quick-h5 system administration list and form pages with the practical search fields, card fields, and form validation used on corresponding quick-ui system screens, without import/export parity.

## ADDED Requirements

### Requirement: User list and form practical alignment

The user list SHALL support account keyword search plus status filtering when the API allows. Card meta SHALL expose key fields including department, roles, phone, and email (when present), and MAY include sex. The user form SHALL include email, sex, and remark in addition to existing fields, with required account/nickname/department/roles and mobile/email format checks when filled.

#### Scenario: User search with status

- **WHEN** the user searches by account and selects a status filter
- **THEN** the list results reflect both conditions

#### Scenario: User form validates email format

- **WHEN** the user enters an invalid email and saves
- **THEN** the client blocks submit with a validation toast

### Requirement: Role, dept, config, dict, oauth, file modules alignment

Role, department, parameter, dictionary type/data, OAuth client, file classify, and file pages SHALL each gain practical keyword search and, where applicable, a status or built-in type filter; list cards SHALL show additional key fields via column config when useful; write forms SHALL enforce required main fields before calling APIs.

#### Scenario: Role status filter

- **WHEN** the admin filters roles by status
- **THEN** only matching roles are listed

#### Scenario: Config required fields

- **WHEN** the admin saves a parameter without name, key, or value
- **THEN** the client blocks submit with a required-field toast

#### Scenario: File upload still requires classify

- **WHEN** the admin uploads a file
- **THEN** a classify must be selected before upload proceeds
