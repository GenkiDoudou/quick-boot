## 1. Backend service & API

- [x] 1.1 扩展 `ISysOauthClientService`：`add` / `update` / `remove`（及可选批量 remove）签名与返回 `SysOauthClientVo`
- [x] 1.2 实现分页条件：`clientId`/`clientName` 模糊、`status` 精确；page 结果清空 `clientSecret`
- [x] 1.3 实现 `add`：校验 `clientId` 唯一、生成随机明文 secret、保存、CacheEvict、响应含 secret
- [x] 1.4 实现 `update`：按 `clientId` 更新可写字段、不改 secret、CacheEvict
- [x] 1.5 实现 `remove`：逻辑/物理删除 + CacheEvict；Controller 暴露 `POST /add`、`POST /update`、`GET|POST /remove`
- [x] 1.6 `mvn -pl quickboot-system -am compile` 通过

## 2. Frontend API

- [x] 2.1 重写 `quick-ui/src/api/system/oauthClient.js`：page / add / update / remove，对齐 `/sys/oauthclient`
- [x] 2.2 删除或停用旧 list/get/reveal 对 `/system/oauth-clients` 的导出（本页不再引用）

## 3. Frontend page (packages)

- [x] 3.1 重写 `oauthClient/index.vue`：`C7JsonTable` 列表/搜索/分页/删除，`pageNum→current` 映射
- [x] 3.2 新增/修改表单：`C7Dialog` + `C7Select`/`C7Switch`；新增无 secret 输入；修改禁用 clientId
- [x] 3.3 创建成功凭证弹窗：展示 clientId/secret，`C7Copy` 可复制
- [x] 3.4 确认页面无 grantTypes/scopes/redirect/reveal 与旧 API 调用

## 4. Verification

- [x] 4.1 手工或接口验证：page 无 secret；add 返回 secret 且入库；update 不改 secret；GET/POST remove 生效
- [x] 4.2 验证写后 `findByClientId` / Client Basic 无脏缓存
