package com.su60.quickboot.system.dos;

import com.su60.quickboot.common.validation.AddGroup;
import com.su60.quickboot.common.validation.UpdateGroup;
import lombok.Data;
import lombok.experimental.Accessors;
import java.io.Serial;
import java.io.Serializable;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Date;

/**
* <p>
* 参数配置表
* </p>
* @author luyanan
* @since 2026/01/11
*/
@Data
@Accessors(chain = true)
public class SysConfigDo implements Serializable{
@Serial
private static final long serialVersionUID = 1L;



    /**
    * 参数主键
    * @since 2026/01/11
    */
    @NotNull(groups = UpdateGroup.class, message = "参数主键 不能为空")
    private Integer configId;




    /**
    * 参数名称
    * @since 2026/01/11
    */
    private String configName;




    /**
    * 参数键名
    * @since 2026/01/11
    */
    private String configKey;




    /**
    * 参数键值
    * @since 2026/01/11
    */
    private String configValue;




    /**
    * 系统内置（Y是 N否）
    * @since 2026/01/11
    */
    private String configType;




    /**
    * 创建者
    * @since 2026/01/11
    */
        @JsonIgnore
    private String createBy;




    /**
    * 创建时间
    * @since 2026/01/11
    */
    private Date createTime;




    /**
    * 更新者
    * @since 2026/01/11
    */
        @JsonIgnore
    private String updateBy;




    /**
    * 更新时间
    * @since 2026/01/11
    */
        @JsonIgnore
    private Date updateTime;




    /**
    * 备注
    * @since 2026/01/11
    */
    private String remark;



}
