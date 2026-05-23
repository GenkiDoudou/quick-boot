package com.su60.quickboot.data.spring;

import com.su60.quickboot.data.spring.restful.RestFulProperties;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * spring的扩展配置
 *
 * @author luyanan
 * @since 2023/09/09
 **/
@Data
@ConfigurationProperties(prefix = SpringExtendProperties.PREFIX)
public class SpringExtendProperties {

    /**
     * 前缀
     *
     * @since 2023/09/09
     */

    public static final String PREFIX = "spring.extends";


    /**
     * 是否开启
     *
     * @since 2023/09/10
     */

    private Boolean enable;
    /**
     * 统一返回
     *
     * @since 2023/09/10
     */

    private RestFulProperties restFul;

}
