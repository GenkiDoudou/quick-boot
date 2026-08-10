package io.github.genkidoudou.web.modulith;

import java.util.List;

import org.springframework.modulith.core.ApplicationModuleSourceFactory;

/**
 * 测试期将业务域包注册为 Modulith 应用模块基包。
 */
public class SystemApplicationModuleSourceFactory implements ApplicationModuleSourceFactory {

  @Override
  public List<String> getModuleBasePackages() {
    return List.of(
      "io.github.genkidoudou.system",
      "io.github.genkidoudou.quartz",
      "io.github.genkidoudou.tool",
      "io.github.genkidoudou.report",
      "io.github.genkidoudou.monitor"
    );
  }
}
