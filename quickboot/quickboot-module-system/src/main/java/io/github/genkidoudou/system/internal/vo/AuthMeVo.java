package io.github.genkidoudou.system.internal.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 当前登录用户信息（供 /auth/me / 前端 getInfo）。
 */
@Data
public class AuthMeVo {

  /** 与 sys_user.user_id 一致的字符串主键 */
  private String userId;

  private String username;

  private String nickName;

  private List<String> roles = new ArrayList<>();

  private List<String> permissions = new ArrayList<>();
}
