# C7Preview（C7 附件预览）设计说明

**日期**：2026-05-08  
**状态**：已定稿（经 brainstorming 澄清与「设计确认」）  
**依据**：`原始需求/前端/C7预览.md` + Q&A 结论（**1B、2B、3B、4B、5B、6B**）及实现路径 **路径 2**（`index.vue` + `parseUrls.ts` + `inferMediaKind.ts`）

---

## 1. 背景与目标

- **背景**：列表/详情常需预览 **图片 / 视频 / 普通文件**；附件常以 **逗号分隔 URL 串** 存储；触发方式需支持 **直接展示封面**、**按钮聚合**、**文件表格式列表**。
- **目标**：提供 **`C7Preview`**：解析 **`urls`**；按 **`autoDetect` / `displayType`** 决定每条资源类型；三种 **`coverType`** 下触发与 **原始需求** 一致的预览行为（多图 Element Plus 预览、视频 **`C7Dialog` + video**、文件 **`window.open`**）；**`onPreview`（含异步）** 可拦截；**`preview` / `close`** 事件。
- **非目标**：上传、转码、Office 在线预览；组件内发起鉴权请求；首版 **不** 支持 **`urls: string[]`**（仅 **逗号分隔字符串**，见 **第 4 节**）。

---

## 2. 命名、边界与落点

| 项 | 约定 |
|----|------|
| **对外组件名** | **`C7Preview`** |
| **目录** | **`quick-ui/src/packages/C7Preview/index.vue`**，同包 **`parseUrls.ts`**、**`inferMediaKind.ts`**（纯函数，可单测） |
| **实现形态** | 主 UI 与状态机在 **`index.vue`**；**URL 拆分**与**扩展名推断**不得散落在模板内联逻辑中。 |
| **全局注册** | 在 **`quick-ui/src/packages/index.js`** 中 **`export`** 与 **`installPackages`** 注册，与其它 C7 一致。 |

**职责**：解析展示 **`urls`**；按条推断或套用类型；三种 **`coverType`**；视频弹窗生命周期（含关闭时暂停与进度归零）；钩子与事件约定。

**不负责**：保证 URL 可访问性、跨域视频首帧、浏览器对 `window.open` 的拦截策略。

---

## 3. 类型推断与 `displayType`（**1B**）

- **`inferMediaKind(url: string): 'image' \| 'video' \| 'file'`**  
  - 从 URL 取 **pathname**（忽略 **hash**；**strip query 后再取扩展名**，避免 `?x=.png` 误判）。  
  - **image**：`jpg`、`jpeg`、`png`、`gif`、`webp`、`bmp`、`svg`（扩展名 **大小写不敏感**）。  
  - **video**：`mp4`、`webm`、`ogg`、`mov`、`avi`。  
  - **其余**：**`file`**。
- **`autoDetect === true`（建议默认 `true`）**：每条 URL 使用 **`inferMediaKind`**。  
- **`autoDetect === false`**：每条 URL 均按 **`displayType`**（**`image` \| `video` \| `file`**）渲染；**调用方保证 URL 与类型一致**，组件不做自动纠正。

---

## 4. `urls` 解析（**6B**）

- **唯一入参形态（首版）**：**`urls: string`**，内容为 **逗号分隔**。  
- 解析步骤：**`split(',')`** → 每项 **`trim`** → **过滤空串** → 得到 **`string[]`** 供内部使用。  
- **空列表**：不渲染封面区（或仅 **`coverType=button`** 时渲染禁用态按钮——实现计划二选一，须在实现中写死并加单测 / E2E 之一）。

---

## 5. `coverType` 与封面表现

### 5.1 `none`（直接展示）

- **image**：每条 **`el-image`**；**`preview-src-list`** 为 **全部图片 URL**（保持 URL 原始顺序中的图片子序列顺序，与 **第 6 节** 一致）；**`initial-index`** 为当前图在「仅图片 URL 列表」中的下标。  
- **video**：每条独立 **封面容器**（尺寸受 **`width` / `height`** 约束，见 **第 8 节**）；**不**将视频 URL 作为 **`poster`**（**2B**）；使用 **统一占位**（如中性底 + **中央播放图标**）。点击走 **第 7 节** 弹窗流程。  
- **file**：**文件图标 + 文件名**（从 pathname 取 basename，无则显示兜底短文案如「文件」）。点击 **`window.open(url, '_blank', 'noopener,noreferrer')`**（未被 **第 9 节** 钩子拦截时）。

**混合类型**：同一区域按条渲染 **图块 / 视频块 / 文件块**（可多行或多列布局以实现计划为准，须避免互相挤压不可点）。

### 5.2 `button`

- **`el-button`** + 可选 **`el-badge`** 显示 **解析后条数**（条数为 **第 4 节** 解析结果长度）。  
- 文案：**`previewText`** prop，默认 **`预览`**（不强制 i18n 词条文件，与现有 C7 风格协调即可）。  
- **点击后分支**（与「验收：与 none 下单条同类操作效果一致」对齐）：  
  1. **若至少存在一条 `image`**：触发 **与 none 下点击图片等价的「大图预览」**——**`preview-src-list` = 全部图片 URL**（顺序同 **第 6 节**），**`initial-index = 0`**（即从第一张图进入预览器；用户可在预览器内切换其它图）。  
  2. **否则若至少存在一条 `video`**：打开 **第 7 节** 所述弹窗，**初始播放索引 = 0**（在 **仅视频 URL 列表** 中的下标）；若 **视频条数 > 1**，弹窗内提供 **上一条 / 下一条** 切换当前 **`video.src`**（切换前 **pause + `currentTime = 0`**）。  
  3. **否则（仅 `file`）**：对 **索引 0** 的 URL 执行 **`window.open`**（**不**在首版一次打开多窗）。  
  4. **若解析后列表为空**：no-op 或 **`console.warn`**（实现计划写死一种）。

