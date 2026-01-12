package com.su60.quickboot.system.entity;
import lombok.Data;
import com.baomidou.mybatisplus.annotation.*;
import lombok.experimental.Accessors;
import java.io.Serial;
import java.io.Serializable;
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
@TableName("sys_config")
public class SysConfigEntity implements Serializable{
@Serial
private static final long serialVersionUID = 1L;

        /**
        * 参数主键
        * @since 2026/01/11
        */

        @TableId(value = "config_id", type = IdType.ASSIGN_ID)
        private Integer configId;


        /**
        * 参数名称
        * @since 2026/01/11
        */
        @TableField("config_name")
        private String configName;


        /**
        * 参数键名
        * @since 2026/01/11
        */
        @TableField("config_key")
        private String configKey;


        /**
        * 参数键值
        * @since 2026/01/11
        */
        @TableField("config_value")
        private String configValue;


        /**
        * 系统内置（Y是 N否）
        * @since 2026/01/11
        */
        @TableField("config_type")
        private String configType;


        /**
        * 创建者
        * @since 2026/01/11
        */
        @TableField(value = "create_by", fill = FieldFill.INSERT)
        private String createBy;


        /**
        * 创建时间
        * @since 2026/01/11
        */
        @TableField(value = "create_time", fill = FieldFill.INSERT)
        private Date createTime;


        /**
        * 更新者
        * @since 2026/01/11
        */
        @TableField(value = "update_by", fill = FieldFill.INSERT_UPDATE)
        private String updateBy;


        /**
        * 更新时间
        * @since 2026/01/11
        */
        @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
        private Date updateTime;


        /**
        * 备注
        * @since 2026/01/11
        */
        @TableField("remark")
        private String remark;



}
