package com.su60.quickboot.data.spring.restful;

import com.su60.quickboot.data.spring.SpringExtendProperties;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 结果封装的配置类
 *
 * @author luyanan
 * @since 2023/09/09
 **/
@Data
@ConfigurationProperties(prefix = RestFulProperties.PREFIX)
public class RestFulProperties {

    public static final String PREFIX = SpringExtendProperties.PREFIX + ".restful";


    /**
     * 是否开启
     *
     * @since 2023/09/09
     */
    private Boolean enable;


    /**
     * 排除不需要统一返回的包路径
     *
     * @since 2023/09/10
     */

    private String[] excludePackages;


    /**
     * 需要添加统一返回的包路径
     *
     * @since 2023/09/10
     */

    private String[] includePackages;
}
