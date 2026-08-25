## ADDED Requirements

### Requirement: Three project practical doc sections
VitePress 站点 MUST 为后端 `quickboot`、管理端 `quick-ui`、移动端 `quick-h5` 各提供一组实用文档，且每组至少包含概述、快速上手、目录/模块结构、关键约定四页。

#### Scenario: Backend section pages exist
- **WHEN** 用户打开文档站侧栏「后端」分组
- **THEN** 可访问概述、上手、结构、约定四页，且内容描述与现网后端（含默认端口与开发依赖形态）一致

#### Scenario: Frontend and H5 sections exist
- **WHEN** 用户打开「管理端」或「移动端」分组
- **THEN** 各组均可访问对应四页，并说明与后端联调所需的 env / OAuth client 前提

### Requirement: Navigation exposes guide and three projects
文档站顶栏与侧栏 MUST 保留指南入口，并新增后端、管理端、移动端入口；侧栏三端顺序为概述 → 上手 → 结构 → 约定。

#### Scenario: Top nav four entries
- **WHEN** 用户查看文档站顶栏
- **THEN** 可见指南、后端、管理端、移动端四个入口且链接可达

### Requirement: Guide and home reflect reality
现有指南页 MUST 修正过时的端口、启动与依赖描述；首页与能力大纲 MUST 不再声称不存在的全量分区已完成，且不得保留指向不存在页面的主导航链接（可改为真实页或移除）。

#### Scenario: Outline accuracy
- **WHEN** 用户阅读能力大纲
- **THEN** 大纲标明实用向三端文档为已完成范围，并说明全量分区暂缓

#### Scenario: Home links resolve
- **WHEN** 用户点击首页 features 中的文档链接
- **THEN** 目标页存在且可打开（相对站点 `base`）
