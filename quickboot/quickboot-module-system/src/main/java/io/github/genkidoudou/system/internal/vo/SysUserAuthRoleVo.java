package io.github.genkidoudou.system.internal.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 用户授权角色读写 VO（含可选角色列表）。
 */
@Data
public class SysUserAuthRoleVo {
  /** 用户主键。 */
  private Long userId;
  /** 登录账号。 */
  private String userName;
  /** 用户昵称。 */
  private String nickName;
  /** 已选角色 ID 列表。 */
  private List<Long> roleIds = new ArrayList<>();
  /** 可选角色列表（含详情）。 */
  private List<SysRoleVo> roles = new ArrayList<>();
}
