package io.github.genkidoudou.web;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;

/**
 * Modulith 结构校验骨架。
 * <p>
 * 使用 {@link ApplicationModules#of(Class)} 基于启动类所在包探测模块；
 * 后续将启动类上移或通过 {@code ApplicationModuleSourceFactory} 启用
 * {@code explicitly-annotated}，仅校验带 {@code @ApplicationModule} 的业务模块。
 */
class ModularityTests {

  @Test
  void verifiesApplicationModules() {
    ApplicationModules.of(WebApplication.class).verify();
  }
}
