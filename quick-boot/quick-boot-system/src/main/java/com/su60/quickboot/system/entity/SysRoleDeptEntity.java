package com.su60.quickboot.system.entity;
import lombok.Data;
import com.baomidou.mybatisplus.annotation.*;
import lombok.experimental.Accessors;
import java.io.Serial;
import java.io.Serializable;

/**
* <p>
* 角色关联部门表
* </p>
* @author luyanan
* @since 2025/12/27
*/
@Data
@Accessors(chain = true)
@TableName("sys_role_dept")
public class SysRoleDeptEntity implements Serializable{
@Serial
private static final long serialVersionUID = 1L;

        /**
        * 
        * @since 2025/12/27
        */
        @TableField("role_id")
        private Long roleId;


        /**
        * 
        * @since 2025/12/27
        */
        @TableField("dept_id")
        private Long deptId;


        /**
        * id
        * @since 2025/12/27
        */

        @TableId(value = "id", type = IdType.ASSIGN_ID)
        private Long id;



}
