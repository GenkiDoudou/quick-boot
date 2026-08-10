package io.github.genkidoudou.system.internal.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class SysUserAuthRoleVo {
  private Long userId;
  private String userName;
  private String nickName;
  private List<Long> roleIds = new ArrayList<>();
  private List<SysRoleVo> roles = new ArrayList<>();
}
