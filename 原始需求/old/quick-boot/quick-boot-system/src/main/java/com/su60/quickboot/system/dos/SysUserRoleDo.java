package com.su60.quickboot.system.dos;

import com.su60.quickboot.common.validation.UpdateGroup;
import lombok.Data;
import lombok.experimental.Accessors;

import jakarta.validation.constraints.NotNull;
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
public class SysUserRoleDo implements Serializable{
@Serial
private static final long serialVersionUID = 1L;



    /**
    * 用户ID
    * @since 2024/08/07
    */
       private Long userId;




    /**
    * 角色ID
    * @since 2024/08/07
    */
       private Long roleId;




    /**
    * id
    * @since 2024/08/07
    */
     @NotNull(groups = UpdateGroup.class, message = "id 不能为空")
       private Long id;



}
