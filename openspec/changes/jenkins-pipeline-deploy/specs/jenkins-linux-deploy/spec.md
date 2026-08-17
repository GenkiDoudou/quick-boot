## Purpose

Defines Linux traditional-host Jenkins deployment for quickboot, quick-ui, and quick-h5: parameterized SSH publish, same-origin Nginx path layout, and production front-end base/API alignment.

## ADDED Requirements

### Requirement: Three independent deploy pipelines

The repository MUST provide three Declarative Jenkins Pipeline definitions (one each for quickboot, quick-ui, and quick-h5) that can be wired as independent Jenkins Jobs.

#### Scenario: Parameterized environment selection
- **WHEN** an operator starts any of the three Jobs with parameter `ENV` set to `test` or `prod`
- **THEN** the pipeline MUST select the corresponding SSH deploy credential mapping for that environment and MUST NOT hard-code production secrets in the repository

#### Scenario: Shared stage skeleton
- **WHEN** any of the three Jobs runs successfully through deploy
- **THEN** the run MUST execute checkout of the selected branch, build of that app, deploy of artifacts to the target host, and a lightweight smoke check; failure in any stage MUST fail the Job

### Requirement: Same-origin Nginx path layout

Deployed environments that follow this capability MUST expose the three apps under one host with path prefixes: UI at `/`, H5 at `/h5/`, and API reverse proxy at `/prod-api/` stripping the prefix to the backend process.

#### Scenario: Path routing
- **WHEN** a client requests `/`, `/h5/`, or `/prod-api/` on the configured Nginx host
- **THEN** Nginx MUST serve quick-ui static files, quick-h5 static files, or proxy to the local backend respectively, without requiring separate public API origins for the browser admin/H5 clients

### Requirement: Backend jar deploy with external prod config

The quickboot pipeline MUST publish a packaged Spring Boot jar to a fixed app directory on the target host and restart a systemd unit that runs with `spring.profiles.active=prod` and loads configuration from a host-local config directory outside the jar.

#### Scenario: Config not overwritten by pipeline
- **WHEN** the quickboot Job deploys a new jar
- **THEN** the pipeline MUST NOT overwrite host-local production datasource/redis/secret files as part of the default deploy

#### Scenario: Embedded stores not required on target
- **WHEN** the backend runs under the prod profile on the target host
- **THEN** the deployment documentation and example unit MUST assume external MariaDB and Redis already exist on the host (or reachable network), not embedded dev stores

### Requirement: Front-end static deploy and production API base

The quick-ui and quick-h5 pipelines MUST build production static assets and publish them to fixed Nginx document roots (`www/ui` and `www/h5`). Production builds MUST use API base path `/prod-api` for same-origin proxying. quick-h5 production assets MUST be built such that they load correctly under the `/h5/` URL prefix.

#### Scenario: Relative API base
- **WHEN** quick-ui or quick-h5 is built for production under this capability
- **THEN** the baked API base MUST be the relative path `/prod-api` (not a machine-specific absolute intranet URL)

#### Scenario: H5 subpath assets
- **WHEN** a browser loads the H5 app from `/h5/`
- **THEN** static asset URLs MUST resolve under `/h5/` without 404s caused by root-relative base mismatch

### Requirement: Secrets stay out of git

The repository MUST NOT contain real production database passwords, OAuth client secrets for production, or SSH private keys. Examples and README MUST instruct operators to place secrets in Jenkins Credentials and/or host-local config files.

#### Scenario: Example config only
- **WHEN** a contributor reviews `deploy/` examples and env README in the repository
- **THEN** they MUST find placeholders or instructions only, with no committed production secret values

### Requirement: Smoke checks after deploy

Each Job MUST perform a lightweight HTTP smoke check appropriate to the app after deploy (backend health via `/prod-api` or local port; front-ends via `/` or `/h5/` returning success status).

#### Scenario: Failed smoke fails job
- **WHEN** the smoke check does not receive an expected successful HTTP response
- **THEN** the Jenkins Job MUST be marked failed