> **说明**：**「图 + 视频 + 文件」同时存在** 时，**`button` 单次点击** 按 **1 → 2 → 3** 优先级执行（**有图则只进入图片预览**）。业务若需在混合集合下优先看视频，应拆数据或使用 **`coverType=none` / `file`**。

### 5.3 `file`（**5B**）

- 使用 **`el-table`** **极简表**（建议列：**类型图标或扩展名**、**文件名**；列数与宽度实现计划定）。  
- **行点击**：对应该行索引 **`index`**，触发 **与 `none` 下点击同索引条目相同** 的预览或打开逻辑（图片进预览器且 **`initial-index`** 为该图在「仅图片列表」中的下标；视频进 **第 7 节** 弹窗单条播放；文件 **`window.open`**）。

---

## 6. 图片列表顺序（实现约束）

内部维护 **`parsedUrls: string[]`**（顺序与 **`urls` 解析顺序一致**）。  
**`preview-src-list`** 始终为 **`parsedUrls` 中类型为 `image` 的 URL 按出现顺序组成的数组**；任意图片条目计算 **`initial-index`** 时，均为该 URL 在此数组中的下标。

---

## 7. 视频弹窗（**3B** + 原始「关闭暂停」）

- 使用 **`C7Dialog`**：**`footer=false`**，避免默认「取消/确定」占高。  
- **标题**：**`videoDialogTitle`** prop，默认 **`视频预览`**。  
- 内容：**`<video controls autoplay :src="currentUrl">`**。  
- **关闭**（含遮罩、右上角关闭、**`v-model` 置 false`**）：**`video.pause()`**、**`video.currentTime = 0`**、**`emit('close')`**。  
- **上一条 / 下一条**（见 **第 5.2 节**）：切换前对当前 **`video` ref** 执行 **pause + 归零** 再赋新 **`src`**。

---

## 8. 尺寸与透传（对应原始需求 `width` / `height`）

- **`width` / `height`**：类型 **`number`** 时视为 **px**，拼 **`${n}px`** 绑定到 **封面容器**（`none` 下单格；`video` 占位同）；**不**替代 **`el-image`** 的 **`fit`** 等。  
- 首版可选 **`imageProps`** 对象透传到 **`el-image`**（实现计划：若成本低则做，否则仅 **`width/height`**）。

---

## 9. 钩子与事件（**4B**）

| 项 | 约定 |
|----|------|
| **`onPreview(url, index)`** | 返回值 **`boolean \| Promise<boolean>`**；**`false` / `Promise<false>` / `Promise` reject** → **阻止**后续预览或 **`window.open`**；**`true` / resolve 其它真值** → 继续。未传钩子 → **视为允许**。 |
| **调用顺序** | **先** **`await` 归一化钩子结果**；若允许，**再** **`emit('preview', url, index)`**，**再**执行具体 UI（大图 / 弹窗 / 新窗口）。 |
| **`preview`** | **`emit('preview', url: string, index: number)`**，在 **通过钩子之后**、**打开预览器 / 弹窗 / 新窗口之前**。 |
| **`close`** | **视频弹窗**从打开态变为关闭态时 **`emit('close')`** 一次（与原始需求一致）。 |

---

## 10. 与 Element Plus 的衔接（实现计划落点）

- **图片**：优先 **`el-image` + `preview-src-list` + `initial-index`**；**`button`** 模式可用 **隐藏 **`el-image`**（`opacity:0` + 固定 1px 或 `visibility` 策略）+ **`ref` 触发 `click()`** 打开预览**（以 EP 版本实际 API 为准，若 API 变更则采用官方等价写法）。  
- **表格**：**`el-table`** **`show-header`** 可按需关闭以减噪；**`row-style`** 指针手型提示可点。

---

## 11. 验收与测试建议

- **多图**：`none` 与 **`button`** 均能进入带 **完整图片列表** 的预览，**`initial-index`** 符合 **第 5～6 节**。  
- **视频**：弹窗内可播；关闭后 **暂停且进度归零**；**`close`** 已触发。  
- **文件**：新窗口打开；**`noopener,noreferrer`**。  
- **`onPreview` 返回 `false` 或 reject**：不打开预览 / 不 **`window.open`**，且 **不** **`emit('preview')`**。  
- **单测**：**`parseUrls`**（逗号、空格、空段）、**`inferMediaKind`**（query、大小写）。  
- **E2E**：与其它 C7 页同模式增加 **`C7PreviewE2E`** 覆盖关键路径（实现计划列用例表）。

---

## 12. 后续流程

- 实现前：由 **`writing-plans`** 产出分步实现计划（含 **`packages/index.js` 注册**、**`C7Dialog` 联调**、**EP 预览触发**细节）。  
- 本文档经用户审阅无修改后，再进入编码阶段。
