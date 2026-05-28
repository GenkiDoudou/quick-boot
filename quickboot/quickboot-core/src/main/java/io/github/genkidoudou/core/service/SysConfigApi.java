package io.github.genkidoudou.core.service;

public interface SysConfigApi {

  /**
   * 按键名查询参数值。
   *
   * @param configKey 参数键名
   * @return 参数值
   */
  String getConfigValueByKey(String configKey);
}
