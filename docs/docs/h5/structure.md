# 移动端目录结构

以现网为准的常见布局：

```text
quick-h5/
  src/
    api/           # 接口封装
    components/    # 业务组件
    pages/         # 页面（login / home / workbench / mine 等）
    static/        # 静态资源
    store/         # 状态
    utils/         # 请求与工具
  .env.development
  .env.production  # 或按 uni 多模式拆分
  pages.json       # 路由与 tabBar
  manifest.json
  vite.config.*
```

## 主要页面

| 路径 | 说明 |
|------|------|
| `/pages/login/login` | 登录 |
| `/pages/home/home` | 首页 Tab（快捷入口等） |
| `/pages/workbench/workbench` | 工作台 Tab |
| `/pages/mine/mine` | 我的 Tab |
| `profile` / `contact` / `about` | 子页 |

静态原型参考：`docs/demo/quick-h5-tab-prototype.html`。
