package io.github.genkidoudou.web.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import io.github.genkidoudou.common.monitor.slowsql.SlowSqlMapperIdInnerInterceptor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis-Plus 插件：慢 SQL mapper_id 标记（可选）+ 分页。
 * <p>
 * 开发库为 H2（MySQL 兼容模式），与生产 MySQL 共用 {@link DbType#MYSQL} 方言分页。
 */
@Configuration
public class MybatisPlusPluginConfiguration {

  /**
   * @param slowSqlMapperIdInnerInterceptor 采集开启时由 common AutoConfiguration 提供
   * @return MP 拦截器链
   */
  @Bean
  public MybatisPlusInterceptor mybatisPlusInterceptor(
    ObjectProvider<SlowSqlMapperIdInnerInterceptor> slowSqlMapperIdInnerInterceptor
  ) {
    MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
    slowSqlMapperIdInnerInterceptor.ifAvailable(interceptor::addInnerInterceptor);
    interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
    return interceptor;
  }
}
