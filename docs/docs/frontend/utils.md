# 工具函数

## request.js

Axios 单例：

- `baseURL`: `VITE_APP_BASE_API`
- 请求拦截：Token + **Client 签名头**
- 响应拦截：`code !== 200` 提示；401 弹窗重登
- `download(url, params, filename)`：文件下载

## auth.js

`getToken` / `setToken` / `removeToken`：Cookie 键名 `Admin-Token`。

## clientSign.js

与后端 `ClientSignService` 一致的 canonical + HMAC-SHA256，依赖：

- `VITE_APP_CLIENT_ID`
- `VITE_APP_CLIENT_SIGN_KEY`

## ruoyi.js

| 函数 | 用途 |
|------|------|
| `parseTime` | 日期格式化 |
| `handleTree` | 列表转树 |
| `resetForm` | 重置表单 |
| `addDateRange` | 查询参数附加日期范围 |
| `selectDictLabel` | 字典值转标签 |
| `tansParams` | 对象序列化为 query |

## dict.js

`useDict(...dictTypes)`：组合式 API，返回 reactive 字典 Map，供 `C7DictTag` 使用。

## validate.js

表单规则：手机号、邮箱、URL 等；挂到 `app.config.globalProperties.$validate`。

## errorCode.js

后端 `code` → 中文提示文案。

## 全局挂载（main.js）

`parseTime`、`handleTree`、`useDict`、`checkPermission` 等可在模板中直接使用。

## 相关

- [OAuth2 / Client 签名](../backend/modules/oauth2)
- [开发规范](./development-guide)
