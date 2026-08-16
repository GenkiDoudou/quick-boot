# quick-h5 首页快捷入口（默认 + 个人可配）

日期：2026-08-16  
状态：已批准（OpenSpec `quick-h5-home-shortcuts`）  
范围：`quick-h5` 首页 Tab + 快捷设置页；后端偏好表与聚合接口  
参照：现网 `h5Workbench` / `sys_menu`（path 以 `/pages/` 开头）

## 1. 已确认决策

| 项 | 选择 |
|----|------|
| 首页定位 | 个人轻工作台：问候 + 真跳转快捷；与「工作台」分工（全量入口 vs 我的常用） |
| 消息 / 待办 | 保留现 mock 壳；点击 toast「待接入」 |
| 快捷模型 | 系统默认若干项 + **每人可独立设置** |
| 候选池 | 复用工作台已授权菜单（与 `buildH5Workbench` 同源过滤） |
| 存储 | 新建表 `sys_user_h5_home_shortcut` |
| 数量与入口 | 最多 **8** 个；首页宫格区「编辑」进入设置页 |
| HTTP | **禁止 PUT/DELETE**；保存用 `POST .../save` |
| 聚合方式 | 服务端聚合最终宫格（方案 1），不把偏好塞进 `/auth/me` |

## 2. 目标与非目标

**目标**

- 首页快捷入口可真实 `navigateTo`，数据按用户权限过滤。
- 未设置偏好时展示系统默认（仅保留仍有权限的项）。
- 用户可编辑选中项与顺序；换设备同步（落库）。
- 角色收回菜单权限后，对应快捷自动消失。

**非目标**

- 消息 / 待办真实 API、审批流。
- PC 端「替用户配置首页」管理页。
- 拖拽排序依赖库（可用勾选顺序 / 上移下移）。
- 修改 `/auth/me` 载荷；使用 PUT/DELETE。

## 3. 信息架构

```
首页 Tab (home)
├── Hero：品牌 + 你好，{昵称}
├── 快捷入口（≤8）──[编辑]──► 设置页 (home/shortcuts)
├── 消息（mock）
└── 今天待办（mock）

工作台 Tab：全量分组菜单（既有，不变）
```

## 4. 数据模型

### 4.1 表 `sys_user_h5_home_shortcut`

| 列 | 类型（示意） | 说明 |
|----|--------------|------|
| `id` | bigint PK | 可选自增主键 |
| `user_id` | varchar / bigint | 用户 id，与现网一致 |
| `menu_id` | bigint | `sys_menu.menu_id`（C 节点） |
| `order_num` | int | 展示顺序，从小到大 |
| `create_time` / `update_time` | datetime | 按项目惯例 |

约束：唯一 `(user_id, menu_id)`。

无该用户任何行 ⇒ **未个性化** ⇒ 走系统默认解析。

### 4.2 系统默认

- 以**有序 menu_id 列表**维护（Flyway 种子旁注释或后端常量，二选一写死在实现里并在本设计验收中列明最终 id）。
- 建议默认偏向高频：用户、部门、角色、在线用户等（须为已存在的 H5 C 菜单 id）。
- 解析时：按默认顺序 ∩ 当前候选池，截断 ≤8；不足则只展示有权限项。

### 4.3 解析规则（服务端）

1. **候选池**：与 `buildH5Workbench` 相同——当前用户可见、`menu_type=C`、`path` 以 `/pages/` 开头、`visible≠1`。
2. **有偏好行**：按 `order_num` 取与候选池的交集；已失权 `menu_id` 丢弃。
3. **无偏好行**：默认 id 列表 ∩ 候选池，≤8。
4. **保存**：`menuIds` 必须 ⊆ 候选池；长度 ≤8；否则业务错误。
5. **`menuIds` 为空数组**：删除该用户全部偏好行 ⇒ 恢复默认（不是「显示空宫格」）。

## 5. API（仅 GET / POST）

挂在现有菜单域旁（如 `SysMenuController`），前缀与现网一致：`/system/menu`。

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/h5HomeShortcuts` | 返回当前用户**最终**宫格列表 |
| GET | `/h5HomeShortcutCandidates` | 返回候选池（扁平 Item 列表） |
| POST | `/h5HomeShortcuts/save` | body：`{ "menuIds": ["9002", ...] }` 全量覆盖 |

**Item VO**（可复用 / 对齐 `H5WorkbenchItemVo`）：

```json
{ "id": "9002", "label": "用户", "path": "/pages/system/user/index", "icon": "", "orderNum": 1 }
```

- 鉴权：需登录；保存时服务端再次校验候选池，禁止越权写入。
- 不使用 PUT、DELETE。

## 6. 前端

### 6.1 `pages/home/home.vue`

- `onShow`：请求 `GET h5HomeShortcuts` 渲染宫格。
- 点击：有 path → `uni.navigateTo`；无 path → toast。
- 区头「编辑」→ `/pages/home/shortcuts`。
- 加载失败：toast + 空宫格，**不**回退假 mock 入口。
- 消息 / 待办：继续 `mock/homeData.ts` + 占位 toast。

### 6.2 `pages/home/shortcuts.vue`（新建）

- 拉候选 + 当前最终列表（或偏好语义：用最终列表作初始勾选；恢复默认单独按钮）。
- 勾选 / 调整顺序，硬上限 8。
- 保存：`POST .../save`；成功后 `navigateBack`。
- 「恢复默认」：`POST` 且 `menuIds: []`。

### 6.3 其它

- `pages.json` 注册设置页（非 Tab）。
- API 封装：`api/system/menu.ts` 增补方法。
- 图标短字：与工作台 `shortOf` 同套路。
- 视觉：沿用现 home 绿渐变 Hero + 白卡片，对齐 `qb-` token。

## 7. 后端改动清单（实现时）

| 项 | 内容 |
|----|------|
| Flyway | 建表；可选注释默认 menu_id 列表 |
| Entity / Mapper | `SysUserH5HomeShortcut` |
| Service | 候选扁平化（抽取与 workbench 共用逻辑更佳）、解析、save |
| Controller | 上述 3 个接口；OpenAPI 中文摘要 |
| 默认常量 | 有序默认 menu_id |

## 8. 验收标准

1. 从未保存过偏好的用户：看到默认快捷（仅含有权限项）。
2. 编辑保存后，重新登录 / 换端仍为个人配置。
3. 「恢复默认」后回到默认解析。
4. 角色去掉某 H5 菜单后，首页与候选均不再出现该项。
5. 消息 / 待办仍为壳，不假装已接通。
6. 接口仅 GET/POST，无 PUT/DELETE。

## 9. 风险

| 风险 | 缓解 |
|------|------|
| 默认 menu_id 环境不一致 | 实现时对照 Flyway 实际 H5 菜单 id 写入常量并自测 |
| 与工作台过滤逻辑分叉 | 优先抽取共用「H5 C 入口列表」方法 |
| 用户勾满 8 个后角色变更导致变少 | 属预期；下次进设置可再选 |

## 10. 推荐实现顺序

1. Flyway + Entity/Mapper  
2. Service 解析 + 三个接口  
3. H5 API + home 接真数据  
4. shortcuts 设置页  
5. 冒烟验收 §8  

---

请审阅本文件。确认「设计可以」后，再写实现计划或 `/openspec-propose`；若要改默认项、上限或恢复默认语义，直接点名。
