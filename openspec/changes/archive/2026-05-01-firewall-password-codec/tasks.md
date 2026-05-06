## 1. 模块与依赖

- [x] 1.1 确认 `quickboot-common`（或与现有防火墙代码一致模块）的包路径，新增 `password`/`codec` 包目录
- [x] 1.2 核对父 POM / common 模块已包含 **Hutool**；若缺失则增补坐标，**禁止** 引入 `spring-security-*`

## 2. PasswordCodec 核心实现

- [x] 2.1 定义 `PasswordCodec` 接口或抽象：声明 `setProperties(Properties)`、`encrypt(String raw, String codecId)`、`matches(String raw, String prefixEncoded)`、`decrypt(String prefixEncoded)`（SM4 有效；bcrypt 调用应失败或文档约定）
- [x] 2.2 实现前缀解析：区分 `{bcrypt}`、`{sm4:keyId}`；无 `{...}` 前缀的 `matches` 走默认 **bcrypt**
- [x] 2.3 实现 bcrypt 分支（Hutool），输出 `{bcrypt}` + 标准哈希串
- [x] 2.4 实现 SM4 分支（Hutool 国密）：从 `setProperties` 解析 `sm4.keys.<keyId>`（32 hex）与可选 `sm4.defaultKeyId`；负载 **hex**；`encrypt`/`matches`/`decrypt` 闭环
- [x] 2.5 为公开类与关键方法编写 **JavaDoc**（简体中文，说明线程安全与「仅初始化期 setProperties」）

## 3. Spring Boot 自动配置

- [x] 3.1 新增配置属性类（绑定 `qc.security.firewall.password.codec`），将环境键转为 `Properties` 并注入默认 Bean
- [x] 3.2 编写 `@AutoConfiguration`，`@ConditionalOnMissingBean(PasswordCodec.class)` 注册默认 `PasswordCodec`
- [x] 3.3 在 `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports` 注册自动配置类

## 4. 验证

- [x] 4.1 单元测试：bcrypt 加密后 `matches`；SM4 多 key、错误 keyId 失败；无前缀 bcrypt `matches`；`decrypt` 与明文一致
- [x] 4.2 （可选）轻量文档：README 或 `docs` 中增加配置键示例，与 `design.md` 一致
