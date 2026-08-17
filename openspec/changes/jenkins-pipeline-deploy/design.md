## Context

See `proposal.md` for motivation. Approved product/ops decisions are also recorded in `docs/superpowers/specs/2026-08-17-jenkins-pipeline-deploy-design.md`.

Current repo has no Dockerfile/Jenkinsfile; local defaults use Spring `dev` with embedded MariaDB/Redis; `quick-ui/.env.production` currently points `VITE_APP_BASE_API` at an absolute intranet URL; quick-h5 lacks a production env aligned to `/h5/` + `/prod-api`.

Constraints: Linux build agents and targets; traditional jar + Nginx; three independent Jobs; `ENV=test|prod` via SSH credentials; no Docker/K8s in this change.

## Goals / Non-Goals

**Goals:**

- Land versioned `deploy/` Jenkinsfiles and Nginx/systemd examples matching the approved path layout.
- Minimal front-end production config so same-origin `/prod-api` and `/h5/` work.
- Document host-local `application-prod.yml` expectations without committing secrets.

**Non-Goals:**

- Installing Jenkins, JDK, Nginx, MariaDB, or Redis via Pipeline.
- Shared Library extraction, auto-rollback, multi-instance LB.
- WeChat mini-program CI publish.

## Decisions

1. **Declarative Jenkinsfiles in-repo over UI-only scripts**  
   - Rationale: reviewable, reproducible across Jenkins masters.  
   - Alternative: Job DSL / Shared Library — deferred until multiple apps share more logic.

2. **Three Jobs, not one multi-select Job**  
   - Rationale: failure isolation and separate permissions/schedules (e.g. timer only on test).  
   - Alternative: single parameterized Job — rejected per approved decision B.

3. **SSH + rsync publish**  
   - Rationale: fits bare-metal Linux; Credentials Plugin maps `ENV` → `deploy-test` / `deploy-prod`.  
   - Alternative: Ansible — heavier for current team size.

4. **Host-local Spring config + jar-only deploy**  
   - Rationale: secrets never enter git or build artifacts; Flyway still runs from app on startup against external DB.  
   - Alternative: bake `application-prod.yml` into jar — rejected (secret risk).

5. **Relative `/prod-api` for both front-ends**  
   - Rationale: one Nginx cert/origin; avoids baking test/prod host IPs into static assets.  
   - Alternative: per-ENV absolute URL at build time — possible later via Jenkins `-mode` files; not required if relative works.

6. **H5 `base` = `/h5/`**  
   - Rationale: matches Nginx `location /h5/`. Verify uni-app output directory during implementation (`dist/build/h5/` or actual).

7. **Smoke via curl**  
   - Backend: prefer `GET /prod-api/actuator/health` through public Nginx URL for that ENV (fallback SSH localhost:9993 if firewall blocks).  
   - Front: `HEAD`/`GET` `/` and `/h5/`.

## Risks / Trade-offs

- [Prod profile missing / wrong active profile] → Mitigation: systemd example forces `prod` + `SPRING_CONFIG_ADDITIONAL_LOCATION`; README checklist.  
- [H5 base mismatch → asset 404] → Mitigation: task to verify build output + curl static asset once.  
- [actuator/health secured or denied] → Mitigation: smoke may use SSH local curl; document if security config blocks anonymous health.  
- [frozen-lockfile fail on agent] → Mitigation: align Node/pnpm; fix lockfile in app PRs separately.  
- [Timer hits prod by mistake] → Mitigation: document binding timers only to test Job / protect prod Job.

## Migration Plan

1. Operators prepare target hosts (dirs, Nginx conf from example, systemd, `application-prod.yml`, SSH trust).  
2. Merge `deploy/` + front-end env fixes.  
3. Create three Jenkins Jobs pointing at Jenkinsfiles; add Credentials.  
4. Dry-run `ENV=test` per Job; then `prod` manually.  
5. Rollback: redeploy previous archived jar/static artifact manually (no automated rollback in this change).

## Open Questions

- Exact uni-app H5 output directory on CI agent (confirm on first green build).  
- Whether anonymous `/actuator/health` is allowed behind `/prod-api` in prod security config (adjust smoke URL if not).
