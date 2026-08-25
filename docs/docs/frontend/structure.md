# 管理端目录结构

以下为常用目录（以仓库现网为准，可随迭代增减）：

```text
quick-ui/
  src/
    api/           # 按域划分的接口封装（禁止页面裸调 axios）
    assets/        # 静态资源
    components/    # 业务通用组件
    directive/     # 权限等指令
    layout/        # 布局与侧栏/顶栏
    packages/      # C7 组件库
    plugins/       # auth / modal / cache 等
    router/        # 路由与动态路由装配
    store/         # Pinia 模块
    utils/         # request、主题等工具
    views/         # 业务页面（system / monitor / tool / oauth / login 等）
  .env.development
  .env.production
  vite.config.*
```

## 业务页大致分区

| 区域 | 示例 |
|------|------|
| `views/system` | 用户、角色、部门、菜单、字典、参数、文件、OAuth 客户端等 |
| `views/monitor` | 操作/登录日志、慢 SQL、链路、用户行为、部署记录等 |
| `views/tool` | 代码生成等 |
| `views/oauth` | 授权回调等 |
| `views/dev` | C7 组件演示（开发用） |

API 与页面一一对应时，优先复用已有 `src/api/**` 模块，避免平行再造。
