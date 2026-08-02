package io.github.genkidoudou.system.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 登录成功响应。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginTokenVo {

  private String accessToken;

  private String tokenName;

  
}
