## Purpose

Defines the Web RUM SDK contract for collecting page, action, API, and error events with correlation IDs so browser traffic can join the shared trace chain.

## ADDED Requirements

### Requirement: SDK collects core event types with correlation fields
The Web SDK SHALL collect `pv`, `action`, `api`, and `error` events and MUST include `page`, `sessionId`, and timestamps. For user actions and follow-on API/error events the SDK SHALL attach `operationId`. For HTTP calls the SDK SHALL attach `traceId` and inject the same value into a request header (e.g. `X-Trace-Id`).

#### Scenario: API call carries trace header
- **WHEN** the instrumented page performs a `fetch` or `XHR` request that is not the ingest endpoint
- **THEN** the request includes a trace correlation header and an `api` event is queued with matching `traceId`, method, normalized url, status, ok, and durationMs

#### Scenario: Action creates operation context
- **WHEN** the application calls the SDK action API or a whitelisted `data-rum-action` element is activated
- **THEN** an `action` event is recorded with a new `operationId` that subsequent related `api`/`error` events in that operation inherit

### Requirement: Batch ingest envelope includes env snapshot
The SDK SHALL POST batched events to the ingest endpoint with `appId`, `sdkVersion`, `clientTime`, `events`, and an `env` object that MUST include raw `ua`. The ingest service SHALL derive `clientIp` server-side and MUST NOT require the client to self-report public IP.

#### Scenario: Successful batch upload
- **WHEN** the SDK flushes a non-empty queue to ingest with a valid `appId`
- **THEN** the server accepts the batch and persists data used for trace projection

#### Scenario: Invalid appId rejected
- **WHEN** ingest receives a batch with an unknown or disallowed `appId`
- **THEN** the server rejects the batch without affecting the host application page

### Requirement: SDK self-protection
The SDK MUST NOT report requests to its own ingest URL as `api` events, MUST rate-limit repeated identical error fingerprints in a short window, and MUST discard after limited retries on ingest failure without blocking the host UI.

#### Scenario: Ingest calls excluded
- **WHEN** the SDK sends a batch to the ingest URL
- **THEN** that HTTP call is not recorded as an `api` speed event
