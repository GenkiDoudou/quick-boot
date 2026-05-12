package com.su60.quickboot.system.entity;
import lombok.Data;
import com.baomidou.mybatisplus.annotation.*;
import lombok.experimental.Accessors;
import java.io.Serial;
import java.io.Serializable;

/**
* <p>
* 角色和菜单关联表
* </p>
* @author luyanan
* @since 2024/08/07
*/
@Data
@Accessors(chain = true)
@TableName("sys_role_menu")
public class SysRoleMenuEntity implements Serializable{
@Serial
private static final long serialVersionUID = 1L;

    /**
    * 角色ID
    * @since 2024/08/07
    */
        @TableField("role_id")
        private Long roleId;


    /**
    * 菜单ID
    * @since 2024/08/07
    */
        @TableField("menu_id")
        private Long menuId;


    /**
    * id
    * @since 2024/08/07
    */

        @TableId(value = "id", type = IdType.ASSIGN_ID)
        private Long id;



}
