---
name: test-checklist-playwright
description: >-
  Given an OpenSpec change folder under openspec/changes/, reads that change's
  《测试用例清单》 markdown: first writes the 《自动化测试报告》 outline as test_report.md from the
  checklist; then for each TC generates one Playwright spec under test/, runs only that spec
  (fix/retry until done or limit), updates that TC in the report, then proceeds
  to the next TC. Must use system-installed browser (channel chrome/msedge) with headed mode locally.
  Backfilled test_report.md must embed Playwright screenshots via Markdown images under ./test/TC_*/.
  Use when the user ties E2E automation to an OpenSpec change
  (触发词：openspec change、变更目录、测试用例清单、Playwright、自动化测试报告).
disable-model-invocation: true
---

# OpenSpec 变更：测试用例清单 → Playwright → 同变更下自动化测试报告

## 执行顺序（必读）：大纲先行 → 逐条生成并执行

**禁止**在首轮把所有 `TC_*` 脚本写完再一次性跑全量（除非用户明确要求）。默认流程如下。

1. **定位变更与清单**（见下节）。
2. **生成《自动化测试报告》大纲**：根据清单 **一次性** 在同路径落盘 **`test_report.md`**（固定文件名），结构与 [reference.md](reference.md)「自动化测试报告模板」**章节顺序一致**，内容为「可回填骨架」：
   - 「测试基本信息」：测试时间可先写「进行中」或占位，环境/账号/页面等与清单对齐；**测试用例总数**填清单条数；关联变更 slug 必填。
   - 「测试结果概览」：表格占位，通过率可先 **—** 或 **待汇总**。
   - 「测试用例清单」表：**每条 `TC_*` 一行**，「通过」列统一 **⏳ 待测试**，备注可空。
   - 「详细测试结果」：**每个用例一节 `### [用例编号] - …`**，自清单复制前置条件/步骤/预期结果；执行状态 **⏳ 待测试**，耗时/实际结果/验证结果等写「待执行」或留空。**「关键步骤截图」**大纲阶段可写占位句；执行后必须改为 **Markdown 图片嵌入**（见 [reference.md](reference.md)「报告中嵌入截图」）。
   - 「问题记录」「测试总结」：可先保留小节标题与简短占位句（如「执行过程中填写」）。
3. **初始化自动化测试工程**（若尚无）：`test/playwright.config.ts`、`package.json` 等，见交付物表；**必须**使用 **本机已安装的 Chrome / Edge（`channel`）+ 本地有头模式**，见下文「执行测试」与 [reference.md](reference.md)。
4. **按清单顺序逐条处理每条用例**（严格串行）：
   - **仅**为当前 TC 编写或修改 `test/TC_<编号>/test.spec.ts`（及本条依赖的 `auth.setup.ts` 等）；
   - **执行**：只跑当前文件（或当前 project 内等价路径），例如  
     `npx playwright test TC_<编号>/test.spec.ts`  
     （路径相对 `test/`）；**等待本轮命令结束**后再进入下一条 TC；
   - **失败调整**：对该文件按「失败时的调整与重试」循环，直至通过或达上限；
   - **回填报告**：更新大纲中该 TC 在「测试用例清单」行与「详细测试结果」对应节的状态、耗时、实际结果等；**在对应 TC 的「关键步骤截图」下写入本用例 spec 已生成的 PNG 的 Markdown 嵌入**（`![说明](./test/TC_<编号>/文件名.png)`，路径相对 `test_report.md`）。Playwright 失败时的 `screenshot`/`test-results` 若有产出，在「失败详情」中同样嵌入或写明路径；概览表可在每条完成后增量更新，或在全部结束后统一核对数字。
5. **收尾**：核对报告统计与清单一致；自检清单。

---

## 定位变更与清单

