## 1. Shared obfuscation and Filter

- [x] 1.1 Add shared deobfuscation utility (agreed reversible algorithm) in `quickboot-auth`
- [x] 1.2 Implement `ClientBasicAuthenticationFilter`: exclude paths; skip valid user Bearer; else require Basic → deobfuscate → load client → matches secret → set request attribute; uniform client-invalid on failure
- [x] 1.3 Register the Filter in Security filter chains (coordinate with Resource Server / AS)

## 2. Login token issuance

- [x] 2.1 Change `AuthTokenService.issueUserToken` to accept `RegisteredClient` (remove hardcoded `FIRST_PARTY_CLIENT_ID` for password login path)
- [x] 2.2 Update `/auth/login` to use Filter-validated client; require password grant; keep body as username/password only
- [x] 2.3 Align `/auth/refresh` (and other no-JWT auth entrypoints) with Basic Filter; reject mismatch vs authorization client if applicable

## 3. Frontend

- [x] 3.1 Add env vars + obfuscate helper + Basic header builder in `quick-ui`
- [x] 3.2 Update request interceptor: no user token → inject Basic; with user token → Bearer only
- [x] 3.3 Keep `login()` body as username/password only; verify login works with interceptor

## 4. Docs and verify

- [x] 4.1 Update `quickboot/README.md` login notes (Basic + env; no hardcode quick-ui for login)
- [x] 4.2 Smoke: missing/wrong Basic fails; correct Basic + user succeeds; Bearer API without Basic succeeds
- [x] 4.3 Mark all tasks complete
