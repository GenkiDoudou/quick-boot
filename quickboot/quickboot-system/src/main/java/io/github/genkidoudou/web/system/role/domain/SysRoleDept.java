package io.github.genkidoudou.web.system.role.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 角色与部门关联（数据权限为「自定义」时使用）。
 */
@Data
@TableName("sys_role_dept")
public class SysRoleDept implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long roleId;

    private Long deptId;
}
