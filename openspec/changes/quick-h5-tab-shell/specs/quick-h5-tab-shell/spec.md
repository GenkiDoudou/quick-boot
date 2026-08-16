## Purpose

Defines the Quick H5 three-tab shell (home, workbench, mine): section layout, mock-backed content for the first version, and mine subpage behaviors including cache clear and logout.

## ADDED Requirements

### Requirement: Three-tab navigation shell
The Quick H5 client SHALL expose exactly three main tabs after login: 首页 (home), 工作台 (workbench), and 我的 (mine). Tab switching MUST use the platform tab navigation (not a stack-only navigation for these three roots).

#### Scenario: Logged-in user sees three tabs
- **WHEN** a user is logged in and opens the main shell
- **THEN** the client shows tab items labeled 首页, 工作台, and 我的

#### Scenario: Switch between tabs
- **WHEN** the user selects a different tab
- **THEN** the corresponding root page is shown without requiring a full re-login

### Requirement: Home tab content sections
The home tab SHALL present three sections in order: 快捷入口, 消息, and 今天待办. First-version content MAY be local mock data; it MUST NOT introduce bak task-domain pages (categories / quadrant).

#### Scenario: Home sections visible
- **WHEN** the user opens the 首页 tab
- **THEN** the page shows 快捷入口, 消息, and 今天待办 sections

#### Scenario: Today todos are shell mock only
- **WHEN** the user interacts with 今天待办 items on the home tab
- **THEN** behavior is limited to shell/mock interaction and does not open bak task/category/quadrant business pages

### Requirement: Workbench configurable menu structure
The workbench tab SHALL render menu groups and items in a structure compatible with backend-configurable delivery (`groups` containing `items` with label and navigation metadata). The first version MAY use local mock data that mirrors this shape. This change does NOT require a live admin configuration API.

#### Scenario: Workbench shows grouped menus from mock
- **WHEN** the user opens the 工作台 tab
- **THEN** the page shows one or more menu groups, each with a grid of menu items

#### Scenario: Menu item tap without backend
- **WHEN** the user taps a workbench menu item while no backend menu API is wired
- **THEN** the client MUST NOT crash and MAY show a non-blocking placeholder feedback

### Requirement: Mine tab entries and subpages
The mine tab SHALL provide entries for 个人信息, 联系我们, 关于/清缓存, and 退出登录. 个人信息, 联系我们, and 关于 MUST be reachable as dedicated pages or equivalent navigable views.

#### Scenario: Open personal info
- **WHEN** the user opens 个人信息 from 我的
- **THEN** the client shows identity fields sourced from the logged-in profile (username, nick name, and user id when available)

#### Scenario: Open contact us
- **WHEN** the user opens 联系我们
- **THEN** the client shows static contact information

#### Scenario: Open about
- **WHEN** the user opens 关于
- **THEN** the client shows application name and version information

### Requirement: Clear cache preserves login session by default
Clearing cache from 我的 MUST remove application cache data and MUST preserve the login token / session by default, unless the product later explicitly changes this policy.

#### Scenario: Clear cache keeps session
- **WHEN** the user confirms 清除缓存 while logged in
- **THEN** the client clears designated app cache keys and the user remains logged in

### Requirement: Logout returns to login
Exiting from 我的 MUST clear local auth state and return the user to the login page.

#### Scenario: Logout
- **WHEN** the user confirms 退出登录
- **THEN** local auth state is cleared and the login page is shown
