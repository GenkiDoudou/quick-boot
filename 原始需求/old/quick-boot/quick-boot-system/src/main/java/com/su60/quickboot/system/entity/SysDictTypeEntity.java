package com.su60.quickboot.system.entity;
import lombok.Data;
import com.baomidou.mybatisplus.annotation.*;
import lombok.experimental.Accessors;
import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
* <p>
* 字典类型表
* </p>
* @author luyanan
* @since 2024/08/07
*/
@Data
@Accessors(chain = true)
@TableName("sys_dict_type")
public class SysDictTypeEntity implements Serializable{
@Serial
private static final long serialVersionUID = 1L;

    /**
    * 字典主键
    * @since 2024/08/07
    */

        @TableId(value = "id", type = IdType.ASSIGN_ID)
        private Long id;


    /**
    * 字典名称
    * @since 2024/08/07
    */
        @TableField("dict_name")
        private String dictName;


    /**
    * 字典类型
    * @since 2024/08/07
    */
        @TableField("dict_type")
        private String dictType;


    /**
    * 状态（0正常 1停用）
    * @since 2024/08/07
    */
        @TableField("status")
        private String status;


    /**
    * 创建者
    * @since 2024/08/07
    */
        @TableField(value = "create_by", fill = FieldFill.INSERT)
        private Long createBy;


    /**
    * 创建时间
    * @since 2024/08/07
    */
        @TableField(value = "create_time", fill = FieldFill.INSERT)
        private Date createTime;


    /**
    * 更新者
    * @since 2024/08/07
    */
        @TableField(value = "update_by", fill = FieldFill.INSERT_UPDATE)
        private Long updateBy;


    /**
    * 更新时间
    * @since 2024/08/07
    */
        @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
        private Date updateTime;


    /**
    * 备注
    * @since 2024/08/07
    */
        @TableField("remark")
        private String remark;



}
