# 内联模板（Playwright 与报告）

## playwright.config.ts 示例

以下为可直接套用后再改 `baseURL` 的完整示例。**须**包含：`channel`（本机 Chrome/Edge）+ 本地有头（`headless: !!process.env.CI`）。

```typescript
import { defineConfig, devices } from '@playwright/test';

export default defineConfig({
  testDir: './',
  fullyParallel: true,
  forbidOnly: !!process.env.CI,
  retries: process.env.CI ? 2 : 0,
  workers: process.env.CI ? 1 : undefined,
  reporter: 'html',
  use: {
    baseURL: 'http://localhost:8800',
    trace: 'on-first-retry',
    screenshot: 'only-on-failure',
    // 外部浏览器：本机已安装的 Chrome（Windows 可用 'msedge'）
    channel: 'chrome',
    // 本地默认有头；CI 设 CI=true 时为无头
    headless: !!process.env.CI,
  },
  projects: [
    {
      name: 'chrome',
      use: {
        ...devices['Desktop Chrome'],
        launchOptions: {
          args: ['--start-maximized'],
        },
        viewport: null,
        deviceScaleFactor: undefined,
      },
    },
  ],
});
```

- `baseURL`：改为清单「测试环境信息」中的站点根（无路径后缀）；Hash 路由在 `page.goto` 中使用 `/#/...`。
- `testDir: './'`：与 **`test/`** 根目录下分散的 `TC_*/test.spec.ts` 匹配。
- **`channel`**：`'chrome'` 或 `'msedge'`（Windows）；机器未安装对应浏览器时须在报告中备注环境，并改为可运行的等价方案（如安装浏览器后再跑）。
- **`headless`**：本 skill 要求本地肉眼可观察自动化过程，故默认 **`headless: !!process.env.CI`**；流水线需导出 `CI=true`（或你在配置中采用的同名变量）再跑无头。


## package.json 示例

```json
{
  "name": "openspec-change-e2e",
  "version": "1.0.0",
  "private": true,
  "scripts": {
    "test": "playwright test"
  },
  "devDependencies": {
    "@playwright/test": "^1.45.0"
  }
}
```

## test.spec.ts 骨架

```typescript
import { test, expect } from '@playwright/test';
import * as path from 'path';

test.describe('TC_XXX_001 - 与清单二级标题一致', () => {
  test('与清单「用例标题」一致', async ({ page }) => {
    const screenshotDir = path.join(__dirname);
    // 步骤来自清单「测试步骤」；expect 对齐「预期结果」
    // 每条用例须在 screenshotDir 下落盘至少一张关键图，供 test_report.md 用 ![](...) 嵌入
    await page.screenshot({ path: path.join(screenshotDir, '01_描述.png'), fullPage: true });
  });
});
```

## 报告中嵌入截图（必选）

`test_report.md` 与 `test/` 同在 `openspec/changes/<change>/` 下，**图片路径一律相对 `test_report.md`**。

1. **语法**：使用标准 Markdown 图片，便于预览：
   - `![简要说明](./test/TC_XXX_YYY/01_描述.png)`
2. **文件来源**：优先使用对应 `test/TC_<编号>/test.spec.ts` 内 `page.screenshot({ path: … })` 生成的 PNG；文件名与报告中嵌入保持一致。
3. **多条步骤**：按步骤递增编号，每条一行嵌入 + 可选简短说明：
   - `![打开登录页](./test/TC_XXX_001/01_login_form.png)`
   - `![登录后落地页](./test/TC_XXX_001/02_after_login.png)`
4. **失败用例**：若配置了 `screenshot: 'only-on-failure'` 或 HTML report / `test-results/` 下有截图，在「失败详情」中 **同样使用 `![](…)` 嵌入**（路径写相对 `test_report.md` 可达的副本，或将截图复制到 `test/TC_<编号>/fail_01.png` 再嵌入，避免长路径失效）。
5. **大纲阶段**：可写「待执行」；**该 TC 跑通并生成 PNG 后必须替换为真实 `![](./test/TC_…/….png)`**，不得长期留空占位。

## 自动化测试报告模板（须完整章节）

生成 **`openspec/changes/<change>/test_report.md`** 时，**按下列结构与顺序**编写（摘录自团队文档标准）；正文一级标题仍可用 **`# [模块名称] - 自动化测试报告`**（模块名取自清单）。

```markdown
# [模块名称] - 自动化测试报告

## 测试基本信息
- **测试时间**: [执行日期]
- **测试环境**: [环境地址]
- **测试账号**: [测试账号]
- **测试模块**: [模块名称]
- **测试页面**: [页面URL]
- **测试用例总数**: [总数]条
- **关联 OpenSpec 变更**: [change slug，如 ui-c7-button]

## 测试结果概览

| 测试项 | 总数 | 通过 | 失败 | 跳过 | 通过率 |
|--------|------|------|------|------|--------|
| [功能分类1] | [X] | [Y] | [Z] | [W] | [通过率%] |
| **总计** | **[总数]** | **[通过数]** | **[失败数]** | **[跳过数]** | **[总通过率%]** |

## 测试用例清单

| 用例编号 | 功能名称 | 用例标题 | 通过 | 备注 |
|---------|---------|---------|------|------|
| [用例编号] | [功能名称] | [用例标题] | ✅ 通过 / ❌ 失败 / ⏳ 待测试 / ⏸️ 跳过 | [备注] |

## 详细测试结果

### [用例编号] - [用例标题]
- **功能名称**：[功能名称]
- **执行状态**：✅ 通过 / ❌ 失败 / ⏸️ 跳过
- **执行耗时**：[耗时]
- **前置条件**：
  [前置条件列表]
- **执行步骤**：
  1. [步骤1描述]
  2. [步骤2描述]
- **关键步骤截图**（**须嵌入自动化产出 PNG**，相对 `test_report.md`）：
  - ![步骤1-描述](./test/[用例编号]/01_描述.png)
  - ![步骤2-描述](./test/[用例编号]/02_描述.png)
- **预期结果**：
  [预期结果列表]
- **实际结果**：
  [实际结果描述]
- **验证结果**：
  - ✅ 步骤1：通过
  - ❌ 步骤2：失败 - [失败原因]
- **失败详情**（如果失败）：
  - 失败步骤：[步骤编号]
  - 失败原因：[详细原因]
  - 错误截图（须 `![](…)` 嵌入）：![失败现场](./test/[用例编号]/fail_01.png) 或 `./test-results/...` 可达路径
- **建议**（如果失败）：
  [修复建议]

（对每个用例重复上述「###」块）

## 问题记录

### [问题编号] - [问题标题]
- **关联用例**：[用例编号]
- **问题描述**：[详细描述]
- **严重程度**：高/中/低
- **复现步骤**：
  1. [步骤1]
- **预期结果**：[预期结果]
- **实际结果**：[实际结果]
- **建议**：[修复建议]

## 测试总结

### 测试执行情况
[总体执行情况说明]

### 测试结果分析
[测试结果分析]

### 问题汇总
[问题汇总和优先级排序]

### 改进建议
[改进建议]
```

## JSON 结果回填（可选）

```bash
npx playwright test --reporter=json --output-file=results.json
```

将 `suites[].specs[].tests[].results[].status` 映射到各 `TC_*` 编号，核对概览表与清单表合计。
