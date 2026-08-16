## 1. TabBar and routes

- [x] 1.1 Configure `pages.json` with tabBar (首页 / 工作台 / 我的) and mine subpages (`profile` / `contact` / `about`)
- [x] 1.2 Add `static/tab` icons referenced by tabBar; confirm H5 starts without missing-icon errors
- [x] 1.3 Ensure login success navigates to home tab; tab roots use switchTab-compatible paths

## 2. Mock data contracts

- [x] 2.1 Add `src/mock/homeData.ts` (shortcuts / messages / todos)
- [x] 2.2 Add `src/mock/workbenchMenus.ts` with `groups[] → items[]` shape aligned to design

## 3. Home tab

- [x] 3.1 Implement home page sections: 快捷入口 · 消息 · 今天待办 consuming home mock
- [x] 3.2 Confirm no bak tasks/categories/quadrant routes are introduced

## 4. Workbench tab

- [x] 4.1 Implement workbench page rendering grouped menu grids from mock
- [x] 4.2 Menu item tap shows non-blocking placeholder (no crash) when API absent

## 5. Mine tab and subpages

- [x] 5.1 Implement mine entries: 个人信息 · 联系我们 · 关于 · 清除缓存 · 退出登录
- [x] 5.2 Implement `profile` / `contact` / `about` pages
- [x] 5.3 Clear cache preserves login token; logout clears auth and returns to login

## 6. Docs and smoke

- [x] 6.1 Align `quick-h5/README.md` page list with Tab IA; keep pointer to design/prototype
- [x] 6.2 Confirm `docs/superpowers/specs/2026-08-12-quick-h5-design.md` §4.0 matches implemented shell
- [x] 6.3 Smoke: logged-in user can switch three tabs and open mine subpages
