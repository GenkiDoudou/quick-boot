# Nginx 配置

## 反向代理示例

```nginx
server {
    listen 80;
    server_name admin.example.com;

    root /usr/share/nginx/html;
    index index.html;

    # 前端 SPA
    location / {
        try_files $uri $uri/ /index.html;
    }

    # API 代理（与前端 VITE_APP_BASE_API 一致）
    location /prod-api/ {
        proxy_pass http://127.0.0.1:9992/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        client_max_body_size 100m;
    }
}
```

开发环境 Vite 等价规则：`/dev-api` → `localhost:9992`（见 `quick-ui/vite.config.js`）。

## 注意

- OAuth `redirect_uri` 须与公网域名一致  
- WebSocket（若启用）需额外 `Upgrade` 头  
- 静态资源开启 `gzip`（`vite-plugin-compression` 已生成 `.gz` 时可 `gzip_static on`）

## 相关

- [SSL 证书](./ssl)
- [本地前端](./local-frontend)
