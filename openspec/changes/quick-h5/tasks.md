## 1. Backend OAuth client seed

- [x] 1.1 Add Flyway migration seeding `sys_oauth_client` for `client_id=quick-h5` (secret `quick-h5-secret`, `status=0`, `check_captcha=0`, `api_path_patterns=/**`; version = next after current max)
- [ ] 1.2 Apply migration / start backend and verify the `quick-h5` row exists

## 2. Scaffold quick-h5 package

- [x] 2.1 Create `quick-h5/` via uni-app Vue3 Vite TS scaffold (or bak config-layer fallback); set package name `quick-h5`; do not copy bak business pages
- [x] 2.2 Install deps (`uview-pro`, `pinia`, `pinia-plugin-persistedstate`, `sass`, `vitest` as needed) and ensure `dev:h5` / `build:h5` / `dev:mp-weixin` / `build:mp-weixin` scripts exist
- [x] 2.3 Smoke-run `pnpm dev:h5` once to confirm the scaffold starts

## 3. Auth utilities and HTTP

- [x] 3.1 Implement `src/utils/oauthClientBasic.ts` matching `quick-ui` obfuscation; add vitest covering vector `Basic IAAAAABvB1pOPjQcFwMcOUBEEA4hHQoA` for `quick-h5:quick-h5-secret`
- [x] 3.2 Add `.env.development` with `VITE_APP_BASE_API`, `VITE_OAUTH_CLIENT_ID=quick-h5`, `VITE_OAUTH_CLIENT_SECRET=quick-h5-secret`
- [x] 3.3 Implement `src/api/http.ts` (Basic vs Bearer, unwrap `R`, `code===200`, 401 → clear token + reLaunch login)
- [x] 3.4 Implement `src/api/auth.ts` (`POST /login` with `auth:false`, `GET /auth/me`)

## 4. Store and app boot

- [x] 4.1 Implement Pinia `user` store (login → save token → fetchMe; logout; hydrateFromStorage)
- [x] 4.2 Wire Pinia + uView Pro in `main.ts`; `App.vue` onLaunch gate to login when no token

## 5. Pages

- [x] 5.1 Set `pages.json` to login / home / mine only; remove scaffold demo/business routes if present
- [x] 5.2 Implement `pages/login/login.vue` calling store.login and navigating to home
- [x] 5.3 Implement `pages/home/home.vue` showing nickName/username and link to mine
- [x] 5.4 Implement `pages/mine/mine.vue` showing profile fields and logout

## 6. Docs and verification

- [x] 6.1 Write `quick-h5/README.md` (install, H5, mp-weixin, backend/client prerequisites, domain tip)
- [x] 6.2 Add a one-line entry for `quick-h5` in root or `quickboot/README.md` module list if present
- [ ] 6.3 Verify H5: login with system account → home → mine logout → back to login
- [x] 6.4 Verify `pnpm dev:mp-weixin` (or build) output can be opened in WeChat DevTools
- [x] 6.5 Confirm no tasks/categories/quadrant pages remain under `quick-h5/src`
