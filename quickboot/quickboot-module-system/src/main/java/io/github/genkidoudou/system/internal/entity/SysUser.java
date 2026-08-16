package io.github.genkidoudou.system.internal.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.github.genkidoudou.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 系统用户实体，与表 {@code sys_user} 对应（最小字段集，支撑角色分配用户）。
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("sys_user")
public class SysUser extends BaseEntity implements Serializable {

  @Serial
  private static final long serialVersionUID = 1L;

  /** 用户主键。 */
  @TableId(value = "user_id", type = IdType.ASSIGN_ID)
  private Long userId;

  /** 所属部门 ID。 */
  private Long deptId;

  /** 登录账号，唯一。 */
  private String userName;

  /** 用户昵称。 */
  private String nickName;

  /** 用户类型（业务扩展标识，可空）。 */
  private String userType;

  /** 邮箱。 */
  private String email;

  /** 手机号。 */
  private String phonenumber;

  /** 性别(sys_user_sex)。 */
  private String sex;

  /** 登录密码（加密存储）。 */
  private String password;

  /** 状态(sys_normal_disable)：0=正常，1=停用。 */
  private String status;


}
