# @quickboot/lite-rum

Quickboot Lite RUM：类 Aegis 的前端采集 SDK（排障链路 / 用户行为）。

## CDN

```html
<script src="/path/to/lite-rum.min.js"></script>
<script>
  const rum = new LiteRum({
    id: 'web-admin',           // 应用 ID → appId
    uin: 'admin',              // 可选
    reportApiSpeed: true,      // hook XHR/fetch（与 axios 双开会双报）
    reportAssetSpeed: false,   // 一期未实现
    spa: true,                 // History/hash → PV
    hostUrl: '/monitor/liteTrace/rum/ingest',
    actionCapture: false       // PC 管理端按钮白名单，默认关
  })
  // 登录后：rum.setUin('xxx')
</script>
```

构建：

```bash
cd packages/lite-rum
pnpm install
pnpm build
# 产物：dist/lite-rum.min.js → window.LiteRum
```

## npm / 源码引用（quick-ui）

```js
import { LiteRum, clearSessionId } from '@quickboot/lite-rum'

const rum = new LiteRum({
  id: 'web-admin',
  spa: false,              // 改用 vue-router
  reportApiSpeed: false,   // 改用 axios 观测
  actionCapture: true,
  autoStart: false,
  // 与业务 axios 一致：Bearer / Client Basic（ingest 需登录）
  getAuthHeaders: () => {
    const token = localStorage.getItem('Admin-Token') // 示例，以项目 getToken 为准
    return token ? { Authorization: 'Bearer ' + token } : {}
  }
})
```

CDN 若直连需登录的 ingest，同样传入 `getAuthHeaders`（或改为匿名上报接口）。

### uni-app（quick-h5）

传入 `storage`（`uni.getStorageSync`）与 `transport`（`uni.request`），`spa/reportApiSpeed/actionCapture` 关闭，由业务层挂 PV / API。

## 事件类型

`pv` / `action` / `api` / `error`，字段与现网 ingest 协议一致。
