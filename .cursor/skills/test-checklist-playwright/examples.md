# 示例

## 触发方式（用户）

- 「变更 `ui-c7-button`：按清单生成 Playwright，报告写在同目录」
- 「`openspec/changes/ui-c7-button/` 里的测试用例清单 → test 目录 + test_report.md」

## Agent 执行顺序摘要

1. 确认变更目录：`openspec/changes/ui-c7-button/`。
2. 读取 `test_case.md`；若无则读取目录内 `*测试用例清单.md`（例如历史文件 `C7Button组件-测试用例清单.md`）。
3. **先**写 **`test_report.md` 大纲**（模板见 reference.md）：概览待定、每条 TC 表与详细节为 ⏳ 待测试。
4. 创建或补齐 `openspec/changes/ui-c7-button/test/`（`playwright.config.ts`：**`channel: 'chrome'`（或 `msedge`）+ 本地 `headless: false`**（如 `headless: !!process.env.CI`）、`package.json`）。
5. **逐条**：仅为 `TC_C7BTN_001` 写 `test.spec.ts`（含 `page.screenshot` 落盘到 `test/TC_…/`）→ 在 `test/` 下执行 `npx playwright test TC_C7BTN_001/test.spec.ts` → 失败则修并重跑 → **回填 `test_report.md`：状态、耗时，并在「关键步骤截图」用 `![](./test/TC_…/01_….png)` 嵌入刚生成的图** → 再处理 `TC_C7BTN_002` …
6. 收尾核对概览统计与全量一致性；必要时再执行一次 `npx playwright test` 做全量回归。

## OpenSpec 归档提示

若变更日后归档至 `openspec/changes/archive/...`，`test/` 目录与 `test_report.md` 随变更目录一并迁移即可；无需复制到仓库其他路径。
