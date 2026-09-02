## ADDED Requirements

### Requirement: Remove orphan frontend API modules
quick-ui MUST remove or relocate API modules with no quickboot backend implementation: `src/api/knowledge/*`, `src/api/ai/*`, `src/api/workflow/*` (14 files total). Removal MUST NOT break build; any future domain MUST regenerate via codegen rather than restoring deleted stubs in place.

#### Scenario: Build without orphan APIs
- **WHEN** `pnpm build` runs after cleanup
- **THEN** no import references deleted knowledge/ai/workflow API paths

### Requirement: ruoyi.js legacy shrink
The project SHALL migrate still-needed helpers from `utils/ruoyi.js` to focused modules (`utils/tree.js`, `utils/route.js`, `utils/formatTime.js`) and remove Options-API-only `resetForm`, global `parseTime`/`selectDictLabel` mounts from `main.js`, and unused exports.

#### Scenario: No global parseTime mount
- **WHEN** application bootstraps after cleanup
- **THEN** `app.config.globalProperties.parseTime` is not registered

### Requirement: Dev E2E pages replaced by Vitest
The 18 `views/dev/C7*E2E.vue` manual pages MUST be deleted after equivalent component tests exist under `src/packages/__tests__/` or similar, covering critical C7JsonTable/C7Dialog behaviors.

#### Scenario: C7JsonTable test exists before E2E removal
- **WHEN** dev E2E vue files are removed
- **THEN** at least one Vitest spec asserts C7JsonTable pagination or column slot rendering

### Requirement: Unused dependencies removed
quick-ui MUST remove `lodash` from dependencies if source has zero imports after migration.

#### Scenario: Package.json lodash removed
- **WHEN** dependency audit runs post-cleanup
- **THEN** `lodash` is not listed in quick-ui package.json dependencies
