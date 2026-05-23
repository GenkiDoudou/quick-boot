# 容器管理

## 常用命令

```bash
docker compose up -d          # 启动
docker compose logs -f quickboot
docker compose ps
docker compose restart quickboot
docker compose down           # 停止（加 -v 删卷需谨慎）
```

## 健康检查

- 后端：`curl http://localhost:9992/actuator/health`
- 前端：访问 Nginx 80 端口首页

## 日志

- 应用日志：容器 stdout / 挂载 `logs/` 目录
- 生产关闭 `qc.monitor.operlog.print` 控制台打印

## 升级

1. 构建新镜像并打 tag  
2. `docker compose pull && docker compose up -d`  
3. 确认 Flyway 迁移无报错  

## 相关

- [监控告警](./monitoring)
