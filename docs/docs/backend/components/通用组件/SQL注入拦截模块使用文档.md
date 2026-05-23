# SQL 注入拦截模块

## 配置

```yaml
qc:
  security:
    firewall:
      sql-injection:
        enabled: true
        ignore-json-fields:
          - apiPathPatterns   # OAuth Ant 路径避免误报
```

## 行为

`SqlInjectionFirewallFilter` 检测 query/form/JSON 中的危险关键字；必要时包装 `CachedBodyHttpServletRequestWrapper` 重复读 body。

## 注意

富 SQL 报表接口可加入 `ignoreUrls`（谨慎）。

## 相关

[安全防护模块](../../modules/security-module)