1. **变更目录**：用户给出 `<change>` slug 或完整路径 `openspec/changes/<change>/`；若未给出，可用 `openspec list` / `openspec instructions apply --change "<name>" --json` 解析，**不得捏造路径**。
2. **《测试用例清单》**：默认在该变更目录下优先查找 **`test_case.md`**（与 `openspec-test-cases` skill 约定一致）；若无则回退查找 `*测试用例清单.md`（历史变更）；若 `proposal.md` 写明了固定文件名，以 proposal 为准。
3. **解析内容**：「模块信息」「测试环境信息」及每条 `## TC_* - …` 下的五级字段（功能名称、用例标题、前置条件、测试步骤、预期结果）。

清单与当前前端 DOM 不一致时，以 **quick-ui 实测** 为准编写选择器，并在报告中备注。

---

## 交付物目录（全部在同一条 OpenSpec 变更下）

根路径：**`openspec/changes/<change>/`**

| 相对路径 | 说明 |
|----------|------|
| `test/playwright.config.ts` | 见 [reference.md](reference.md) 内联示例；`baseURL` 对齐清单「测试环境信息」；**`channel` + 本地 `headless: false`**（CI 可用环境变量改为无头）。 |
| `test/package.json` | `@playwright/test` 为 devDependency；`scripts.test`: `playwright test`。 |
| `test/TC_<编号>/test.spec.ts` | 每条清单用例一个目录；`test.describe` / `test` 标题与清单一致。 |
| `test/TC_<编号>/*.png` | 关键步骤截图（spec 内 `page.screenshot` 产出）；**`test_report.md` 内须用 `![](...)` 嵌入**，相对路径：`./test/TC_xxx/yy_描述.png`。 |
| `test_report.md` | **固定文件名**，与 `test/` **同级**，位于 `openspec/changes/<change>/`；正文内「模块名称」等仍取自清单。 |

---

## 生成 Playwright 脚本

**节奏**：一次只落实 **一条** 清单用例对应的目录与 `test.spec.ts`；跑通并回填报告后再写下一条的脚本。

1. **抽取字段**：用例编号来自二级标题 `## TC_XXX_YYY - …`。
2. **001 登录类**：可每条 spec 内登录，或 `auth.setup.ts` + `storageState`（择一，变更内需统一）。
3. **选择器**：优先 `getByRole`、`getByLabel`、`getByPlaceholder`；有 `data-testid` 优先；避免脆弱 XPath。
4. **断言**：「预期结果」逐条映射为 `expect`；必要时 `waitForResponse` / 等待提示组件。
5. **稳定性**：少用 `networkidle`；优先 `locator.waitFor` 与合理 `timeout`。
6. **截图（供报告嵌入）**：每条用例应在关键步骤（登录后、操作前后、断言点前等）调用 `page.screenshot`，文件落在 **`test/TC_<编号>/`**，命名建议 `01_简述.png` 递增；回填报告时在「关键步骤截图」**逐条嵌入**对应图片，不得仅列路径而无 `![…](…)`。
7. **注释**：每个 `test` 顶部注明对应清单用例编号。

代码骨架与 `playwright.config.ts` 全文示例见 [reference.md](reference.md)。

---

## 执行测试

工作目录：**`openspec/changes/<change>/test/`**

### 外部浏览器 + 有头模式（本 skill 默认，必选）

- **`channel: 'chrome'`**（macOS/Linux/Windows 已安装 Google Chrome）或 **`channel: 'msedge'`**（Windows 已安装 Edge）：使用**本机浏览器二进制**，不得仅依赖未配置的 Playwright 内置 Chromium 作为默认回归方式。
- **本地执行默认必须有界面**：在 `playwright.config.ts` 的顶层 `use` 中设置 **`headless: !!process.env.CI`**（或等价：`CI=true` 时无头，否则 **`headless: false`**）。生成的配置须保证开发者在不设 `CI` 时**自动弹出本机浏览器窗口**。
- 若仍需 CLI 覆盖（例如临时无头调试），可使用 `npx playwright test … --headed` / `--debug` 等；**默认仍以配置文件为准优先保证有头 + channel**。
- 首次仍需 `npx playwright install`（安装 Playwright 浏览器驱动）；配合 `channel` 时使用官方文档中与 Chrome/Edge 匹配的依赖说明。
- **CI**：在流水线中设置 `CI=true`（或项目约定的环境变量）以启用无头；并确保 Runner 已安装对应 Chrome/Edge 或使用 Playwright 提供的镜像。

