## Purpose

Provides a JSON-configured field renderer for quick-h5 list card meta areas, so pages can adjust labels, grid span, and kv layout via column config instead of hand-written markup, starting with the user management list.

## ADDED Requirements

### Requirement: JSON columns render card meta fields

The H5 client SHALL provide a reusable card-fields renderer that accepts a row object and a columns array. Each column MUST support at least `prop`, `label`, optional `span` (24-grid), optional `kv` (`row` or `stack`), and optional `type` (`text` default). Empty values MUST display a configurable empty placeholder (default em dash).

#### Scenario: Text column with default layout

- **WHEN** a column has `prop` and `label` and the row has a value for that prop
- **THEN** the renderer shows the label and value using the default kv and span behavior

#### Scenario: Span and kv from config

- **WHEN** a column sets `span` to 12 and `kv` to `row`
- **THEN** the field occupies half of the row width and places label and value on one line

#### Scenario: Stack kv for long text

- **WHEN** a column sets `kv` to `stack`
- **THEN** the label appears above the value

### Requirement: Dict and slot column types

The renderer MUST support `type: 'dict'` using caller-provided options to display a dictionary tag, and `type: 'slot'` to render a named slot (default name equals `prop`, override via `slotName`) for custom cell content.

#### Scenario: Dict column

- **WHEN** a column has `type: 'dict'` and `options`
- **THEN** the value is shown as a dictionary tag based on those options

#### Scenario: Slot column

- **WHEN** a column has `type: 'slot'`
- **THEN** the parent can supply a scoped slot to render that cell

### Requirement: Hide column when prop empty

When a column enables `showIfProp`, the renderer MUST omit that column if the row property is null, undefined, or an empty string.

#### Scenario: Email hidden when empty

- **WHEN** a column for `email` has `showIfProp: true` and the row has no email
- **THEN** the email field is not rendered

### Requirement: User list uses column config for meta

The user management list page SHALL drive its card meta fields (department, phone, roles, email) via column configuration through the shared renderer, while keeping search, status badge, and action buttons as page-owned UI with existing permission behavior.

#### Scenario: User card meta from columns

- **WHEN** an admin opens the user list with records that include department and phone
- **THEN** those fields appear according to the page's cardColumns (half-width row kv for dept/phone; full-width stack for roles)

#### Scenario: Actions unchanged

- **WHEN** the user has edit and reset-password permissions
- **THEN** the corresponding action controls remain available as before this change
