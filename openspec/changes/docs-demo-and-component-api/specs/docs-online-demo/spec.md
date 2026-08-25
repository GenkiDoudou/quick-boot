## ADDED Requirements

### Requirement: Online demo guide page
文档站 MUST 提供指南页「在线演示」（路径对应 `/docs/guide/demo`），页内 MUST 列出并可点击以下链接：文档 `https://qc.126w.com/docs`、后台 `https://qc.126w.com`、H5 `https://qc.126w.com/h5`。

#### Scenario: Demo page lists three URLs
- **WHEN** 用户打开在线演示页
- **THEN** 可见上述三个 URL，且链接指向对应地址

### Requirement: Demo navigation entry
文档站顶栏 MUST 提供「在线演示」入口指向该页；项目介绍与快速上手页 MUST 提供指向该页的互链。

#### Scenario: Nav reaches demo page
- **WHEN** 用户点击顶栏「在线演示」
- **THEN** 打开在线演示页且内容可渲染
