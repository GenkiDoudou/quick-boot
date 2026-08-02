package io.github.genkidoudou.system.entity;

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

  @TableId(value = "user_id", type = IdType.ASSIGN_ID)
  private Long userId;

  private Long deptId;

  private String userName;

  private String nickName;

  private String userType;

  private String email;

  private String phonenumber;

  private String sex;

  private String password;

  private String status;


}
