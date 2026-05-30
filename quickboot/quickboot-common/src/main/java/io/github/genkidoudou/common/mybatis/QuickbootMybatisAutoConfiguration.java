package io.github.genkidoudou.common.mybatis;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;

/**
 * 注册 MyBatis-Plus 审计字段填充处理器。
 */
@AutoConfiguration
@ConditionalOnClass(MetaObjectHandler.class)
public class QuickbootMybatisAutoConfiguration {

    @Bean
    public MetaObjectHandler quickbootMetaObjectHandler() {
        return new QuickbootMetaObjectHandler();
    }
}
