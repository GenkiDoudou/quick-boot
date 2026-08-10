package io.github.genkidoudou.system.internal.config;

import io.github.genkidoudou.common.excel.dict.DictLookup;
import io.github.genkidoudou.common.excel.dict.DictLookupHolder;
import io.github.genkidoudou.system.internal.service.ISysDictDataService;
import io.github.genkidoudou.system.internal.support.SysDictLookup;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 将系统字典 Lookup 挂到 {@link DictLookupHolder}，供 Excel 字典转换使用。
 *
 * <p>本模块已被启动类 {@code scanBasePackages} 扫描，故使用 {@code @Configuration}
 *（与 {@link OperLogPersistConfiguration} 一致），避免再走 AutoConfiguration.imports 导致重复注册。
 */
@Configuration
@ConditionalOnBean(ISysDictDataService.class)
public class DictLookupConfiguration {

  /**
   * 注册并挂载 {@link SysDictLookup}。
   *
   * @param dictDataService 字典数据服务
   * @return DictLookup Bean
   */
  @Bean
  public DictLookup sysDictLookup(ISysDictDataService dictDataService) {
    SysDictLookup lookup = new SysDictLookup(dictDataService);
    DictLookupHolder.set(lookup);
    return lookup;
  }
}
