## Purpose

Defines the H5 home tab behavior for real, permission-aware shortcuts and a personal settings page, while keeping message and todo sections as non-functional mock shells.

## ADDED Requirements

### Requirement: Home shows navigable shortcuts from API

The home tab SHALL load final shortcuts from the home-shortcuts GET API on show, render at most 8 entries, and navigate to each item's path when present. Load failure MUST show an empty shortcut area with an error toast and MUST NOT fall back to fake mock shortcut entries. Message and todo sections MAY remain mock and MUST indicate pending integration when tapped.

#### Scenario: Tap shortcut with path

- **WHEN** the user taps a shortcut that includes a page path
- **THEN** the client navigates to that path

#### Scenario: API failure does not show fake shortcuts

- **WHEN** the home-shortcuts request fails
- **THEN** the shortcut grid is empty (or error state) and mock shortcut data is not used as a fallback

### Requirement: User can edit personal shortcuts

The home shortcuts section SHALL provide an edit entry that opens a settings page. The settings page SHALL load candidates and the current selection, allow choosing and ordering at most 8 items, save via the POST save API, and support restore-default by posting an empty menu id list. After a successful save, returning to home SHALL show the updated shortcuts.

#### Scenario: Save from settings

- **WHEN** the user selects up to 8 candidates and saves
- **THEN** the client calls the save API and navigates back so home refreshes the grid

#### Scenario: Restore default from settings

- **WHEN** the user chooses restore default
- **THEN** the client posts an empty menu id list and subsequent home load shows default resolution
