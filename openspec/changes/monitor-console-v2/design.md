## Context

Canonical product design: `docs/superpowers/specs/2026-08-15-monitor-console-v2-design.md`.  
Current stack: Lite Trace (`sys_trace_index` / `sys_trace_span` / `sys_rum_event`) already powers「请求链路」; overview and clientTrack are retired or deleted. Oper/login live in system module (Facade); slow SQL in monitor.

See proposal.md for motivation.

## Goals / Non-Goals

**Goals:**

- One change delivers: overview removal, request-trace UX, user-behavior timeline, log hub.
- Prefer reuse of existing Facades and Lite RUM tables; no new dual-track behavior store.

**Non-Goals:**

- Perfect cross-source SQL UNION paging; BI dashboards; restoring `sys_client_track`; ingesting app file logs into the hub.

## Decisions

### D1 — Single Flyway migration batch (V27+)

- Disable overview menus (2169/2170); add `sys_rum_event.uin` + indexes; insert user-behavior and log-hub menus + admin role grants.
- **Why:** Matches prior V25/V26 menu style; one restart applies IA + schema.
- **Alt:** Separate migrations per feature — rejected for same-iteration delivery.

### D2 — Overview: hard delete code, soft-disable menu

- Remove `internal/overview` and FE overview assets; menu status disabled for safety if component already gone.
- **Why:** BI replaces dashboard; dead code must not compile-wire.
- **Alt:** Keep API stub — rejected (user chose full delete).

### D3 — Trace filters on `started_at` + `ok_flag`

- Extend `TraceIndexQueryBo` with `beginTime`/`endTime`/`okFlag`; FE date-time range + outcome radios; default last 24h.
- Detail: expand span `attrsJson`; link buttons using existing routes with query params.
- **Alt:** Client-only filter — rejected (incorrect with pagination).

### D4 — User behavior from `sys_rum_event`

- Sessions + timeline APIs under `/monitor/userBehavior/*`; FE left list / right timeline.
- Ingest: resolve login username/id via Sa-Token (or project equivalent) into `uin`.
- **Alt:** Rebuild clientTrack — rejected by product decision.

### D5 — Log hub approximate merge

- Monitor `LogHub` service pulls capped recent rows from system Facades + slowSql mapper, merges by `occurredAt`, returns `approximate=true`.
- Modulith: only `system :: api` + local slowSql; no system internal imports.
- **Alt:** FE calling three APIs — rejected (pagination/filter inconsistency).

## Risks / Trade-offs

- [Behavior coverage] → Depends on `trackAction` / router pv; document on UI; no silent full-click capture.
- [Log hub scale] → Approximate paging undercounts deep history; mitigate with tighter time windows and caps; document in API/UI.
- [uin backfill] → Historical rum rows remain without uin; only new ingest fills; timeline by sessionId still works.

## Migration Plan

1. Deploy app with V27+ Flyway.  
2. Verify menus: no overview; new user-behavior + log-hub.  
3. Smoke: trace filters, span expand, behavior after login browse, log hub three sources.  
4. Rollback: re-enable overview menu only restores entry if code redeployed from prior revision; prefer forward-fix (BI) over restoring overview code.
