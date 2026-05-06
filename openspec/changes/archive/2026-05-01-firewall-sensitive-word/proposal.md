## Why

业务侧评论、昵称、工单等文本入口需要统一治理敏感词，减少脏数据进入下游；与现有「防火墙」能力（安全头、幂等、密码编解码等）同属 `qc.security.firewall` 配置族，便于运维一致开关与排障。

## What Changes

- 在 `quickboot-common`（与现有 `firewall.*` 同级）提供 **可开关** 的敏感词 **Servlet Filter**：处理 **Query/Form 参数** 与 **`application/json` 请求体**（递归 Map/List，标量原样）。
- 配置前缀 **`qc.security.firewall.sensitive-word`**，主开关为 **`enabled`**（与 `firewall.headers` 的 `enabled` 命名一致）。**BREAKING**：若存量配置使用 `enable`，须改为 `enabled`。
- 词库：黑名单在内置默认基础上追加；白名单放行；资源路径支持 **`classpath:`** / **`file:`**；支持 **`#` 注释行**与空行；**启动期加载一次**，运行期不热更。
- 策略 **`REPLACE`**（按 `sensitive-word` 库规则替换，对外表现为掩码）与 **`THROW`**（`SensitiveWordException`，业务码 **30501**，携带命中词）；Filter 层写出与项目 **`R` + HTTP 200**、traceId/i18n 约定一致的 JSON。
- **`ignoreUrls`**：Ant 风格，命中则整请求跳过过滤。
- Filter **`Order` 取链上最前**（尽早），避免 body 被前置组件消费导致无法重写。

## Capabilities

### New Capabilities

- `firewall-sensitive-word`：敏感词过滤（启用、忽略 URL、词库加载、参数与 JSON 体范围、REPLACE/THROW、异常与响应、自动配置与验收）。

### Modified Capabilities

- （无）不修改现有 `openspec/specs/` 中其它能力的 REQ 级条文。

## Impact

- **代码**：`quickboot-common` 新增 `security.firewall.sensitiveword`（或项目既定 firewall 子包）下 Filter、配置属性、词库加载、JSON 递归处理、异常类型；`META-INF/spring/...AutoConfiguration.imports` 注册自动配置。
- **依赖**：复用已有 **`com.github.houbb:sensitive-word`**、Spring **`ResourceLoader`**、**Jackson**（与 `ObjectMapper` / `readTree` 等一致）。
- **配置**：`application.yml` 示例由 `enable` 迁移为 **`enabled`**；保留 `whiteList`/`blackList`/`ignoreUrls`/`strategy`。
- **边界**：不入库后审核流程；非 `application/json` 的 body 不重写（仍可对 URL/query/form 按需求处理）。
