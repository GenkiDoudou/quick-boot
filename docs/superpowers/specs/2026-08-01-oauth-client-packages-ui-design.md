# OAuth 客户端管理页（packages + 新实体）设计

**日期：** 2026-08-01  
**状态：** 待用户审阅  
**范围：** 按新 `SysOauthClient` / `SysOauthClientVo` 重做 `quick-ui` 管理页（`C7*` 组件）；补齐后端增删改；去掉旧 grant/scope/redirect/揭密流程。

## 已确认决策

| 项 | 选择 |
|----|------|
| 方案 | 前后端一起：新字段 + packages |
| HTTP | 新增 / 修改均 **POST**；删除 **GET 或 POST** |
| Client Secret | **后端自动生成**；仅创建成功后弹窗用 **C7Copy** 展示一次 |
| 旧能力 | 不做：grantTypes / scopes / redirectUris / 密码验密揭密 |

## 目标

1. 列表：`C7JsonTable` 对接分页，字段对齐新 Vo  
2. 表单：`C7Dialog` + `C7Select` / `C7Switch` 等完成新增 / 修改  
3. 后端：`page` 已有；补 `add` / `update` / `remove`；新增时生成明文 secret 并回传一次  
4. 前端 API 与旧 `/system/oauth-clients` 脱钩，对齐 `sys/oauthclient`

## 非目标

- 修改时强制重新生成密钥（本轮不做 regenerate 开关；改 secret 可后续加）  
- 列表/详情回显完整 `clientSecret`（列表脱敏或不返回）  
- 导入导出、权限码细粒度（可后续挂 `v-hasPermi`）  
- OpenSpec change 归档流程（本轮仅本设计文档）

## 后端 API

前缀：`/sys/oauthclient`（与现有 `SysOauthClientController` 一致）。

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/page` | 已有；body `PageRequest<SysOauthClientVo>`（`current`/`size`/`param`） |
| POST | `/add` | body：除 `clientSecret` 外的可写字段；服务端生成 secret；响应含明文 secret（仅此一次） |
| POST | `/update` | body：含 `clientId` + 可改字段；**不**改 secret（除非后续加开关） |
| POST / GET | `/remove` | 参数：`clientId`（单删）；POST 可扩展 `clientIds` 批量 |

### 新增请求字段

- `clientId`（必填，唯一）  
- `clientName`  
- `apiPathPatterns`（逗号分隔 Ant 路径，可空）  
- `tokenTimeout`（秒，可空）  
- `checkCaptcha`（String：`0`/`1`）  
- `status`（默认 `'0'` 启用）  
- `remark`（可选）

服务端：生成随机 `clientSecret`（明文入库，与现有种子/登录 Basic 约定一致）→ `save` → 清/写缓存 → 返回 Vo（含 secret）。

### 修改

- 以 `clientId` 定位；不可改 `clientId`  
- 更新上述可写字段；响应可不含 secret（或恒为 null）  
- `@CacheEvict` / 等价清理 `findByClientId` 缓存（`sys-oauthClient#3600`）

### 删除

- 逻辑删除（`del_flag`）若实体已支持；否则物理删  
- 清理缓存  

### 分页查询条件（param）

- `clientId` 模糊、`clientName` 模糊、`status` 精确  

列表记录：**不返回** `clientSecret`（或置空），避免表格泄露。

## 前端

### 文件

- 重写：`quick-ui/src/views/system/oauthClient/index.vue`  
- 重写：`quick-ui/src/api/system/oauthClient.js`  

### 列表（C7JsonTable）

- `row-key="clientId"`  
- `listFunction`：把 `pageNum/pageSize` 映射为 `current/size`，包一层 `POST .../page`  
- 搜索列：clientId、clientName、status  
- 表格列：clientId、clientName、apiPathPatterns、tokenTimeout、checkCaptcha、status、createTime、操作  
- 工具栏：新增；删除走 `deleteFunction` → `POST/GET remove`  
- 行操作：修改；（可选）复制 clientId 用 `C7Copy`

### 表单（C7Dialog）

- 新增：填 `clientId` 等；不出现 secret 输入框  
- 修改：`clientId` 禁用；其余可改  
- `checkCaptcha` → `C7Switch`；`status` → `C7Select`  

### 创建成功凭证弹窗

- 第二次 `C7Dialog`：展示 `clientId` + `clientSecret`  
- 各用 `C7Copy`（或整段文本一个 Copy）  
- 关闭后无法再从本页取回 secret  

## 组件映射

| 原 el-* | packages |
|---------|----------|
| 列表+分页+工具栏 | C7JsonTable |
| el-dialog | C7Dialog |
| el-select | C7Select |
| el-switch | C7Switch |
| 手工 clipboard | C7Copy |
| ElMessageBox.confirm | c7Confirm / c7DangerConfirm（可选） |

## 成功标准

1. 分页列表能按新字段展示，且无 secret 列  
2. 新增成功后弹窗可复制一次 secret；库中有对应记录  
3. 修改 POST 生效；删除 GET 或 POST 生效；再次登录/Basic 仍能读到更新后的客户端配置（缓存已失效）  
4. 页面不再引用旧 grant/scope/redirect/reveal API  

## 实现顺序（摘要）

1. Controller + Service：add / update / remove + 分页条件 + secret 生成 + 缓存清理  
2. 前端 API 对齐  
3. 重写 index.vue（C7*）  
4. 手工或编译验证 page/add/update/remove  
