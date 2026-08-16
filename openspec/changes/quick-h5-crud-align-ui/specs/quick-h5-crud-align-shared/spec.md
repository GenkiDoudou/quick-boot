## Purpose

Defines shared H5 CRUD conventions for keyword-plus-filter search and lightweight client-side form validation used across system and monitor list pages.

## ADDED Requirements

### Requirement: Paged lists support extra filters

The shared paged-list helper SHALL allow pages to supply additional filter fields that are merged into each fetch request together with the keyword, and changing filters MUST reload from the first page.

#### Scenario: Status filter reloads list

- **WHEN** the user selects a status filter on a list that supports it
- **THEN** the list reloads from page 1 using that status in the request parameters

### Requirement: Shared form validation helpers

The H5 client SHALL provide shared validation helpers for required fields and optional mobile/email format checks. Failed validation MUST show a non-blocking toast and MUST NOT call the write API.

#### Scenario: Required field blocks submit

- **WHEN** the user submits a form with an empty required field
- **THEN** a toast explains the missing field and no create/update request is sent

#### Scenario: Invalid mobile blocked when provided

- **WHEN** the user enters a non-empty invalid mobile number
- **THEN** submit is blocked with a format error toast
