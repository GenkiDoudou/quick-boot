## ADDED Requirements

### Requirement: Excel dict annotation contract
The system SHALL provide a field-level `@ExcelDictFormat` annotation for Excel String fields with attributes `dictType` (default empty), `dictText` (default empty array of `value=label` entries), `separator` (default `,`), and `missPolicy` (default `KEEP`, allowed values `KEEP` | `ERROR` | `EMPTY`). When both `dictType` and `dictText` are present, the system MUST use `dictType` and ignore `dictText`. When `dictType` is empty and `dictText` is non-empty, the system MUST use the inline mapping. Inline entries MUST split on the first `=` only; malformed entries without `=` MUST be skipped without failing the whole import/export.

#### Scenario: Prefer dictType over dictText
- **WHEN** a field declares both a non-empty `dictType` and non-empty `dictText`
- **THEN** conversion uses the system dictionary for that `dictType` and does not use `dictText`

#### Scenario: Inline mapping parses first equals
- **WHEN** `dictText` contains an entry such as `0=男=备用`
- **THEN** the value is `0` and the label is `男=备用`

### Requirement: Bidirectional Excel dict conversion
Through `ExcelUtils` import/export paths that register `ExcelDictConvert`, the system SHALL convert annotated String fields as follows: on export, dictionary values MUST become labels; on import, labels MUST become values. Blank cells MUST remain blank without dictionary lookup. Fields without `@ExcelDictFormat` MUST pass through unchanged. When `separator` is empty, the whole cell MUST be treated as a single token. When `separator` is non-empty, the system MUST split, convert each token, and join with the same separator. On import, if a token does not match as a label but already equals a known value, the system MUST keep that value.

#### Scenario: Export value to label
- **WHEN** exporting a field annotated with inline or system dict whose stored value is `0` mapped to label `男`
- **THEN** the Excel cell contains `男`

#### Scenario: Import label to value
- **WHEN** importing a cell `男` for a field mapped `0=男`
- **THEN** the Java field becomes `0`

#### Scenario: Multi-value round trip
- **WHEN** exporting value `0,1` with separator `,` and mappings `0=男`,`1=女`, then importing the resulting cell
- **THEN** the imported field equals `0,1`

#### Scenario: Import accepts raw value
- **WHEN** importing cell `0` for a mapping where `0` is a known value and `男` is its label
- **THEN** the Java field becomes `0`

#### Scenario: Unannotated string unchanged
- **WHEN** exporting or importing a String field without `@ExcelDictFormat`
- **THEN** the cell/field content is not rewritten by dictionary conversion

### Requirement: Miss policy for unmatched tokens
For each unmatched token (no label/value mapping found, including missing Lookup when `dictType` is used), the system MUST apply `missPolicy`: `KEEP` retains the original token; `EMPTY` drops the token (omit empty segments when joining); `ERROR` fails the operation with `ExcelDataCheckException` (or the project's Excel validation exception) including field name, optional `dictType`, and the original token. If any token uses `ERROR` and is unmatched, the whole conversion MUST fail.

#### Scenario: KEEP unmatched token
- **WHEN** `missPolicy` is `KEEP` and a token has no mapping
- **THEN** that token remains unchanged in the result

#### Scenario: EMPTY unmatched token in multi-value
- **WHEN** `missPolicy` is `EMPTY`, input is `男,未知` with only `男` mapped, separator `,`
- **THEN** the result contains only the converted mapped segment(s) without a dangling empty segment for `未知`

#### Scenario: ERROR unmatched token
- **WHEN** `missPolicy` is `ERROR` and a token has no mapping
- **THEN** conversion throws an Excel validation exception describing the field and original token

### Requirement: DictLookup SPI and system mounting
`quickboot-common` SHALL define a `DictLookup` SPI with `getLabel(dictType, value)` and `getValue(dictType, label)` returning null when unmapped, plus a `DictLookupHolder` for static access. `quickboot-system` SHALL provide a `SysDictLookup` implementation that resolves via cached `listByType` (MUST NOT query the database per cell) and register it into `DictLookupHolder` at application startup via AutoConfiguration. When a field uses `dictType` but the Holder has no Lookup: `ERROR` MUST fail with a clear “dictionary service not ready” style message; `KEEP`/`EMPTY` MUST apply the miss policy without inventing mappings. Inline-only conversion MUST work without a registered Lookup.

#### Scenario: System dict uses cache-backed lookup
- **WHEN** converting a field with `dictType` after system AutoConfiguration has run
- **THEN** labels/values resolve through `SysDictLookup` backed by cached dictionary data

#### Scenario: Inline works without Lookup
- **WHEN** converting a field that only uses `dictText` and no Lookup is registered
- **THEN** conversion still succeeds using the inline map

#### Scenario: Missing Lookup with ERROR
- **WHEN** a field uses `dictType`, Lookup is not registered, and `missPolicy` is `ERROR`
- **THEN** conversion fails indicating the dictionary service is not ready
