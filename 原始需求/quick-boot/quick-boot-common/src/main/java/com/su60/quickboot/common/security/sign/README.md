# 接口签名验证功能（基于国密SM3）

## 快速开始

### 1. 配置文件（application.yml）

```yaml
security:
  sign:
    enabled: true                              # 启用签名验证
    algorithm: SM3                             # 签名算法
    secret-key: your-secret-key-2026          # 签名密钥
    expire-time: 300                          # 有效期（秒）
    ignore-urls:                              # 忽略验签的URL
      - /actuator/**
      - /public/**
    ip-whitelist:                             # IP白名单
      - 127.0.0.1
      - 192.168.1.100
```

### 2. 签名算法

**步骤：**
1. 收集参数（GET/POST表单：所有参数；POST JSON：json=JSON字符串）
2. 添加 timestamp、nonce
3. 按key排序：`key1=value1&key2=value2`
4. 拼接密钥：`参数字符串&key=secretKey`
5. SM3哈希：`SmUtil.sm3(signContent)`

### 3. 客户端示例

#### Java客户端

```java
// 使用SignatureHelper
Map<String, String> params = new HashMap<>();
params.put("username", "admin");

// GET请求
Map<String, String> signedParams = SignatureHelper.signGetRequest(params, SECRET_KEY);

// POST JSON请求
String jsonBody = "{\"username\":\"admin\"}";
Map<String, String> signHeaders = SignatureHelper.signJsonRequest(jsonBody, SECRET_KEY);
```

#### JavaScript客户端

```javascript
import { sm3 } from 'sm-crypto';

function generateSignature(params, secretKey) {
    const sortedKeys = Object.keys(params).sort();
    const paramStr = sortedKeys
        .filter(key => key !== 'sign')
        .map(key => `${key}=${params[key]}`)
        .join('&');
    const signContent = `${paramStr}&key=${secretKey}`;
    return sm3(signContent);
}

// POST JSON请求
const jsonBody = JSON.stringify({ username: 'admin' });
const timestamp = Math.floor(Date.now() / 1000);
const nonce = Date.now().toString();

const signParams = {
    timestamp: timestamp.toString(),
    nonce: nonce,
    json: jsonBody
};

const sign = generateSignature(signParams, SECRET_KEY);

// 发送请求（签名放Header）
fetch('/api/users', {
    method: 'POST',
    headers: {
        'Content-Type': 'application/json',
        'timestamp': timestamp.toString(),
        'nonce': nonce,
        'sign': sign
    },
    body: jsonBody
});
```

### 4. 核心类说明

| 类名 | 说明 |
|------|------|
| `SignatureFilter` | 签名验证过滤器（自动拦截） |
| `SM3SignatureUtils` | SM3签名工具类 |
| `SignatureHelper` | 签名辅助类（客户端使用） |
| `CachedBodyHttpServletRequest` | 可重复读取Body的Request包装类 |
| `SignatureException` | 签名验证异常 |

### 5. 特性

✅ IP白名单（白名单内IP不验签）  
✅ 支持GET、POST表单、POST JSON  
✅ 时间戳验证（防重放攻击）  
✅ 国密SM3算法  
✅ 灵活配置（忽略URL、有效期）

### 6. 调试

开启DEBUG日志：

```yaml
logging:
  level:
    com.su60.quickboot.common.security.sign: DEBUG
```

### 7. 测试

运行单元测试：

```bash
mvn test -Dtest=SM3SignatureUtilsTest
```

详细文档请查看：[接口签名验证使用说明.md](./接口签名验证使用说明.md)

