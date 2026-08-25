## 1. 在线演示

- [x] 1.1 新增 `docs/docs/guide/demo.md`（三链接：`https://qc.126w.com/docs`、`https://qc.126w.com`、`https://qc.126w.com/h5`）
- [x] 1.2 更新 `nav.ts` 增加「在线演示」入口
- [x] 1.3 `introduction.md` / `quick-start.md` 增加指向演示页的互链

## 2. 导航骨架（组件分区）

- [x] 2.1 更新 `sidebar.ts`：三端增加 components 分组（先挂索引链接，子页随内容补齐）
- [x] 2.2 新增三端 `components/index.md` 索引骨架（随后回填完整链接表）

## 3. 后端 common 手册

- [x] 3.1 为 `quickboot-common` 各一级包新增手册页（api/cache/captcha/… 等，含 API 表与源码路径）
- [x] 3.2 回填 `backend/components/index.md` 全量链接，并更新 backend 侧栏子项

## 4. 管理端 C7 手册

- [x] 4.1 为 `quick-ui/src/packages` 各 C7 组件（含 C7MessageBox）新增手册页（Props/Events/Slots + 示例 + 源码路径）
- [x] 4.2 回填 `frontend/components/index.md` 与 frontend 侧栏子项

## 5. 移动端 Qb 手册

- [x] 5.1 为 `quick-h5/src/components/qb` 各组件及 `qbCardColumn` 新增手册页
- [x] 5.2 回填 `h5/components/index.md` 与 h5 侧栏子项

## 6. 大纲与验收

- [x] 6.1 更新 `capabilities-outline.md` 组件手册状态
- [x] 6.2 `cd docs && pnpm build` 成功；抽查演示页与三端若干组件页可打开
