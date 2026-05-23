# SSL 证书

## Let's Encrypt（certbot）

```bash
certbot certonly --nginx -d admin.example.com
```

Nginx 增加 443 server：

```nginx
server {
    listen 443 ssl http2;
    server_name admin.example.com;
    ssl_certificate     /etc/letsencrypt/live/admin.example.com/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/admin.example.com/privkey.pem;
    # ... 同上 location /
}
```

HTTP 80 做 301 跳转 HTTPS。

## 应用层

- 生产强制 HTTPS，避免 Client 签名与 Token 明文传输  
- `qc.security.firewall.headers` 可启用 HSTS（见 `application.yml`）

## 相关

- [Nginx 配置](./nginx)
