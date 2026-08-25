## ADDED Requirements

### Requirement: Common package API handbook
文档站 MUST 在 `backend/components` 提供索引页，并为 `quickboot-common` 下每个一级能力包提供独立手册页；每页 MUST 包含用途、公开 API（或主要类型/注解/工具）表格，以及源码包路径；有配置键时 MUST 列出。

#### Scenario: Common index links all packages
- **WHEN** 用户打开后端组件/能力索引
- **THEN** 可导航至每个已存在的 common 包手册页

#### Scenario: Package page has API table
- **WHEN** 用户打开任一 common 包手册页
- **THEN** 页内存在 API 级表格（非仅一句话描述）

### Requirement: C7 component API handbook
文档站 MUST 在 `frontend/components` 提供索引页，并为 `quick-ui/src/packages` 中每个 C7 组件（含 C7MessageBox）提供独立手册页；每页 MUST 包含用途、导入、Props 表，以及 Events/Slots（若源码存在）与源码路径。

#### Scenario: C7 pages cover packages
- **WHEN** 用户打开管理端组件索引
- **THEN** 索引覆盖 packages 中对外导出的 C7 组件并可进入对应手册页

### Requirement: Qb component API handbook
文档站 MUST 在 `h5/components` 提供索引页，并为 `quick-h5/src/components/qb` 下各 Qb 组件及 `qbCardColumn` 工具提供独立手册页，结构与 C7 手册同级（Props/API 表 + 源码路径）。

#### Scenario: Qb handbook pages exist
- **WHEN** 用户打开移动端组件索引
- **THEN** 可导航至各 Qb 组件/工具手册页且含 API 级表格

### Requirement: Component sections in sidebar
三端侧栏 MUST 在保留原实用向页面的同时，增加组件/能力分组（索引 + 子页）；能力大纲 MUST 反映组件手册已落地（或进行中状态与事实一致）。

#### Scenario: Sidebar exposes component groups
- **WHEN** 用户浏览后端/管理端/移动端侧栏
- **THEN** 可见组件或能力相关分组且链接可达

### Requirement: No fabricated APIs
手册内容 MUST 以源码定义为准；源码未声明的 API MUST NOT 臆造，可标注透传底层库或「见源码」。

#### Scenario: Missing prop not invented
- **WHEN** 某组件未在源码中声明某 prop
- **THEN** 手册不得将其列为已支持 API
