## Context

See `proposal.md` for motivation. Product design: `docs/superpowers/specs/2026-08-16-quick-h5-json-card-list-design.md`.

H5 already has global grid/kv utilities (`qb-row`, `qb-col-*`, `qb-kv`) and `QbListCard`. User list meta is still hand-written markup.

## Goals / Non-Goals

**Goals:**

- Ship `QbJsonCardFields` driven by column JSON
- Wire user list as the first consumer without changing search/actions

**Non-Goals:**

- Porting C7 searchColumns / listFunction / import-export
- Migrating all ops-suite pages in this change

## Decisions

### 1. Fields-only component, not a full page shell

- **Choice:** `QbJsonCardFields` renders columns inside existing `QbListCard` `#meta`
- **Why:** Matches current card UX; smaller blast radius
- **Alternative:** `QbJsonCardList` owning search+list — deferred

### 2. Span via CSS classes, not inline width

- **Choice:** Map `span` to `qb-col-6|8|12|16|24` (fallback nearest / default 12)
- **Why:** Reuses approved utilities; keeps markup on `view`
- **Alternative:** Dynamic style width — rejected for consistency with user request for class-based grid

### 3. Dict options passed in, not auto useDict inside component (phase one)

- **Choice:** `type: 'dict'` uses `options` from parent; render with `QbDictTag`
- **Why:** Avoid coupling composable lifecycle inside list cell; parent already has dicts when needed
- **Alternative:** `dictType` + internal useDict — later

### 4. `showIfProp` for empty email-like fields

- **Choice:** When `showIfProp: true`, skip column if row[prop] is null/empty string
- **Why:** Covers current email behavior without per-page filters
- **Alternative:** Function `show(row)` — deferred

## Risks / Trade-offs

- [Unsupported span values] → Clamp to supported set or default 12
- [Slot naming collision] → Document `slotName` default = `prop`
- [Visual drift vs hand-written meta] → Match current user columns exactly in sample config

## Migration Plan

1. Add component (unused until page switches)
2. Switch user `index.vue` meta to columns
3. Rollback: revert user page to previous template; leave component unused

## Open Questions

- None for phase one
