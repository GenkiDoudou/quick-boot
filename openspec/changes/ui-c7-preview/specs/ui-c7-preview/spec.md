# ui-c7-preview

## Purpose

为 **quick-ui** 提供 **`C7Preview`**：解析 **逗号分隔 `urls`**，按 **`autoDetect`/`displayType`** 区分 **图片 / 视频 / 文件**，支持 **`coverType`**：`none` | `button` | `file`；图片走 **Element Plus 预览**；视频走 **`C7Dialog` + video**；文件 **`window.open`**；**`onPreview`** 可异步拦截；**`preview`/`close`** 事件。需求来源：**`原始需求/前端/C7预览.md`** 与 **`docs/superpowers/specs/2026-05-08-c7-preview-design.md`**。

## ADDED Requirements

### Requirement: 组件与注册位置

**`C7Preview`** MUST 位于 **`quick-ui/src/packages/C7Preview`**（至少包含 **`index.vue`**），并通过 **`quick-ui/src/packages/index.js`** 的 **`installPackages(app)`** 注册为全局组件 **`C7Preview`**。

#### Scenario: 全局注册可用

- **WHEN** 应用已执行 **`installPackages`**
- **THEN** 模板中 MUST 能直接使用 **`<C7Preview />`** 而无额外局部注册

### Requirement: URL 解析纯函数 parseUrls

系统 MUST 提供 **`parseUrls(urls: string): string[]`**：对 **`urls`** 执行 **`split(',')`**，对每项 **`trim`**，并 **过滤空串**；**不改变**非空项的原始字符（除首尾空白）。

#### Scenario: 逗号与空格

- **WHEN** **`urls`** 为 **`" a.png , b.png , "`**
- **THEN** 结果 MUST 为 **`["a.png","b.png"]`**

#### Scenario: 连续空段

- **WHEN** **`urls`** 为 **`"x,,y"`**
- **THEN** 结果 MUST 为 **`["x","y"]`**

### Requirement: 媒体类型推断 inferMediaKind

系统 MUST 提供 **`inferMediaKind(url: string): 'image' | 'video' | 'file'`**：从 **`url`** 解析 **pathname**（忽略 **hash**；**去掉 query 串后再取扩展名**）；扩展名 **大小写不敏感**。

- **image**：**`jpg` `jpeg` `png` `gif` `webp` `bmp` `svg`**
- **video**：**`mp4` `webm` `ogg` `mov` `avi`**
- **其它扩展名或无扩展名**：**`file`**

#### Scenario: query 不误判

- **WHEN** **`url`** 为 **`"https://cdn/x?fmt=.png"`** 且 pathname 为 **`/x`**（无图片扩展名）
- **THEN** 推断结果 MUST 为 **`file`**（以 pathname 扩展名为准，**MUST NOT** 仅凭 query 判为 **`image`**）

#### Scenario: 大写扩展名

- **WHEN** **`url`** 路径以 **`.MP4`** 结尾
- **THEN** 结果 MUST 为 **`video`**

### Requirement: autoDetect 与 displayType

当 **`autoDetect=true`**（组件默认值 MUST 为 **`true`**，除非 JSDoc 另有约定）时，每条 **`parsedUrls[i]`** 的有效类型 MUST 为 **`inferMediaKind(parsedUrls[i])`**。

当 **`autoDetect=false`** 时，每条 URL 的有效类型 MUST 为 **`displayType`**；**`displayType`** MUST 为 **`image` | `video` | `file`** 之一。

#### Scenario: 混合 URL 在 autoDetect 下

- **WHEN** **`autoDetect=true`** 且 **`urls`** 同时包含图片与视频 URL
- **THEN** **`none`** 模式下界面 MUST 能分别渲染 **图片条目** 与 **视频封面条目**（均可交互），且 **不**因单一 **`displayType`** 丢弃某一类条目

### Requirement: 图片 preview-src-list 与 initial-index

对任意 **`parsedUrls`**，**`previewSrcList`** MUST 定义为：**按 `parsedUrls` 顺序**，保留 **有效类型为 `image`** 的 URL 所组成的数组。

对 **`parsedUrls` 中下标为 `i` 且有效类型为 `image` 的条目**，在打开 Element Plus 图片预览时，**`initial-index`** MUST 等于 **`previewSrcList` 中该 URL 的下标**。

#### Scenario: 多图顺序一致

- **WHEN** **`urls`** 为 **`"a.png,b.png"`** 且二者均为图
- **THEN** **`preview-src-list`** MUST 为 **`["a.png","b.png"]`**；点击第二张图打开预览时 **初始索引** MUST 为 **1**

### Requirement: coverType none

当 **`coverType=none`** 时：

- **image** 条目 MUST 使用 **`el-image`**，并设置 **`preview-src-list`** 为 **`previewSrcList`**，**`initial-index`** 满足上一 Requirement。
- **video** 条目 MUST 展示 **占位封面**（**MUST NOT** 将视频 URL 作为 **`poster`** 以依赖浏览器取帧），并带 **可识别的播放入口**；用户点击且未被 **`onPreview`** 拦截时 MUST 打开 **视频弹窗**（见 **Requirement: 视频弹窗与播放控制**）。
- **file** 条目 MUST 展示 **文件名**（从 pathname 取 basename，失败时 **MUST** 有兜底文案）与 **文件意图图标或等价视觉**；用户点击且未被拦截时 MUST **`window.open(url, '_blank', 'noopener,noreferrer')`**。

#### Scenario: 文件新窗口

- **WHEN** 用户点击 **file** 条目且 **`onPreview`** 未阻止
- **THEN** 浏览器 MUST 以 **新窗口/新标签** 打开该 URL，且 **`rel`/target** 策略符合 **`noopener,noreferrer`**