### 命令（默认：单条用例文件）

```bash
npm install
npx playwright test TC_<编号>/test.spec.ts
```

便于回填报告：

```bash
npx playwright test TC_<编号>/test.spec.ts --reporter=list
npx playwright test TC_<编号>/test.spec.ts --reporter=json --output-file=results.json
```

用户明确要求「一次性回归全部」时，可在收尾阶段执行：

```bash
npx playwright test
```

---

## 失败时的调整与重试（必读）

不得跑一次失败即停止。循环：**读报错 / trace / 失败截图 → 修改脚本或配置或标注跳过 → 再跑**，直至本轮全部通过或达到 **建议上限 5 轮**。

1. **证据**：终端输出、HTML report、`test-results/`、`playwright test --last-failed`。
2. **修复**：超时 → 等待策略；选择器失效 → 按当前 DOM 重写；环境未就绪 → 启动前后端或在报告中标 ⏸️ 并说明。
3. **默认不改业务代码**（除非用户明确要求）。
4. **仍失败**：写入报告「问题记录」，对应用例 ❌ 或 ⏸️。

---

## 更新《自动化测试报告》

- **路径**：`openspec/changes/<change>/test_report.md`（固定文件名）。
- **结构**：严格遵循 [reference.md](reference.md) 中的「自动化测试报告模板」（章节顺序与表格字段完整）。
- **两阶段**：先按「执行顺序」写入 **大纲**（占位与 ⏳）；每执行完一条 TC 即 **局部刷新** 该 TC 在清单表与详细节的内容，避免一次性文末才写报告。
- **截图嵌入（必选）**：每个 TC 的「关键步骤截图」小节须包含与本用例 **`test/TC_<编号>/` 下实际 PNG 文件一致** 的 Markdown 图片语法，例如 `![登录表单](./test/TC_XXX_001/01_login_form.png)`，以便在 IDE / Git / VitePress 中直接预览。**禁止**仅在报告中写「见某目录」而无嵌入（失败用例除外：可嵌入 Playwright 失败截图或指向 `test-results/` 下的相对路径并注明）。
- **增强**：可同时保留简短文字说明、**备注**、分步骤「验证结果」；状态含 ✅ / ❌ / ⏳ / ⏸️。
- **刷新策略**：同一变更多次执行时 **覆盖更新** 同一份报告中的基本信息、概览表、用例清单表与各 TC 详细节；历史靠 Git。

---

## 自检清单

- [ ] 已 **先于脚本** 生成 **`test_report.md` 大纲**（章节齐全、每条 TC 占位）。
- [ ] 脚本与报告均在 **`openspec/changes/<change>/`** 下（`test/` + `test_report.md`），未写到无关目录。
- [ ] `playwright.config.ts` 的 `baseURL` 与清单一致；已配置 **`channel: 'chrome'` 或 `'msedge'`**，且本地默认 **`headless: false`**（CI 通过环境变量无头）。
- [ ] 每条清单用例：**生成 → 单文件执行 → 回填** 后再处理下一条；或有 ⏸️ 及理由。
- [ ] 每条清单用例有 `TC_*/test.spec.ts` 或报告中有 ⏸️ 及理由。
- [ ] 报告章节与 [reference.md](reference.md) 模板一致；统计数字与详细结果一致。
- [ ] 每条已执行 TC 的「关键步骤截图」已 **Markdown 嵌入** `./test/TC_<编号>/…png`（与 spec 产出文件名一致）。
- [ ] 失败已按「调整与重试」执行或已达上限并记录。

---

## 附加参考

- 模板与配置全文：[reference.md](reference.md)
- 触发示例：[examples.md](examples.md)
