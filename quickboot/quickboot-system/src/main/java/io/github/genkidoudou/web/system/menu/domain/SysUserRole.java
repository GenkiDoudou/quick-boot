package io.github.genkidoudou.web.system.menu.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 用户与角色关联（最小表，与占位登录 userId=1 联调）。
 */
@Data
@TableName("sys_user_role")
public class SysUserRole implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long userId;

    private Long roleId;
}
