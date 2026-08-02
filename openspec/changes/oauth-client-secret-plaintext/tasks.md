## 1. PasswordEncoder and plaintext write path

- [x] 1.1 Replace `AuthBeansConfiguration#passwordEncoder` with dual-mode encoder (BCrypt `encode`; BCrypt `matches` for `$2a$`/`$2b$`/`$2y$`, else plaintext equals)
- [x] 1.2 Change `OAuthClientService#toRegisteredClient` to store plaintext `clientSecret` (no encode)
- [x] 1.3 Update `Oauth2RegisteredClientSeeder` to write plaintext secrets; migrate existing BCrypt seed secrets to agreed plaintext values

## 2. Reveal API

- [x] 2.1 Add `POST /system/oauth-clients/{clientId}/reveal-secret` on `OAuthClientController` with password body DTO
- [x] 2.2 Implement reveal in `OAuthClientService`: resolve current user, verify password via `AuthUserLookup` + encoder, return `clientId` + plaintext secret (404 if missing, error if wrong password)
- [x] 2.3 Smoke: wrong password rejected; correct password returns secret; `GET` list/detail still omit secret; `/auth/login` with plaintext `quick-ui` secret succeeds

## 3. Frontend

- [x] 3.1 Add `revealOauthClientSecret(clientId, password)` in `quick-ui/src/api/system/oauthClient.js`
- [x] 3.2 Add 查看 flow on `oauthClient/index.vue`: password prompt → reveal → show copyable Client ID / Secret
- [x] 3.3 Add eye + `el-tooltip` field hints on create/edit form labels (Client ID, Secret, grants, redirects, scopes, consent)

## 4. Docs / wrap-up

- [x] 4.1 Align README or AS design note if it still says client secrets are always BCrypt
- [x] 4.2 Mark all tasks complete after smoke checks pass
