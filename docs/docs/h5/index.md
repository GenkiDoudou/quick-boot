# 移动端概述（quick-h5）

`quick-h5/` 是 uni-app（Vue3 + Vite + uView Pro）多端客户端，对接 quickboot 登录与业务能力，当前重点支持 **H5** 与 **微信小程序**。

## 技术栈

- uni-app Vue3 + Vite
- uView Pro
- 与管理端共用后端账号体系；独立 OAuth Client：`quick-h5`

## 与另外两端

| 对比 | 说明 |
|------|------|
| 后端 | 同一套 API / 权限；开发默认端口 9993 |
| 管理端 | 不同工程与 Client；H5 产物常挂 Nginx `/h5/` |

设计背景可参考仓库内 `docs/superpowers/specs/2026-08-12-quick-h5-design.md`（未编入本站点构建）。
