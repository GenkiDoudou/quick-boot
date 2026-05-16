package io.github.genkidoudou.web.config;



import com.baomidou.mybatisplus.annotation.DbType;

import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;

import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;

import io.github.genkidoudou.web.system.user.datascope.DataPermissionInnerInterceptor;

import org.springframework.context.annotation.Bean;

import org.springframework.context.annotation.Configuration;



/**

 * MyBatis-Plus 插件：数据权限（须先于分页）与分页；开发库为 H2（MySQL 兼容模式），与生产 MySQL 共用 {@link DbType#MYSQL} 方言分页。

 */

@Configuration

public class MybatisPlusPaginationConfig {



    /**

     * @param dataPermissionInnerInterceptor 参照旧 quick-boot 的 JSQLParser 数据权限

     * @return MP 拦截器链

     */

    @Bean

    public MybatisPlusInterceptor mybatisPlusInterceptor(

            DataPermissionInnerInterceptor dataPermissionInnerInterceptor) {

        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();

        interceptor.addInnerInterceptor(dataPermissionInnerInterceptor);

        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));

        return interceptor;

    }

}

