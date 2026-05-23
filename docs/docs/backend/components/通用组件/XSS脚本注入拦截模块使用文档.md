# XSS 脚本注入拦截

## 配置

```yaml
qc:
  security:
    firewall:
      xss:
        enabled: true
        forbidden-message: 您的请求包含非法字符，请检查！
```

## 行为

- `XssFirewallFilter` 过滤参数与 JSON 文本
- `MultipartFormDataTextParts` 处理文件表单中的文本字段
- 公告富文本另用 OWASP HTML Sanitizer（web 层）

## 类

`XssFirewallRuleSet`、`XssFirewallAutoConfiguration`

## 相关

[安全防护模块](../../modules/security-module)
