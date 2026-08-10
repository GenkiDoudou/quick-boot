package io.github.genkidoudou.common.config;

/**
 * 按配置键读取参数值（由 system 等域提供实现；他域可选注入）。
 */
@FunctionalInterface
public interface ConfigValueLookup {

  /**
   * @param configKey 配置键，如 {@code qc.gen.author}
   * @return 配置值，不存在时可返回 null
   */
  String getConfigValueByKey(String configKey);
}
