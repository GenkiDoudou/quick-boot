/**
 * 监控域 Modulith 应用模块：仅通过 {@code api} 命名接口对外暴露类型；允许依赖 {@code system::api}。
 */
@org.springframework.modulith.ApplicationModule(
  displayName = "monitor",
  allowedDependencies = {"system :: api"}
)
package io.github.genkidoudou.monitor;