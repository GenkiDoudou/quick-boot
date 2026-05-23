# Changelog
Changelog of My Project.

## Unreleased
### No issue

**安全登录模块**


[52e4f790855297a](https://gitee.com/luyanan/quickboot/commit/52e4f790855297a) luyanan *2026-03-06 08:52:05*

**docs: 更新文档结构和VitePress规范**

 * - 添加Java泛型和HTML标签转义规范到.cursorrules
 * - 重命名文档目录路径，将components/backend改为backend/components
 * - 更新sidebar.ts中的组件文档路径和新增多个模块设计文档
 * - 修复文档中的泛型语法，将List&lt;T&gt;等改为List\&lt;T\&gt;格式
 * - 删除旧的前端和后端组件索引文档
 * - 添加前端组件总览文档
 * - 修改客户端认证异常类继承WarningException并使用错误码
 * - 优化客户端认证过滤器代码结构

[e418e60f8e5d2ac](https://gitee.com/luyanan/quickboot/commit/e418e60f8e5d2ac) luyanan *2026-03-05 15:26:06*

**docs(security): 移除安全模块相关技术文档**

 * - 移除了字段脱敏模块使用文档
 * - 移除了敏感词过滤模块使用文档
 * - 移除了请求方式和域名拦截模块使用文档
 * - 清理了相关的API说明和配置示例内容
 * - 删除了模块的快速开始和使用示例章节
 * - 移除了注意事项和最佳实践相关内容

[41ba1a86f2a4f42](https://gitee.com/luyanan/quickboot/commit/41ba1a86f2a4f42) luyanan *2026-03-04 15:56:28*

**docs**


[76029917a68ffa5](https://gitee.com/luyanan/quickboot/commit/76029917a68ffa5) luyanan *2026-03-03 15:51:11*

**feat(security): 添加敏感词过滤和字段脱敏功能**

 * - 集成 sensitive-word 库实现敏感词过滤功能
 * - 添加敏感词白名单和黑名单配置支持
 * - 实现多种敏感词过滤策略（替换或抛出异常）
 * - 添加字段脱敏工具类和注解支持
 * - 配置敏感词过滤的 URL 忽略列表
 * - 添加 Micrometer 链路追踪依赖和配置
 * - 更新应用配置文件以支持新的安全功能
 * - 修复包路径错误并将相关类移动到正确位置

[f0ce2409d28fcfb](https://gitee.com/luyanan/quickboot/commit/f0ce2409d28fcfb) luyanan *2026-03-03 15:33:57*

**common模块增加缓存模块和系统应用信息模块**


[17576a2c7089544](https://gitee.com/luyanan/quickboot/commit/17576a2c7089544) luyanan *2026-03-02 14:23:12*

**common模块增加缓存模块和系统应用信息模块**


[0dea1ff8473696a](https://gitee.com/luyanan/quickboot/commit/0dea1ff8473696a) luyanan *2026-03-01 15:52:00*


