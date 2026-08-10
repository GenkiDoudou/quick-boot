package io.github.genkidoudou.system.internal.vo;

import io.github.genkidoudou.common.validation.group.AddGroup;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 登录请求类
 *
 * @author luyanan
 * @since 2026/7/30
 */
@Data
public class LoginRequestVo {


  /**
   * 用户名
   *
   * @since 2026/7/30
   */

  @NotBlank(message = "用户名不能为空", groups = AddGroup.class)
  private String username;


  /**
   * 密码
   *
   * @since 2026/7/30
   */
  @NotBlank(message = "密码不能为空", groups = AddGroup.class)
  private String password;


  /**
   * 验证码uuid
   *
   * @since 2026/7/30
   */

  private String uuid;
}
