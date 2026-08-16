## Context

See `proposal.md` for motivation. Product decisions and field/storage contracts are fixed in `docs/superpowers/specs/2026-08-14-lite-frontend-rum-design.md`. Static UX references: `docs/demo/lite-rum-chain-a-unified.html`, `docs/demo/lite-rum-chain-e.html`.

This change spans Web SDK, ingest/projection, backend instrumentation, and `quick-ui` console pages.

## Goals / Non-Goals

**Goals:**

- End-to-end `traceId` from browser (or API/job entry) through access/SQL/exception into `trace_index`/`trace_span`.
- Ship A′ + E consoles for developers.
- Keep optional domain tables; consoles read only index/span.

**Non-Goals:**

- Health-score / PV ops dashboard, ISP geo dashboards, H5 SDK, D′ Issue (optional follow-up task group, not blocking ①).
- Replacing existing slow-SQL UI; may project from it later.

## Decisions

1. **Storage: `trace_index` + `trace_span` as console source of truth**  
   - Rationale: A′/E need fast list + waterfall without multi-table joins.  
   - Alternative: query-time JOIN of rum/access/sql — rejected for latency and coupling.

2. **Dual-write/project from sources into span**  
   - Rationale: preserves optional raw `rum_event` while unifying UI model.  
   - Alternative: span-only — acceptable later; ① may write rum_event + project.

3. **Header name: `X-Trace-Id` (configurable)**  
   - Rationale: simple, matches design doc; align with any existing request-id if present via adapter.  
   - Alternative: W3C `traceparent` only — defer; can map later.

4. **Module placement: prefer extending `quickboot-module-monitor` + `quick-ui` monitor routes**  
   - Rationale: monitoring cohesion; follow `code_formater.md` layering.  
   - Alternative: brand-new Maven module — only if monitor module boundaries block ingest.

5. **Console stack: Vue pages in `quick-ui`, UX aligned to dark dense prototypes**  
   - Rationale: matches chosen A′/E demos; Element Plus + chart/CSS waterfall acceptable.

6. **D′ Issue deferred as optional ①b**  
   - Rationale: A′+E already close the primary loop; Issue is acceleration, not blocker.

## Risks / Trade-offs

- [Missing traceId on SQL] → Mitigation: document Filter order; interceptor reads MDC; tests for absent id skip span.  
- [Projection lag if async] → Mitigation: ① prefer sync project on write path; async compensation later.  
- [Volume/TTL] → Mitigation: 7–14 day TTL; truncate SQL in attrs.  
- [Header conflict with existing request id] → Mitigation: accept either header; normalize to `trace_id` column.

## Migration Plan

1. Add DDL for `trace_index` / `trace_span` (and optional `rum_event`).  
2. Ship ingest + projection + backend Filter.  
3. Ship `quick-ui` A′/E behind monitor menu.  
4. Enable SDK on target web app.  
5. Rollback: disable menu + SDK; tables can remain unused.

## Open Questions

- Exact Maven package path if monitor module naming differs — resolve during tasks by reading current `module-monitor` layout.  
- Whether existing slow-SQL table already has a correlation column to map — probe in backend tasks; if absent, add `trace_id` nullable.
