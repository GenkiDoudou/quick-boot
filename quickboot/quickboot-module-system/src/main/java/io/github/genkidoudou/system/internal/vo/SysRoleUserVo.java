package io.github.genkidoudou.system.internal.vo;

import lombok.Data;

/**
 * 角色授权用户行。
 */
@Data
public class SysRoleUserVo {

  private String userId;

  private String userName;

  private String nickName;

  /** 0=正常 1=停用 */
  private String status;
}