### Requirement: coverType button

当 **`coverType=button`** 时，组件 MUST 渲染 **`el-button`**，且 **MUST** 支持 **`el-badge`** 展示 **`parsedUrls.length`**（长度为 0 时的展示策略与 **Requirement: 空 urls** 一致）。

**`previewText`** prop MUST 存在且默认 **`预览`**（或中文等价，实现固定一处并在 JSDoc 写明）。

用户点击按钮后，系统 MUST 按下列 **优先级** 执行 **且仅执行匹配到的第一项分支**：

1. **若存在至少一条 `image`**：MUST 打开与 **`none`** 下点击图片 **相同的大图预览能力**，**`preview-src-list`** MUST 等于 **`previewSrcList`**，**`initial-index`** MUST 为 **0**。
2. **否则若存在至少一条 `video`**：MUST 打开 **视频弹窗**，**当前播放索引** 在 **仅视频 URL 列表**（按 **`parsedUrls` 顺序过滤）** 中为 **0**；若视频条数 **大于 1**，弹窗内 MUST 提供 **上一条/下一条** 切换 **`video.src`**（切换前 MUST **pause** 且 **`currentTime=0`**）。
3. **否则（仅 `file`）**：MUST 对 **`parsedUrls[0]`** 执行 **`window.open`**（**MUST NOT** 在首版一次打开多个窗口）。

#### Scenario: 混合图与视频时按钮优先图

- **WHEN** **`urls`** 同时含 **`.png`** 与 **`.mp4`**，且 **`coverType=button`**
- **THEN** 点击按钮后 MUST **进入图片大图预览**，**MUST NOT** 在该次点击中打开视频弹窗

### Requirement: coverType file 表格

当 **`coverType=file`** 时，组件 MUST 使用 **`el-table`** 展示 **`parsedUrls`** 每一行（列至少包含 **文件名** 与 **类型/扩展提示**；表头是否展示由实现选定并在 JSDoc 说明）。

用户 **点击某行** 时，行为 MUST 与 **`none`** 模式下 **点击同一下标 `index` 的条目** **等价**（含 **`previewSrcList`/`initial-index`**、视频弹窗、**`window.open`** 的同一套规则）。

#### Scenario: 表格点视频行

- **WHEN** 该行 URL 推断为 **video** 且未被 **`onPreview`** 拦截
- **THEN** MUST 打开 **视频弹窗** 且 **当前 URL** 为该行的 URL

### Requirement: 视频弹窗与播放控制

视频弹窗 MUST 使用 **`C7Dialog`**，且 **`footer=false`**（**MUST NOT** 展示 **`C7Dialog`** 默认「取消/确定」脚栏）。

弹窗标题 MUST 来自 **`videoDialogTitle`** prop，默认 **`视频预览`**。

弹窗内容 MUST 包含 **`<video controls autoplay>`**，其 **`src`** 为当前选中的视频 URL。

当弹窗从 **打开** 变为 **关闭**（含遮罩、关闭图标、**`v-model` 置 false** 等任一关闭路径）时，组件 MUST **`video.pause()`**、**`video.currentTime = 0`**，且 MUST **`emit('close')`** **一次**。

#### Scenario: 关闭后停止播放

- **WHEN** 视频曾播放中，用户关闭弹窗
- **THEN** 再次打开同 URL 时 **MUST NOT** 从上次进度自动续播（以 **进度归零** 为准）

### Requirement: onPreview 与 preview 事件顺序

组件 MUST 接受 **`onPreview?: (url: string, index: number) => boolean | Promise<boolean>`**。

在 **任何** 打开大图预览、打开视频弹窗、执行 **`window.open`** 之前，组件 MUST **先** **`await`** 将 **`onPreview(url, index)`** 归一为 **boolean**。

当结果为 **`false`**，或 **Promise reject**，或 **resolve 为 `false`** 时，组件 MUST **不**打开预览、**不** **`window.open`**，且 **MUST NOT** **`emit('preview', ...)`**。

当结果为 **true**（或真值且实现文档化）时，组件 MUST **先** **`emit('preview', url, index)`**，**再**执行具体打开逻辑。

#### Scenario: 拦截时不 emit preview

- **WHEN** **`onPreview`** 返回 **`Promise.resolve(false)`**
- **THEN** 组件 MUST **不** **`emit('preview')`**

#### Scenario: 通过后先发 preview 再打开

- **WHEN** **`onPreview`** 返回 **`true`**
- **THEN** **`emit('preview')`** MUST **先于** **`window.open`**（或先于预览器打开）发生

### Requirement: width 与 height

当传入 **`width`** 或 **`height`** 为 **number** 时，组件 MUST 将其视为 **像素**，并绑定到 **封面容器**（**`none`** 下单格、**video** 占位、**`button`** 外容器策略在 JSDoc 与实现一致即可），格式为 **`${n}px`**。

#### Scenario: 数字转 px

- **WHEN** **`width=120`**
- **THEN** 对应容器样式 MUST 包含 **120px** 宽度语义（以计算样式或绑定 style 为实现手段）

### Requirement: 空 urls

当 **`parseUrls(urls)`** 长度为 **0** 时，组件行为 MUST 在实现中 **二选一写死** 并在 **tasks** 中勾选：**（A）不渲染任何触发/封面区** 或 **（B）`button` 模式按钮禁用且无 badge 计数误显示**；**MUST NOT** 抛未捕获异常。

#### Scenario: 空串不崩溃

- **WHEN** **`urls`** 为 **`""`** 或 **`", , "`**
- **THEN** 组件 MUST **不**因解析抛出错误
