# 环境要求

## 开发环境

| 组件 | 版本 |
|------|------|
| JDK | 17+ |
| Maven | 3.6+ |
| Node.js | 18 / 20 LTS |
| pnpm | 9.x |

## 生产环境

| 组件 | 说明 |
|------|------|
| MySQL | 8+，主库 |
| Redis | 推荐：Sa-Token、Client nonce、验证码 |
| Nginx | 反向代理静态资源与 `/dev-api` |
| JRE | 17，运行 `quickboot-web.jar` |

## 端口（默认）

| 服务 | 端口 |
|------|------|
| quickboot-web | 9992 |
| quick-ui dev | 8800 |
| docs dev | VitePress 默认（5173） |

## 相关

- [配置说明](./configuration)
- [环境搭建](../guide/installation)
