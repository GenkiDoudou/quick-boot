package com.su60.quickboot.system.entity;
import lombok.Data;
import com.baomidou.mybatisplus.annotation.*;
import lombok.experimental.Accessors;
import java.io.Serial;
import java.io.Serializable;

/**
* <p>
* 用户和角色关联表
* </p>
* @author luyanan
* @since 2024/08/07
*/
@Data
@Accessors(chain = true)
@TableName("sys_user_role")
public class SysUserRoleEntity implements Serializable{
@Serial
private static final long serialVersionUID = 1L;

    /**
    * 用户ID
    * @since 2024/08/07
    */
        @TableField("user_id")
        private Long userId;


    /**
    * 角色ID
    * @since 2024/08/07
    */
        @TableField("role_id")
        private Long roleId;


    /**
    * id
    * @since 2024/08/07
    */

        @TableId(value = "id", type = IdType.ASSIGN_ID)
        private Long id;



}
