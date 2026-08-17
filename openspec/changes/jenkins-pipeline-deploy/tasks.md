## 1. Deploy scaffolding

- [x] 1.1 Create `deploy/jenkins/`, `deploy/nginx/`, `deploy/systemd/`, `deploy/env/` directory layout
- [x] 1.2 Write `deploy/env/README.md` (host config location, Credentials IDs, no-secrets rule, first-time ops checklist)
- [x] 1.3 Add `deploy/nginx/quickboot.conf.example` (`/`, `/h5/`, `/prod-api/` proxy_pass strip prefix)
- [x] 1.4 Add `deploy/systemd/quickboot.service.example` (`prod` profile + `SPRING_CONFIG_ADDITIONAL_LOCATION`)

## 2. Jenkinsfiles

- [x] 2.1 Add `deploy/jenkins/Jenkinsfile.quickboot` (params `ENV`/`BRANCH`, Maven package, SSH jar deploy, restart, smoke)
- [x] 2.2 Add `deploy/jenkins/Jenkinsfile.quick-ui` (pnpm install/build:prod, rsync `dist/` → `www/ui`, nginx reload, smoke `/`)
- [x] 2.3 Add `deploy/jenkins/Jenkinsfile.quick-h5` (pnpm install/build:h5, rsync H5 dist → `www/h5`, reload, smoke `/h5/`)
- [x] 2.4 Map `ENV=test|prod` to Credentials `deploy-test`/`deploy-prod`; fail fast on unknown ENV
- [x] 2.5 Document in `deploy/env/README.md` how to create the three Jenkins Jobs pointing at these files (manual + optional timer on test only)

## 3. Front-end production alignment

- [x] 3.1 Change `quick-ui/.env.production` `VITE_APP_BASE_API` to `/prod-api`
- [x] 3.2 Add or update quick-h5 production env so API base is `/prod-api` (and native/base notes as needed)
- [x] 3.3 Configure quick-h5 production `base` to `/h5/` (vite/uni config or env — match existing project patterns)
- [x] 3.4 Verify locally (or note in README) the actual H5 build output directory and hard-code that path in `Jenkinsfile.quick-h5`

## 4. Backend prod config template (no secrets)

- [x] 4.1 Add `deploy/env/application-prod.yml.example` with placeholders for datasource, redis, issuer, and flags to disable embedded MariaDB/Redis
- [x] 4.2 Cross-link example from `deploy/env/README.md` and systemd unit comments

## 5. Validation notes

- [x] 5.1 Smoke URLs documented (public `/prod-api/actuator/health` preferred; SSH localhost:9993 fallback if health blocked)
- [x] 5.2 Self-check: no real passwords/keys in `deploy/` or committed `.env.production` secrets beyond existing non-prod placeholders
- [x] 5.3 Mark `docs/superpowers/specs/2026-08-17-jenkins-pipeline-deploy-design.md` status as approved / linked to this change
