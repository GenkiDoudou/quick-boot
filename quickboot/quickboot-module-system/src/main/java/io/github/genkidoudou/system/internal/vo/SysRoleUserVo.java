package io.github.genkidoudou.system.internal.vo;

import lombok.Data;

/**
 * 角色授权用户行。
 */
@Data
public class SysRoleUserVo {

  /** 用户 ID。 */
  private String userId;

  /** 登录账号。 */
  private String userName;

  /** 用户昵称。 */
  private String nickName;

  /** 0=正常 1=停用 */
  private String status;
}
