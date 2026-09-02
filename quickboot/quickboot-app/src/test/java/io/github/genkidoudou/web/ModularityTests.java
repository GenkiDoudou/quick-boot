package io.github.genkidoudou.web;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Modulith 结构校验：基于启动类探测模块并执行 {@link ApplicationModules#verify()}。
 * <p>
 * 业务模块根包须带 {@code @ApplicationModule}（common/core/system/monitor/quartz/tool/report 等）。
 */
class ModularityTests {

  @Test
  void verifiesApplicationModules() {
    ApplicationModules modules = ApplicationModules.of(WebApplication.class);
    assertThat(modules.stream().map(m -> m.getDisplayName()))
      .contains("System", "Monitor", "Quartz", "Report", "Tool");
    modules.verify();
  }
}
