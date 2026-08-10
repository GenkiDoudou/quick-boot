## ADDED Requirements

### Requirement: Template export can apply annotation-driven column constraints

The system SHALL support an explicit export flag `applyTemplateConstraints`. When the flag is `true`, exporting an EasyExcel head class SHALL register a write handler that projects field annotations into Excel column constraints (dropdown and/or input prompts). When the flag is `false` or omitted, export behavior SHALL remain unchanged (no template constraint handler).

#### Scenario: Default export does not apply constraints
- **WHEN** a caller invokes `ExcelUtils.exportExcel` without enabling template constraints (default)
- **THEN** the workbook MUST NOT receive annotation-driven dropdowns or validation prompts from this capability

#### Scenario: Import template export enables constraints
- **WHEN** a caller exports an import template with `applyTemplateConstraints=true` for an `*ImportRow` class
- **THEN** the system MUST register the template constraint write handler for that sheet

### Requirement: Dict fields become Excel dropdowns of labels

For fields annotated with both `@ExcelProperty` and `@ExcelDictFormat`, when template constraints are enabled, the system SHALL add an Excel explicit-list data validation whose options are the dictionary **labels** resolved from inline `dictText` or from `dictType` via `DictLookup`. Dropdown resolution MUST NOT change `@ExcelDictFormat` import/export conversion semantics (`missPolicy`, value↔label).

#### Scenario: Inline dictText dropdown
- **WHEN** a field has `@ExcelDictFormat(dictText = {"0=男", "1=女"})` and template constraints are enabled
- **THEN** the corresponding column MUST expose a dropdown containing labels `男` and `女`

#### Scenario: dictType via Lookup
- **WHEN** a field has non-empty `dictType`, `DictLookup` is available, and template constraints are enabled
- **THEN** the dropdown options MUST be the labels returned for that dict type

#### Scenario: Lookup missing does not fail export
- **WHEN** a field uses `dictType` but `DictLookup` is not registered
- **THEN** the export MUST still succeed, MUST skip that column's dropdown, and MUST log a warning

### Requirement: Validation annotations become input prompts

For `@ExcelProperty` fields with Jakarta Validation annotations among `@NotBlank`, `@NotNull`, `@Pattern`, `@Size`, `@Length`, and `@Email`, when template constraints are enabled, the system SHALL attach Excel input prompts (and MAY attach comments) describing the constraints. Prompt text MUST prefer the annotation `message` when non-blank; otherwise a built-in Chinese default MUST be used. Multiple constraints on one field MUST be concatenated in order: required → format/length → other, separated by semicolons.

#### Scenario: Required field prompt
- **WHEN** a field has `@NotBlank` (or `@NotNull`) and template constraints are enabled
- **THEN** the column MUST show an input prompt indicating the field is required

#### Scenario: Pattern prompt without Excel regex enforcement
- **WHEN** a field has `@Pattern` and template constraints are enabled
- **THEN** the column MUST show a format prompt derived from `message` (and MAY include a shortened regexp hint)
- **AND** the system MUST NOT write the Java regexp as an Excel custom formula that claims full equivalence

#### Scenario: Soft mode allows invalid paste
- **WHEN** a user pastes a value that violates the prompted rules into the template
- **THEN** Excel template constraints from this capability MUST NOT be relied on as hard rejection
- **AND** authoritative validation remains on the import path

### Requirement: Oversized dropdown lists degrade safely

When building an explicit-list dropdown would exceed POI/Excel practical limits, the system SHALL either use a hidden-sheet formula list or degrade to prompt-only for that column, and MUST warn. The system MUST NOT silently truncate dropdown options.

#### Scenario: Too many options
- **WHEN** resolved labels for a dict field exceed the safe explicit-list limit
- **THEN** the export MUST succeed with either a hidden-sheet list or prompt-only fallback
- **AND** a warning MUST be logged
- **AND** options MUST NOT be silently truncated without indication

### Requirement: Import template endpoints opt in

Business `import/template` endpoints that download empty-head import templates via `ExcelUtils` SHALL pass `applyTemplateConstraints=true`. Ordinary business data export endpoints SHALL keep the default (`false`) unless explicitly justified.

#### Scenario: Template endpoint enables flag
- **WHEN** a system module serves an import template download
- **THEN** the call MUST enable template constraints

#### Scenario: Data export stays default
- **WHEN** a system module exports non-empty business rows for download
- **THEN** template constraints MUST remain disabled by default
