package com.su60.quickboot.system.dos;

import com.su60.quickboot.common.validation.UpdateGroup;
import lombok.Data;
import lombok.experimental.Accessors;

import jakarta.validation.constraints.NotNull;
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
public class SysRoleMenuDo implements Serializable{
@Serial
private static final long serialVersionUID = 1L;



    /**
    * 角色ID
    * @since 2024/08/07
    */
       private Long roleId;




    /**
    * 菜单ID
    * @since 2024/08/07
    */
       private Long menuId;




    /**
    * id
    * @since 2024/08/07
    */
     @NotNull(groups = UpdateGroup.class, message = "id 不能为空")
       private Long id;



}
