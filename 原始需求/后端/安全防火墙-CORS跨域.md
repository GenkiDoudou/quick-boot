# 安全防火墙：CORS 跨域（firewall/cors）原始需求

## 背景
- 前后端分离部署时需要配置跨域资源共享（CORS）。
- 不同环境允许的 Origin/Method/Header 不同，需要可配置且可快速切换。

## 目标
- 提供一套基于 Filter 的 CORS 配置能力，并支持开关。

## 功能需求
- 启用条件：`qc.security.firewall.cors.enabled=true`
- 支持配置：
  - `allowedOrigins`（支持 `*`）
  - `allowedMethods`（默认 GET/POST/PUT/DELETE/OPTIONS）
  - `allowedHeaders`（默认 `*`）
  - `exposedHeaders`
  - `allowCredentials`（默认 `true`）
  - `maxAge`（默认 3600 秒）
  - `pathPattern`（默认 `/**`）
- 对匹配路径应用 CORS 策略，确保预检请求通过。

## 配置项
- 前缀：`qc.security.firewall.cors`
  - `enabled`
  - `allowedOrigins/allowedMethods/allowedHeaders/exposedHeaders`
  - `allowCredentials/maxAge/pathPattern`

## 验收标准
- 启用后浏览器跨域请求不会被 CORS 拦截（含 OPTIONS 预检）。
- 仅对配置的路径模式生效。

